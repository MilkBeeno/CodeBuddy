---
description: Android 网络层封装规范，定义 ApiResult、safeApiCall、RetrofitFactory、拦截器及数据分层准则，所有业务模块必须遵守
alwaysApply: true
enabled: true
---

# 网络层封装规范（base 模块）

网络层统一封装在 `base/network/`，**禁止**业务模块自行创建 OkHttpClient / Retrofit 实例。

```
network/
├── interceptor/
│   ├── LoggingInterceptor.kt   # 日志（仅 Debug）
│   ├── HeaderInterceptor.kt    # 注入 Authorization / App-Version / Platform
│   └── AuthInterceptor.kt      # 401 自动刷新 Token
├── RetrofitFactory.kt          # OkHttp + Retrofit 工厂
└── ApiResult.kt                # 网络结果密封类
```

---

## 一、ApiResult

```kotlin
sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>
    data class Error(val code: Int, val message: String, val throwable: Throwable? = null) : ApiResult<Nothing>
    data object Loading : ApiResult<Nothing>
}
// 错误码常量：TIMEOUT=-1 / NO_NETWORK=-2 / IO=-3 / UNKNOWN=-99
```

---

## 二、safeApiCall

所有网络请求**必须**通过此函数，Repository 层统一返回 `ApiResult`。

```kotlin
fun <T> safeApiCall(call: suspend () -> T): Flow<ApiResult<T>> = flow {
    emit(ApiResult.Loading)
    emit(ApiResult.Success(call()))
}.catch { emit(classifyError(it)) }.flowOn(Dispatchers.IO)
```

- **禁止**在 ViewModel 中 `try-catch` 网络异常
- UI 层不得跳过 `Loading` 状态

---

## 三、RetrofitFactory

```kotlin
RetrofitFactory(
    baseUrl      = BuildConfig.BASE_URL,
    isDebug      = BuildConfig.DEBUG,
    tokenProvider   = { session.token },
    tokenRefresher  = { authRepo.refresh() },
    onTokenExpired  = { navToLogin() }
)
// 默认超时：CONNECT=15s / READ=30s / WRITE=30s
```

---

## 四、拦截器职责

| 拦截器                  | 职责                                        | 约束                      |
|----------------------|-------------------------------------------|-------------------------|
| `LoggingInterceptor` | 打印请求/响应日志                                 | 仅 Debug，禁止生产包输出 Token   |
| `HeaderInterceptor`  | 注入 Authorization / App-Version / Platform | Token 通过 Lambda 动态读取    |
| `AuthInterceptor`    | 拦截 401，同步刷新并重试（最多 1 次）                    | 刷新失败调用 `onTokenExpired` |

---

## 五、Service 接口

```kotlin
interface UserService {
    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: String): UserResponse  // 必须 suspend，禁止返回 Call<T>
}
```

- `BaseResponse` 中 `code != 200` 抛业务异常
- 禁止日志打印 Token / 密码

---

## 六、数据分层

```
DTO (RemoteModel)  →  .toDomain()  →  Entity (DomainModel)
```

- `toDomain()` 定义在模块 `mapper` 包，转换**必须在 Repository 层**完成

```kotlin
fun fetchUser(id: String) = safeApiCall { service.getUser(id).toDomain() }
```

---

## 七、Hilt 注入

```kotlin
@Module @InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Singleton
    fun provideRetrofit(): Retrofit = RetrofitFactory(/*...*/).create()

    @Provides @Singleton
    fun provideUserService(retrofit: Retrofit): UserService =
        retrofit.create(UserService::class.java)
}
```

---

## 八、禁止事项

| 禁止行为                               | 原因                            |
|------------------------------------|-------------------------------|
| 业务模块自建 `OkHttpClient` / `Retrofit` | 拦截器失效                         |
| ViewModel `try-catch` 网络异常         | 由 `safeApiCall.catch {}` 统一处理 |
| Service 方法返回 `Call<T>`             | 不兼容协程                         |
| DTO 直接传给 ViewModel / UI            | 违反数据分层                        |
| 日志打印 Token / 密码                    | 安全风险                          |
