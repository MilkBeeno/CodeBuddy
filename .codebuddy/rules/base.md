---
description: Android base 模块公共组件总规范，定义模块结构索引与各层核心约束，详细规范见各子文件
alwaysApply: true
enabled: true
---

# Android 公共组件规范（base 模块）

> 子规范：`compose.md` | `datestore.md`| `navigation.md` | `network.md` | `paging.md` | `room.md`

`base` 是全项目公用基础库，所有业务模块依赖此模块，**禁止**在业务模块中重复实现已有能力。

```
com.milk.codebuddy.base
├── network/                        # 网络层 → network.md
│   ├── interceptor/
│   │   ├── LoggingInterceptor.kt   # 日志（仅 Debug）
│   │   ├── HeaderInterceptor.kt    # 注入 Authorization / App-Version / Platform
│   │   └── AuthInterceptor.kt      # 401 自动刷新 Token
│   ├── RetrofitFactory.kt          # OkHttp + Retrofit 工厂
│   └── ApiResult.kt                # 网络结果密封类
├── paging/                         # 分页层 → paging.md
│   ├── BasePagingSource.kt         # 通用 PagingSource 封装
│   └── PagingExt.kt                # Pager 构建扩展函数
├── datastore/                      # 存储层 → datastore.md / room.md
│   ├── AppPreferences.kt
│   ├── AppPreferencesKeys.kt
│   └── room/
│       ├── AppDatabase.kt
│       ├── BaseDao.kt
│       └── converter/CommonConverters.kt
├── utils/                          # 通用工具、扩展函数
└── ui/
    ├── navigation/                 # 导航 → navigation.md
    │   ├── NavControllerLocals.kt
    │   └── Screen.kt
    └── theme/
        ├── Color.kt                # 原始色板 + AppColors 语义层
        ├── Theme.kt                # MaterialTheme Light/Dark 配置
        └── Type.kt                 # 字体排版系统
```

---

## 命名规范

| 场景             | 规范                     | 示例               |
|----------------|------------------------|------------------|
| `const val`    | `SCREAMING_SNAKE_CASE` | `BASE_URL`       |
| 类 / Composable | `PascalCase`           | `LoginViewModel` |
| 函数 / 变量        | `camelCase`            | `fetchUser()`    |
| 回调参数           | `on` 前缀                | `onItemClick`    |
| 资源文件           | `snake_case`           | `ic_back.xml`    |

- 魔法数字 / 字符串提取为 `companion object` 的 `const val`。
- 提交前通过 `Detekt` / `ktlint` 扫描，零容忍命名类警告。

---

## Flow & 协程

| 场景                  | 作用域                  |
|---------------------|----------------------|
| ViewModel 业务逻辑      | `viewModelScope`     |
| Activity / Fragment | `lifecycleScope`     |
| Repository 内部       | **禁止** `GlobalScope` |

```kotlin
val uiState = repo.observeData()
    .map { UiState.Success(it) }
    .catch { emit(UiState.Error(it.message)) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)
```

- 搜索防抖：`.debounce(300).distinctUntilChanged().filter { it.isNotBlank() }`。
- 取最新值用 `flatMapLatest`；并行合并用 `combine` / `zip`。
- Flow 异常统一用 `.catch {}`；`callbackFlow` 必须有 `awaitClose {}`。
- **禁止** `Thread.sleep()`；共享状态用 `Atomic` 类或 `Mutex`。

---

## 依赖注入（Hilt）

- ViewModel：`@HiltViewModel` + `@Inject constructor`。
- Repository / Service：`@Inject constructor` 或 `@Provides`。
- 配置项用 `@Named` 注入，**禁止**硬编码。

```kotlin
@HiltViewModel
class LoginViewModel @Inject constructor(private val repo: UserRepository) : ViewModel()
```

---

## UI 主题

> 详见 **compose.md**

- 自定义主题 `AppTheme` 控制全局页面主题。
- 颜色通过 `LocalAppColors.current` 访问，**禁止**硬编码 `Color(0xFF…)`。
- 原始色板 `Color_XXXXXX` 仅在 `AppColors` 组装时引用，业务代码不得直接引用。
- 字体通过 `MaterialTheme.typography` 引用，**禁止**硬编码 `fontSize` / `fontWeight`。
- `Theme.kt` 支持 ·Light / Dark，`AppColors` 依据 `isSystemInDarkTheme()` 自动切换。

---

## 导航器

> 详见 **navigation.md**

### NavController 访问

- 路由定义在 `Screen.kt`，使用 `@Serializable`，**禁止**硬编码字符串路由。
- 通过 `LocalNavController.current` 获取，**禁止**逐层参数透传。
- ViewModel **禁止**持有 `NavController`，导航动作通过 `UiEffect` 传递。

```kotlin
// 根节点
ProvideNavHostController(rememberNavController()) { AppNavHost() }

// 任意子节点
val navController = LocalNavController.current
```

