---
description: Android MAD 技术栈总规范，涵盖命名、Compose UI、Flow/协程、Hilt、分页、测试的核心约束，网络/导航/存储详见各子规范文件
alwaysApply: true
enabled: true
---

# Android MAD 技术栈规范

> 坚持 UDF + 响应式编程，生成代码须类型安全、零 Warning。
> 网络见 `network.md`，导航见 `navigation.md`，Room 见 `room.md`，DataStore 见 `datastore.md`。

---

## 一、命名规范

| 场景             | 规范                     | 示例               |
|----------------|------------------------|------------------|
| `const val`    | `SCREAMING_SNAKE_CASE` | `BASE_URL`       |
| 类 / Composable | `PascalCase`           | `LoginViewModel` |
| 函数 / 变量        | `camelCase`            | `fetchUser()`    |
| 回调参数           | `on` 前缀                | `onItemClick`    |
| 资源文件           | `snake_case`           | `ic_back.xml`    |

- 魔法数字 / 字符串提取为 `companion object` 的 `const val`
- 提交前通过 `Detekt` / `ktlint` 扫描，零容忍命名类警告

---

## 二、Jetpack Compose UI

**状态管理**
- State / 事件 Lambda 必须提升（Hoisting），Composable 保持无状态
- 用 `collectAsStateWithLifecycle()` 订阅 `UiState`，**禁止** `collectAsState()`
- 一次性事件（Toast / 导航）用 `Channel`，在 `LaunchedEffect` 消费

```kotlin
val uiEffect = Channel<Effect>(Channel.BUFFERED)
LaunchedEffect(Unit) { viewModel.uiEffect.receiveAsFlow().collect { /* handle */ } }
```

**性能**
- 复杂 Data Class 加 `@Immutable` / `@Stable`
- 耗时计算放 `remember` / `derivedStateOf`
- `LazyColumn` / `LazyRow` 必须提供稳定 `key`

**布局**
- 优先 Material 3，颜色 / 字体通过 `MaterialTheme` 引用，禁止硬编码
- 必须处理 `WindowInsets`；字符串 / 尺寸用 `stringResource()` / `dimensionResource()`
- 单个 Composable 不超过 80 行

---

## 三、Flow & 协程

| 场景                       | 作用域              |
|--------------------------|------------------|
| ViewModel 业务逻辑           | `viewModelScope` |
| Activity / Fragment 生命周期 | `lifecycleScope` |
| Repository 内部            | 禁止 `GlobalScope` |

```kotlin
// 冷流 → 热流
val uiState = repo.observeData()
    .map { UiState.Success(it) }
    .catch { emit(UiState.Error(it.message)) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)
```

- 搜索防抖：`.debounce(300).distinctUntilChanged().filter { it.isNotBlank() }`
- 取最新值：`flatMapLatest`；并行合并：`combine` / `zip`
- Flow 异常用 `.catch {}`；`callbackFlow` 必须有 `awaitClose {}`
- 禁止 `Thread.sleep()`；共享状态用 `Atomic` 类或 `Mutex`

---

## 四、依赖注入（Hilt）

- ViewModel：`@HiltViewModel` + `@Inject constructor`
- Repository / Service：`@Inject constructor` 或 `@Provides`
- 配置项用 `@Named` 注入，禁止硬编码

```kotlin
@HiltViewModel
class LoginViewModel @Inject constructor(private val repo: UserRepository) : ViewModel()

@Module @InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Named("BASE_URL")
    fun provideBaseUrl(): String = BuildConfig.BASE_URL
}
```

---

## 五、分页（Paging 3）

- 纯网络：继承 `PagingSource`；离线优先：`RemoteMediator`（网络写 Room，UI 观察本地）
- 禁止在 `load()` 中切 `Dispatchers.Main`

```kotlin
// ViewModel
val pagingFlow = Pager(PagingConfig(pageSize = 20)) { userDao.pagingSource() }
    .flow.cachedIn(viewModelScope)

// UI
val items = viewModel.pagingFlow.collectAsLazyPagingItems()
LazyColumn {
    items(items, key = { it.id }) { UserCard(it) }
    item {
        when (val s = items.loadState.append) {
            is LoadState.Loading -> CircularProgressIndicator()
            is LoadState.Error   -> RetryButton { items.retry() }
            else -> Unit
        }
    }
}
```

---

## 六、测试

- **ViewModel**：`kotlinx-coroutines-test` + `Turbine` 验证 StateFlow / Channel
- **Repository**：Mock Service，验证 `ApiResult` 各状态
- **Room**：`Room.inMemoryDatabaseBuilder`
- 命名格式：`LoginViewModelTest`

```kotlin
@Test fun `login success emits NavigateToHome`() = runTest {
    viewModel.uiEffect.receiveAsFlow().test {
        viewModel.onLoginClick("138xxxx", "pwd")
        assertIs<Effect.NavigateToHome>(awaitItem())
    }
}
```

---

## 七、常用触发指令

| 指令                   | 说明                                     |
|----------------------|----------------------------------------|
| `[构建 Screen 骨架]`     | Scaffold + TopAppBar + FAB 的 MVI 页面    |
| `[实现 MVI UI]`        | Loading / Empty / Success / Error 四态布局 |
| `[冷流转热流]`            | Repository 冷流 → ViewModel StateFlow    |
| `[实现搜索建议]`           | debounce + flatMapLatest               |
| `[实现标准分页]`           | PagingSource + ViewModel + LazyColumn  |
| `[写 RemoteMediator]` | 离线优先分页                                 |
| `[定义类型安全路由]`         | @Serializable 路由 + 导航跳转                |
| `[DataStore 封装]`     | AppPreferences 主题 / 语言配置               |
| `[生成 API 接口]`        | 根据 JSON 生成 Data Class + suspend 接口     |
