---
description: Android Paging 3 分页规范，分页基础设施统一封装在 base/paging/，定义 BasePagingSource、RemoteMediator、ViewModel、UI 层的准则与代码模板
alwaysApply: true
enabled: true
---

# Paging 3 分页规范（base 模块）

> 分页统一使用 Jetpack Paging 3，基础设施封装在 `base/paging/`，**禁止**业务模块自建分页模板代码或手动维护页码状态。

```
base/paging/
├── BasePagingSource.kt   # 通用 PagingSource 封装（错误处理 + getRefreshKey）
└── PagingExt.kt          # Pager 构建扩展函数，统一 PagingConfig 默认参数
```

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

## 二、BasePagingSource（base 封装）

所有纯网络分页继承 `BasePagingSource`，统一处理 `getRefreshKey` 和异常捕获。

```kotlin
// base/paging/BasePagingSource.kt
abstract class BasePagingSource<T : Any> : PagingSource<Int, T>() {

    override fun getRefreshKey(state: PagingState<Int, T>) =
        state.anchorPosition?.let { state.closestPageToPosition(it)?.prevKey?.plus(1) }

    abstract suspend fun fetchPage(page: Int, pageSize: Int): List<T>

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        val page = params.key ?: 1
        return try {
            val items = fetchPage(page, params.loadSize)
            LoadResult.Page(
                data    = items,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (items.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
```

### 业务 PagingSource 示例

```kotlin
class UserPagingSource @Inject constructor(
    private val service: UserService
) : BasePagingSource<UserEntity>() {
    override suspend fun fetchPage(page: Int, pageSize: Int) =
        service.getUsers(page, pageSize).map { it.toDomain() }
}
```

---

## 三、PagingExt（base 封装）

统一 `PagingConfig` 默认参数，业务代码通过扩展函数构建 `Pager`。

```kotlin
// base/paging/PagingExt.kt
fun <T : Any> buildPager(
    pageSize: Int = 20,
    prefetchDistance: Int = pageSize,
    source: () -> PagingSource<Int, T>
): Flow<PagingData<T>> =
    Pager(PagingConfig(pageSize = pageSize, prefetchDistance = prefetchDistance)) { source() }.flow

@OptIn(ExperimentalPagingApi::class)
fun <T : Any> buildPagerWithMediator(
    pageSize: Int = 20,
    mediator: RemoteMediator<Int, T>,
    source: () -> PagingSource<Int, T>
): Flow<PagingData<T>> =
    Pager(PagingConfig(pageSize = pageSize), remoteMediator = mediator) { source() }.flow
```

---

## 四、RemoteMediator（离线优先）

网络数据写入 Room，UI 始终观察本地数据库。

```kotlin
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

- `REFRESH` 时必须清空旧数据，数据库操作必须包裹在 `db.withTransaction {}`
- `PREPEND` 固定返回 `endOfPaginationReached = true`

---

## 五、ViewModel

```kotlin
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
```

- `cachedIn(viewModelScope)` 必须添加，防止屏幕旋转重新请求
- 禁止在 ViewModel 中手动维护 `page` 变量

---

## 六、UI 层（Compose）

```kotlin
val items = viewModel.pagingFlow.collectAsLazyPagingItems()

LazyColumn {
    items(items, key = { it.id }) { user ->
        if (user != null) UserCard(user)
    }
    item {
        when (val s = items.loadState.append) {
            is LoadState.Loading -> CircularProgressIndicator(Modifier.fillMaxWidth().wrapContentWidth())
            is LoadState.Error   -> RetryButton { items.retry() }
            else -> Unit
        }
    }
}

// 首次加载失败
if (items.loadState.refresh is LoadState.Error) {
    ErrorScreen { items.refresh() }
}
```

- `key` 必须使用 Entity 主键，禁止使用 `index`
- 占位符为 `null` 时必须处理
- 必须同时处理 `refresh`（首次）和 `append`（追加）两种错误态

---

## 七、搜索 / 过滤分页

```kotlin
val query = MutableStateFlow("")

val pagingFlow = query
    .debounce(300)
    .distinctUntilChanged()
    .flatMapLatest { q -> buildPager { SearchPagingSource(service, q) } }
    .cachedIn(viewModelScope)
```

---

## 八、测试

```kotlin
@Test fun `load returns correct page`() = runTest {
    val source = UserPagingSource(fakeService)
    val result = source.load(
        PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false)
    )
    assertIs<PagingSource.LoadResult.Page<Int, UserEntity>>(result)
    assertEquals(20, result.data.size)
    assertNull(result.prevKey)
}
```

---

## 九、禁止事项

| 禁止行为                                                         | 原因                  |
|--------------------------------------------------------------|---------------------|
| 手动维护 `page` / `offset` 变量                                    | Paging 3 已内置        |
| 直接继承 `PagingSource` 而非 `BasePagingSource`                    | 绕过统一错误处理            |
| 直接调用 `Pager(...)` 而非 `buildPager` / `buildPagerWithMediator` | 绕过统一 `PagingConfig` |
| 不加 `cachedIn(viewModelScope)`                                | 旋转屏幕会重新请求           |
| `LazyColumn` 的 `key` 使用 `index`                              | 数据变化时动画错乱           |
| `RemoteMediator` 的 `REFRESH` 不清空旧数据                          | 产生重复数据              |
| ViewModel 暴露 `List<T>` 而非 `Flow<PagingData<T>>`              | 失去增量加载能力            |
