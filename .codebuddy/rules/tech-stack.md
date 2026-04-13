---
description: Android 现代架构（MAD）技术栈规范，涵盖命名、Compose UI、Flow/协程、存储、网络、分页、导航等全领域约束与代码模板
alwaysApply: true
enabled: true
---

# Android MAD 技术栈规范

> 角色：精通现代 Android 开发栈的资深架构师，坚持 UDF + 响应式编程，生成代码须高性能、类型安全、零 Warning。

---

## 一、命名规范

| 场景             | 规范                     | 示例                          |
|----------------|------------------------|-----------------------------|
| `const val`    | `SCREAMING_SNAKE_CASE` | `const val BASE_URL = "…"`  |
| 类 / Composable | `PascalCase`           | `LoginViewModel`、`UserCard` |
| 函数 / 变量        | `camelCase`            | `fetchUser()`、`isLoading`   |
| 回调参数           | `on` 前缀                | `onItemClick`、`onSubmit`    |
| 资源文件           | `snake_case`           | `ic_back.xml`               |

- 魔法数字 / 字符串提取为 `companion object` 的 `const val`
- 提交前通过 `Detekt` / `ktlint` 扫描，零容忍命名类警告

---

## 二、Jetpack Compose UI

**状态管理**
- State / 事件 Lambda 必须提升（Hoisting），Composable 保持无状态
- 用 `collectAsStateWithLifecycle()` 订阅 `UiState`，禁止 `collectAsState()`
- 一次性事件（Toast / 导航）用 `Channel` 或 `SharedFlow(replay=0)`，在 `LaunchedEffect` 消费

```kotlin
// ViewModel
val uiEffect = Channel<LoginEffect>(Channel.BUFFERED)
// UI
LaunchedEffect(Unit) {
    viewModel.uiEffect.receiveAsFlow().collect { effect -> /* handle */ }
}
```

**性能**
- 复杂 Data Class 加 `@Immutable` / `@Stable`
- 耗时计算放 `remember` / `derivedStateOf`，禁止在函数体直接计算
- `LazyColumn` / `LazyRow` 必须提供稳定 `key`

**布局**
- 优先 Material 3，颜色 / 字体通过 `MaterialTheme` 引用，禁止硬编码
- 必须处理 `WindowInsets`；String / Dimension 用 `stringResource()` / `dimensionResource()`
- 单个 Composable 不超过 80 行，复杂嵌套拆分子函数

---

## 三、Flow & 协程

| 场景                     | 作用域              |
|------------------------|------------------|
| ViewModel 业务逻辑         | `viewModelScope` |
| Activity/Fragment 生命周期 | `lifecycleScope` |
| Repository 内部          | 禁止 `GlobalScope` |

```kotlin
// 冷流 → 热流
val uiState = repo.observeUser(id)
    .map { UiState.Success(it) }
    .catch { emit(UiState.Error(it.message)) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)
```

- 搜索防抖：`debounce(300)` + `distinctUntilChanged()` + `filter { it.isNotBlank() }`
- 取最新值：`flatMapLatest`；并行合并：`combine` / `zip`
- Flow 异常用 `.catch { }`；`callbackFlow` 必须有 `awaitClose { }`
- 禁止 `Thread.sleep()`；共享状态用 `Atomic` 类或 `Mutex`

---

## 四、依赖注入（Hilt）

- ViewModel：`@HiltViewModel` + `@Inject constructor`
- Repository / Service：`@Inject constructor` 或 `@Provides`
- `BaseUrl` 等配置用 `@Named` 注入，禁止硬编码

```kotlin
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repo: UserRepository
) : ViewModel()

@Module @InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Named("BASE_URL")
    fun provideBaseUrl(): String = BuildConfig.BASE_URL
}
```

---

## 五、存储

