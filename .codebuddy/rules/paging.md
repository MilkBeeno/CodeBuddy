---
description: Android Paging 3 分页规范，分页基础设施统一封装在 base/paging/，定义 BasePagingSource、RemoteMediator、ViewModel、UI 层的准则与代码模板
alwaysApply: true
enabled: true
---

# PPaging 3 分页库开发规范 (MAD Architecture)

本规范定义了在处理大规模数据集时，如何实现高性能、响应式且具备错误处理机制的分页加载。我们坚持 协程流 (Flow) 与 三层架构 的深度集成。

---

## 一、依赖配置

```toml
# libs.versions.toml
[versions]
paging = "3.3.6"

[libraries]
androidx-paging-runtime = { group = "androidx.paging", name = "paging-runtime", version.ref = "paging" }
androidx-paging-compose = { group = "androidx.paging", name = "paging-compose", version.ref = "paging" }
androidx-paging-testing = { group = "androidx.paging", name = "paging-testing", version.ref = "paging" }
```

```kotlin
// base/build.gradle.kts
dependencies {
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)
    testImplementation(libs.androidx.paging.testing)
}
```

---

## 二、核心技术规范

### 数据流架构

- **单一数据流**：分页数据必须以 `Flow<PagingData<T>>` 的形式从 `Repository` 层向 UI 层传递。
- **作用域绑定**：必须使用 `cachedIn(viewModelScope)`。这能确保在配置更改（如屏幕旋转）时，分页状态得以保留，且避免数据重复加载。

### 数据源选型 (PagingSource)

- **唯一真实数据源 (SSOT)**：如果应用支持离线模式，必须使用 `Room + RemoteMediator` 方案。UI 只观察 Room 的分页数据。
- **Key 类型**：网络分页通常使用 `Int` (页码)；对于无限滚动流，推荐使用 `String` (游标/`Cursor`)。

### UI 适配器

- **PagingDataAdapter**：必须使用 `PagingDataAdapter` 以获得内置的 `DiffUtil` 差分更新支持。
- **状态监听**：必须通过 `loadStateFlow` 或 `addLoadStateListener` 处理加载中、空状态和错误状态。

---

## 三、约束与原则

### 性能约束

- **禁止在 UI 层过滤**：所有的 `filter`、`map` 转换必须在 `Flow<PagingData>` 发射前通过 `Paging` 库提供的 `map` 或 `filter` 操作符完成。
- **预加载优化**：根据业务需求合理配置 `PagingConfig` 的 `prefetchDistance`（预取距离），默认值为 `pageSize`。

### 错误处理

- **重试机制**：每个分页列表必须提供“点击重试”功能，调用 `adapter.retry()`。
- **异常捕获**：在 `PagingSource` 的 `load` 方法中，必须使用 `try-catch` 包裹请求，并返回 `LoadResult.Error(e)`。

### 转换解耦

- **禁止 Entity 穿透**：`PagingSource` 加载的数据库/网络 `Entity` 必须在 `Repository` 层转换为 `Domain Model`。

---

## 四、Agent 工作流

网络数据写入 Room，UI 始终观察本地数据库。

1. **创建 PagingSource**：实现 `load` 函数，处理 `Key` 的递增/递减。
2. **定义 RemoteMediator (可选)**：如果需要网络+本地缓存，编写协调逻辑。
3. **构建 Pager**：在 `Repository` 中配置 `PagingConfig` 并返回 `Flow`。
4. **ViewModel 缓存**：调用 `cachedIn(viewModelScope)`。
5. **UI 绑定**：创建 `PagingDataAdapter` 并将 `LoadState` 绑定到 `ProgressBar/ErrorView`。

---

## 五、见指令参考

- **下拉刷新**：直接调用 `adapter.refresh()`。
- **空数据处理**：监听 `loadState.refresh is LoadState.NotLoading && adapter.itemCount == 0`。
- **追加 Header/Footer**：使用 `adapter.withLoadStateHeaderAndFooter(...)` 快速添加加载进度条。
- **本地更新**：如果需要局部修改列表项（如点赞），应在数据库层修改 `Entity`，触发 `Room` 的分页流自动刷新，而不是手动操作 `Adapter` 数据。

```kotlin
// ViewModel 中调用
@HiltViewModel
class UserViewModel @Inject constructor(
    private val source: UserPagingSource          // 纯网络
    // private val mediator: UserRemoteMediator   // 离线优先
    // private val dao: UserDao
) : ViewModel() {

    // 纯网络
    val pagingFlow = buildPager { source }.cachedIn(viewModelScope)

    // 离线优先
    // val pagingFlow = buildPagerWithMediator(mediator = mediator) { dao.pagingSource() }
    //     .cachedIn(viewModelScope)
}

// Pager 加载数据直接返回
class ArticleRepository @Inject constructor(
    private val service: ApiService
) {
    fun getArticles(): Flow<PagingData<Article>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                initialLoadSize = 20
            ),
            pagingSourceFactory = { ArticlePagingSource(service) }
        ).flow
    }
}

// Pageer 加载数据存到本地中更新
@OptIn(ExperimentalPagingApi::class)
class UserRemoteMediator @Inject constructor(
    private val service: UserService,
    private val dao: UserDao,
    private val db: AppDatabase
) : RemoteMediator<Int, UserEntity>() {

    override suspend fun load(loadType: LoadType, state: PagingState<Int, UserEntity>): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> 1
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND  -> state.lastItemOrNull()?.page?.plus(1)
                ?: return MediatorResult.Success(endOfPaginationReached = true)
        }
        return try {
            val items = service.getUsers(page, state.config.pageSize)
            db.withTransaction {
                if (loadType == LoadType.REFRESH) dao.clearAll()
                dao.insertAll(items.map { it.toDomain() })
            }
            MediatorResult.Success(endOfPaginationReached = items.isEmpty())
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}
```

---

## 六、性能与测试

* `REFRESH` 时必须清空旧数据，数据库操作必须包裹在 `db.withTransaction {}`。
* `PREPEND` 固定返回 `endOfPaginationReached = true`。
* `cachedIn(viewModelScope)` 必须添加，防止屏幕旋转重新请求。
* 禁止在 ViewModel 中手动维护 `page` 变量。
* 禁止直接调用 `Pager(...)` 而非 `buildPager` / `buildPagerWithMediator`、绕过统一 `PagingConfig`。
* 禁止 ViewModel 暴露 `List<T>` 而非 `Flow<PagingData<T>>`，失去增量加载能力。