### 导航图拆分

按业务模块拆分为扩展函数，在根 `NavHost` 统一注册。

```kotlin
// app 模块
@Composable
fun AppNavHost() {
    val nav = LocalNavController.current
    NavHost(nav, startDestination = Splash) {
        loginGraph()
    }
}

// login 模块
fun NavGraphBuilder.loginGraph() {
    composable<Splash>  { SplashScreen() }
    composable<Login>   { LoginScreen() }
    composable<Register>{ RegisterScreen() }
}
```

### 导航动作

- 所有导航动作必须加 `launchSingleTop = true`
- 登录后跳主页必须 `popUpTo` 清栈

```kotlin
// 普通跳转
navController.navigate(Login) { launchSingleTop = true }

// 登录成功清栈跳主页
navController.navigate(Main) {
    popUpTo<Splash> { inclusive = true }
    launchSingleTop = true
}

// 带参跳转
navController.navigate(ResetPassword(phone = "138xxxx")) { launchSingleTop = true }
```

---

## 网络层

> 详见 **network.md**

### ApiResult

```kotlin
sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>
    data class Error(val code: Int, val message: String, val throwable: Throwable? = null) : ApiResult<Nothing>
    data object Loading : ApiResult<Nothing>
}
// 错误码常量：TIMEOUT=-1 / NO_NETWORK=-2 / IO=-3 / UNKNOWN=-99
```

### safeApiCall

- 所有请求通过 `safeApiCall` 包装，Repository 统一返回 `ApiResult`
- **禁止**业务模块自建 `OkHttpClient` / `Retrofit`
- DTO→Entity 转换在 Repository 层完成，`toDomain()` 定义在 `mapper` 包

### RetrofitFactory

```kotlin
RetrofitFactory(
    baseUrl      = BuildConfig.BASE_URL,
    isDebug      = BuildConfig.DEBUG,
    tokenProvider   = { session.token },
    tokenRefresher  = { authRepo.refresh() },
    onTokenExpired  = { navToLogin() }
)
// 默认超时：CONNECT=20s / READ=30s / WRITE=30s
```

### 数据分层

```
DTO (RemoteModel)  →  .toDomain()  →  Entity (DomainModel)
```

- `toDomain()` 定义在模块 `mapper` 包，转换**必须在 Repository 层**完成

```kotlin
fun fetchUser(id: String) = safeApiCall { service.getUser(id).toDomain() }
```

---

## 分页（Paging 3）

> 详见 **paging.md**

### BasePagingSource

- 所有纯网络分页继承 `BasePagingSource`，统一处理 `getRefreshKey` 和异常捕获。

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

### PagingExt

- 统一 `PagingConfig` 默认参数，业务代码通过扩展函数构建 `Pager`。

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

### UI 层

- `key` 必须使用 Entity 主键，禁止使用 `index`
- 占位符为 `null` 时必须处理
- 必须同时处理 `refresh`（首次）和 `append`（追加）两种错误态

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

### 搜索 / 过滤分页

```kotlin
val query = MutableStateFlow("")

val pagingFlow = query
    .debounce(300)
    .distinctUntilChanged()
    .flatMapLatest { q -> buildPager { SearchPagingSource(service, q) } }
    .cachedIn(viewModelScope)
```

---

## 数据存储

> DataStore 详见 **datastore.md**，Room 详见 **room.md**

- 简单键值对（Token、主题、语言等）用 `Preferences DataStore`，**禁止**使用 `SharedPreferences`。
- 复杂结构体、强类型用 `Proto DataStore`。
- 关系型数据、多表用 Room，**禁止**业务模块自建 `Database` 实例
- ViewModel **禁止**直接依赖 DAO / `AppPreferences`，统一通过 Repository 访问

### DataStore

#### Key 管理

- 所有 Key 集中定义在 `AppPreferencesKeys.kt`，**禁止**在业务模块中散落定义。

```kotlin
object AppPreferencesKeys {
    val ACCESS_TOKEN    = stringPreferencesKey("access_token")
}
```
#### AppPreferences 封装

```kotlin
private val Context.dataStore by preferencesDataStore(name = "app_preferences")

@Singleton
class AppPreferences @Inject constructor(@ApplicationContext context: Context) {

    private val dataStore = context.dataStore

    fun <T> observe(key: Preferences.Key<T>, default: T): Flow<T> =
        dataStore.data
            .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
            .map { it[key] ?: default }

    suspend fun <T> get(key: Preferences.Key<T>, default: T): T =
        observe(key, default).first()

    suspend fun <T> put(key: Preferences.Key<T>, value: T) =
        dataStore.edit { it[key] = value }

    suspend fun <T> remove(key: Preferences.Key<T>) =
        dataStore.edit { it.remove(key) }

    // 语义化属性
    val isLoggedIn: Flow<Boolean> = observe(AppPreferencesKeys.IS_LOGGED_IN, false)
    val appTheme: Flow<String>    = observe(AppPreferencesKeys.APP_THEME, "system")

    suspend fun saveTokens(access: String, refresh: String) = dataStore.edit {
        it[AppPreferencesKeys.ACCESS_TOKEN]  = access
        it[AppPreferencesKeys.REFRESH_TOKEN] = refresh
        it[AppPreferencesKeys.IS_LOGGED_IN]  = true
    }

    suspend fun clearSession() = dataStore.edit {
        it.remove(AppPreferencesKeys.ACCESS_TOKEN)
        it.remove(AppPreferencesKeys.REFRESH_TOKEN)
        it[AppPreferencesKeys.IS_LOGGED_IN] = false
    }
}
```