**Room**
- DAO 查询返回 `Flow<T>`，禁止同步阻塞；多表操作用 `@Transaction`
- 复杂字段配 `@TypeConverter`；插入用 `OnConflictStrategy.REPLACE`
- 方法名体现响应式：`observeUserById()` 而非 `getUser()`

```kotlin
@Dao interface UserDao {
    @Query("SELECT * FROM user WHERE id = :id")
    fun observeUserById(id: String): Flow<User>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: UserEntity)
}
```

**DataStore**
- 简单键值对 → `Preferences DataStore`；复杂结构体 → `Proto DataStore`
- 封装在 Repository / Manager 中，禁止 UI 层直接操作
- 读取加 `catch { e -> if (e is IOException) emit(defaultValue) }`；退出时清空敏感数据

---

## 六、网络

> 具体实现（`ApiResult`、`safeApiCall`、拦截器）见 `base_component.md`。

- Service 方法必须 `suspend`，禁止返回 `Call<T>`
- `BaseUrl` 从 `BuildConfig` 读取；`BaseResponse` 中 `code != 200` 抛业务异常
- 禁止日志打印 Token / 密码等敏感字段

```kotlin
interface UserService {
    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: String): UserResponse
}
```

---

## 七、分页（Paging 3）

- 纯网络：继承 `PagingSource`；离线优先：`RemoteMediator`（网络 → Room → UI）
- 数据变更后调用 `PagingSource.invalidate()`；禁止在 `load()` 切 `Dispatchers.Main`

```kotlin
// ViewModel
val pagingFlow = Pager(PagingConfig(pageSize = 20, prefetchDistance = 5)) {
    userDao.pagingSource()
}.flow.cachedIn(viewModelScope)

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

## 八、导航（Navigation Compose）

> 路由定义、`LocalNavController` 用法见 `base_component.md`。

- 禁止硬编码字符串路由；所有导航动作加 `launchSingleTop = true`
- ViewModel 不感知导航，由 UI 层消费 `UiEffect` 后执行
- 登录后跳主页用 `popUpTo(inclusive = true)` 清栈

```kotlin
fun NavGraphBuilder.authGraph(navController: NavController) {
    composable<LoginRoute> { LoginScreen() }
    composable<RegisterRoute> { RegisterScreen() }
}
```

---

## 九、测试

- **ViewModel**：`kotlinx-coroutines-test` + `Turbine` 验证 StateFlow / Channel
- **Repository**：Mock Service，验证 `ApiResult` 各状态流转
- **Room**：`Room.inMemoryDatabaseBuilder` 做 DAO 测试
- 命名：`LoginViewModelTest`

```kotlin
@Test fun `login success emits NavigateToHome`() = runTest {
    viewModel.uiEffect.receiveAsFlow().test {
        viewModel.onLoginClick("13800138000", "123456")
        assertIs<LoginEffect.NavigateToHome>(awaitItem())
    }
}
```

---

## 十、常用触发指令

| 指令                   | 说明                                        |
|----------------------|-------------------------------------------|
| `[构建 Screen 骨架]`     | 生成 Scaffold + TopAppBar + FAB 的 MVI 页面    |
| `[实现 MVI UI]`        | 生成 Loading / Empty / Success / Error 四态布局 |
| `[冷流转热流]`            | Repository 冷流 → ViewModel StateFlow       |
| `[实现搜索建议]`           | debounce + flatMapLatest 搜索流              |
| `[配置拦截器]`            | 完整 OkHttp 拦截器链                            |
| `[实现标准分页]`           | PagingSource + ViewModel + LazyColumn     |
| `[写 RemoteMediator]` | 离线优先分页，网络写 Room，UI 观察本地                   |
| `[定义类型安全路由]`         | @Serializable 路由类 + 导航跳转代码                |
| `[DataStore 封装]`     | AppSettingManager 管理主题 / 语言配置             |
| `[生成 API 接口]`        | 根据 JSON 生成 Data Class + suspend 接口        |