---
description: Android 网络层封装规范，定义 ApiResult、safeApiCall、RetrofitFactory、拦截器及数据分层准则，所有业务模块必须遵守
alwaysApply: false
enabled: true
---

# Android 网络层开发规范 (Retrofit + Flow + Coroutines)

本规范定义了基于 OkHttp、Retrofit 和 Kotlin 协程的网络通信标准。我们坚持 分离关注点 (SoC) 原则，确保网络层具备极高的可观测性和容错能力。

---

## 一、依赖配置

```toml
[versions]
retrofit = "2.9.0"
okhttp = "4.12.0"

[libraries]
network-retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
network-retrofit-serialization = { group = "com.jakewharton.retrofit", name = "retrofit2-kotlinx-serialization-converter", version = "1.0.0" }
network-okhttp = { group = "com.squareup.okhttp3", name = "okhttp", version.ref = "okhttp" }
network-okhttp-logging = { group = "com.squareup.okhttp3", name = "logging-interceptor", version.ref = "okhttp" }
```

---

## 二、核心技术规范

### 异步模型 (Concurrency)

- **挂起函数优先**：Retrofit 定义必须使用 `suspend` 关键字。
- **Flow 转换**：在 `Repository` 层将网络请求结果封装为 `Flow<Resource<T>>` 或 `Flow<T>`，以便利用 `Flow` 的操作符（如 `retry`, `catch`, `debounce`）。
- **非阻塞调度**：所有网络请求必须显式指定在 `Dispatchers.IO` 线程池中执行。

### OkHttp 拦截器 (Interceptors)

- **日志拦截器**：仅在 DEBUG 模式下启用 `HttpLoggingInterceptor`。
- **头部参数拦截器**：添加网络请求公用头部信息 `HeaderLoggingInterceptor`。
- **Auth 拦截器**：统一处理 `Token` 注入、刷新机制以及 401 状态码 `AuthInterceptor`。
- **超时配置**：`Connect/Read/Write` 超时时间原则上不应超过 20 秒。

### 序列化

- **Kotlin Serialization 优先**：推荐使用官方 `kotlinx-serialization`，避免使用 GSON 导致的可空性（`Null-safety`）失效问题。

---

## 三、约束与原则

### 响应包装 (Response Handling)

- **禁止原始数据透传**：`Repository` 不应直接返回 `retrofit2.Response<T>`。必须在数据层处理状态码，并将其映射为业务逻辑对象或封装类（如 `Result<T>` 或 `NetworkResult<T>`）。
- **统一异常处理**：必须捕获 `IOException`（网络连接）和 `HttpException`（服务器返回错误码）。

### 接口定义规范 (API Interface)

- **单一职责**：每个 `Service` 接口应对应一个功能模块（如 `AuthService`, `UserService`）。
- **参数校验**：使用 `@Body`, `@Query`, `@Path` 等注解时，必须确保 Kotlin 参数的 `@Nullable` 与 API 文档一致。

### 内存泄漏防护

- **生命周期绑定**：请求必须在 `viewModelScope` 或 `lifecycleScope` 中启动，确保页面销毁时自动取消正在进行的网络任务。

---

### 四、Agent 工作流

1. **定义数据模型**：创建符合 API 结构的 @Serializable Data Class。
2. **编写 Service 接口**：声明 suspend 方法。
3. **配置 OkHttp/Retrofit**：在 Hilt 模块中生成 Retrofit 实例，包含 Converter.Factory。
4. **封装 BaseRepository**：编写通用的 safeApiCall 方法，将 suspend 调用包装成 Flow。
5. **编写测试**：使用 MockWebServer 验证边界情况（404, 500, 超时）。

---

## 五、见指令参考

- **处理重试**：使用 Flow 的 `retryWhen { cause, attempt -> ... }` 实现指数退避重试。
- **取消请求**：由于使用了协程，只需调用 `Job.cancel()` 即可。
- **并发请求**：使用 zip 操作符合并两个网络流，或使用 `async/await` 处理多个并行请求。

### 通用请求包装函数

```kotlin
// 在 BaseRepository 中定义
suspend fun <T> safeApiCall(
    apiCall: suspend () -> T
): Flow<Result<T>> = flow {
    try {
        emit(Result.success(apiCall.invoke()))
    } catch (e: Exception) {
        emit(Result.failure(e))
    }
}.flowOn(Dispatchers.IO)
```

### Service 接口示例

```kotlin
interface ApiService {
    @GET("users/{id}")
    suspend fun getUserProfile(
        @Path("id") userId: String
    ): UserResponse
}
```

---

## 六、性能与测试

* **拦截器失效**：禁止业务模块自建 `OkHttpClient` / `Retrofit`
* **不兼容协程**：禁止 `Service` 方法返回 `Call<T>`
* **违反数据分层**：禁止 DTO 直接传给 ViewModel / UI
* **安全风险**：禁止日志打印 Token / 密码
