---
description: Android base 模块公共组件规范，定义网络层、存储层、UI 主题、导航的使用准则与代码模板，所有业务模块必须遵守
alwaysApply: true
enabled: true
---

# Android 公共组件规范（base 模块）

`base` 是全项目公用基础库，所有业务模块依赖此模块，**禁止**在业务模块中重复实现已有能力。

```
com.milk.codebuddy.base
├── network/
│   ├── interceptor/
│   │   ├── LoggingInterceptor.kt   # 日志拦截器（仅 Debug）
│   │   ├── HeaderInterceptor.kt    # 注入 Authorization / App-Version / Platform
│   │   └── AuthInterceptor.kt      # 401 自动刷新 Token
│   ├── RetrofitFactory.kt          # OkHttp + Retrofit 工厂 + safeApiCall
│   └── ApiResult.kt                # 网络结果密封类
├── datastore/                      # DataStore / Room
├── utils/                          # 通用工具、扩展函数
└── ui/
    ├── navigation/
    │   ├── NavControllerLocals.kt  # LocalNavController + ProvideNavHostController
    │   └── Screen.kt               # 全局类型安全路由（@Serializable）
    └── theme/
        ├── Color.kt                # 原始色板 + AppColors 语义层
        ├── Theme.kt                # MaterialTheme Light/Dark 配置
        └── Type.kt                 # 字体排版系统
```

---

## 一、网络层

### ApiResult 密封类

```kotlin
sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>
    data class Error(val code: Int, val message: String, val throwable: Throwable? = null) : ApiResult<Nothing>
    data object Loading : ApiResult<Nothing>
}
```

### safeApiCall（所有网络请求必须通过此函数）

```kotlin
// 错误码常量：TIMEOUT=-1 / NO_NETWORK=-2 / IO=-3 / UNKNOWN=-99
fun <T> safeApiCall(call: suspend () -> T): Flow<ApiResult<T>> = flow {
    emit(ApiResult.Loading)
    emit(ApiResult.Success(call()))
}.catch { emit(classifyError(it)) }.flowOn(Dispatchers.IO)
```

- Repository 层统一包装为 `ApiResult`，禁止在 ViewModel 中 `try-catch`
- UI 层不得跳过 `Loading` 状态

### RetrofitFactory 配置

```kotlin
RetrofitFactory(
    baseUrl = BuildConfig.BASE_URL,
    isDebug = BuildConfig.DEBUG,
    tokenProvider = { session.token },
    tokenRefresher = { authRepo.refresh() },
    onTokenExpired = { navToLogin() }
)
// 默认超时：CONNECT=15s / READ=30s / WRITE=30s
```

### 拦截器职责

| 拦截器                  | 职责                                        | 约束                      |
|----------------------|-------------------------------------------|-------------------------|
| `LoggingInterceptor` | 打印请求/响应日志                                 | 仅 Debug，禁止生产包输出 Token   |
| `HeaderInterceptor`  | 注入 Authorization / App-Version / Platform | Token 通过 Lambda 动态读取    |
| `AuthInterceptor`    | 拦截 401，同步刷新并重试（最多 1 次）                    | 刷新失败调用 `onTokenExpired` |

### 数据分层

```
RemoteModel (DTO)  →  .toDomain()  →  DomainModel (Entity)
```
- DTO→Entity 转换**必须在 Repository 层**完成，`toDomain()` 定义在模块 `mapper` 包
- Repository 模板：`fun fetchUser(id: String) = safeApiCall { service.getUser(id).toDomain() }`

---

## 二、UI 主题

**颜色**（`Color.kt`）
- 原始色板 `Color_XXXXXX`：仅在组装 `AppColors` 时引用
- 语义层 `AppColors`（`primaryTextColor` / `secondaryTextColor` / `primaryBackgroundColor` 等）：业务代码唯一入口
- 禁止在 Composable 中直接用 `Color_XXXXXX` 或硬编码 `Color(0xFF…)`，通过 `LocalAppColors.current` 访问

**字体**：通过 `MaterialTheme.typography` 引用，禁止硬编码 `fontSize` / `fontWeight`

**主题**：`Theme.kt` 支持 Light / Dark，`AppColors` 依据 `isSystemInDarkTheme()` 切换

---

## 三、导航

**路由定义**（`Screen.kt`）
```kotlin
@Serializable object Splash
@Serializable object Login
@Serializable object Main
@Serializable data class ResetPassword(val phone: String)
```
- 禁止硬编码字符串路由；新增页面必须在 `Screen.kt` 追加，不得在业务模块自行声明

**NavController 访问**（`NavControllerLocals.kt`）
```kotlin
// 根节点
ProvideNavHostController(navController = rememberNavController()) { AppNavHost() }
// 任意子节点
val navController = LocalNavController.current
```
- 禁止参数逐层透传；ViewModel 不得持有 `NavController`

---

## 四、新增公共组件原则

1. **通用性**：至少被 2 个业务模块使用，单业务逻辑禁止放入 `base`
2. **无业务依赖**：`base` 不得依赖 `login`、`main` 等业务模块
3. **KDoc**：每个公共类/函数必须说明职责、参数、使用示例
4. **可测试**：公共工具类必须配套单元测试（正常路径 + 异常路径）