#### Repository 层集成

- 涉及认证/会话**必须**通过 Repository 封装，ViewModel 不直接依赖 `AppPreferences`。

```kotlin
interface SessionRepository {
    val isLoggedIn: Flow<Boolean>
    suspend fun saveTokens(access: String, refresh: String)
    suspend fun clearSession()
}

class SessionRepositoryImpl @Inject constructor(
    private val prefs: AppPreferences
) : SessionRepository {
    override val isLoggedIn = prefs.isLoggedIn
    override suspend fun saveTokens(access: String, refresh: String) = prefs.saveTokens(access, refresh)
    override suspend fun clearSession() = prefs.clearSession()
}

@Module @InstallIn(SingletonComponent::class)
abstract class SessionModule {
    @Binds @Singleton
    abstract fun bindSession(impl: SessionRepositoryImpl): SessionRepository
}
```

#### ViewModel 使用

```kotlin
@HiltViewModel
class SettingsViewModel @Inject constructor(private val session: SessionRepository) : ViewModel() {
    val isLoggedIn = session.isLoggedIn
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
}
```

### Room

#### 业务 DAO

- DAO 方法全部返回 `Flow<T>` 或 `suspend`，**禁止**同步方法
- 多表关联加 `@Transaction`

```kotlin
@Dao
interface UserDao : BaseDao<UserEntity> {
    @Query("SELECT * FROM user WHERE uid = :uid")
    fun observeByUid(uid: String): Flow<UserEntity?>   // observe 前缀 = 持续监听

    @Query("SELECT * FROM user ORDER BY created_at DESC")
    fun observeAll(): Flow<List<UserEntity>>

    @Query("SELECT * FROM user WHERE uid = :uid LIMIT 1")
    suspend fun findByUid(uid: String): UserEntity?    // find 前缀 = 一次性读取

    @Query("DELETE FROM user")
    suspend fun clearAll()
}
```

#### Repository 层

- `ViewModel` **禁止**直接依赖 DAO，统一通过 `Repository` 接口访问。

```kotlin
interface UserLocalRepository {
    fun observeAll(): Flow<List<UserEntity>>
    suspend fun save(entity: UserEntity)
    suspend fun delete(entity: UserEntity)
}

class UserLocalRepositoryImpl @Inject constructor(private val dao: UserDao) : UserLocalRepository {
    override fun observeAll() = dao.observeAll()
    override suspend fun save(entity: UserEntity) = dao.upsert(entity)
    override suspend fun delete(entity: UserEntity) = dao.delete(entity)
}

@Module @InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton
    abstract fun bindUserLocal(impl: UserLocalRepositoryImpl): UserLocalRepository
}
```

### ViewModel 调用

```kotlin
val uiState = localRepo.observeAll()
    .map { UserUiState.Success(it) }
    .catch { emit(UserUiState.Error(it.message ?: "")) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserUiState.Loading)
```

---

## 新增公共组件原则

1. **通用性**：至少被 2 个业务模块使用，单业务逻辑**禁止**放入 `base`
2. **无业务依赖**：`base` 不得依赖 `login`、`main` 等业务模块
3. **KDoc**：每个公共类/函数必须说明职责、参数、使用示例
4. **可测试**：公共工具类必须配套单元测试（正常路径 + 异常路径）

---

## 常用触发指令

| 指令                   | 说明                                         |
|----------------------|--------------------------------------------|
| `[构建 Screen 骨架]`     | Scaffold + TopAppBar + FAB 的 MVI 页面        |
| `[实现 MVI UI]`        | Loading / Empty / Success / Error 四态布局     |
| `[冷流转热流]`            | Repository 冷流 → ViewModel StateFlow        |
| `[实现搜索建议]`           | debounce + flatMapLatest                   |
| `[实现标准分页]`           | BasePagingSource + buildPager + LazyColumn |
| `[写 RemoteMediator]` | 离线优先分页                                     |
| `[定义类型安全路由]`         | @Serializable 路由 + 导航跳转                    |
| `[DataStore 封装]`     | AppPreferences 主题 / 语言配置                   |
| `[生成 API 接口]`        | 根据 JSON 生成 DTO + suspend 接口                |
