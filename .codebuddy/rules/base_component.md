---
description:
alwaysApply: false
enabled: true
updatedAt: 2026-04-09T04:20:41.159Z
provider:
---

# Role：Android 公共组件

## 概览 (Profile)

本规范定义了 Android 项目中数据模型（Base Model）、状态持有者以及数据流的核心准则，旨在确保代码的高性能、类型安全及 100% 可测试性。

## 核心技术 (Core Technology)

项目中公用组件在这里定义、严格按照以下结构进行构建。

```.text
com.milk.codebuddy.base
├── network/                                                        # 基类网络组件封装 Retrofit+Okhttp+Coroutine+Flow
│   ├── interceptor/                                                # 页面导航组件
│   │   ├── LoggingInterceptor.kt                                     # 日志拦截器
│   │   ├── HeaderInterceptor.kt                                      # 请求头拦截器
│   │   └── AuthInterceptor.kt                                        # Token 刷新拦截器
│   ├── RetrofitFactory.kt                                          # Retrofit 生产功能
│   └── ApiResult.kt                                                # 数据返回
├── datastore/                                                      # 数据存储功能、DataStore 、Room 数据库
├── utils/                                                          # 通用工具、扩展函数、基类
└── ui/                                                             # 展示层 (按功能模块划分)
    ├── navigation/                                                 # 页面导航组件
    │   ├── NavControllerLocals.kt                                    # 导航全局控制
    │   └── Screen.kt                                                 # 屏幕全局命名
    └── theme/                                                      # 设计系统 (Color, Type, Shape)
        ├── Color.kt                                                  # 导航全局控制
        ├── Theme.kt                                                  # 导航全局控制
        └── Type.kt                                                   # 屏幕全局命名
```

#### 网络请求

* **传输层**： OkHttp 4.12+ (配置合理的超时与连接池)。
* **协议层**： Retrofit 2.11+ (配合 Kotlin 挂起函数)。
* **流式处理**： 必须返回 `Flow<ApiResult<T>>`，严禁在 Repository 以上层级处理原始 `Response`
* **线程调度**： 网络请求必须强制在 `Dispatchers.IO` 执行，通过 `flowOn(Dispatchers.IO)` 显式声明。
* **数据模型分离**：
    - RemoteModel (DTO)：对应 JSON 结构，允许使用 SerialName。
    - DomainModel (Entity): App 内部业务逻辑使用的模型。
    - 转换器: 必须在 Repository 层完成从 DTO 到 Entity 的 Map 转换。
* **错误处理与 Result 封装**：严禁使用 try-catch 散落在 ViewModel 中。必须使用统一的密封类（Sealed Class）包装结果。

```kotlin 
sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>
    data class Error(val code: Int, val message: String, val throwable: Throwable? = null) : ApiResult<Nothing>
    data object Loading : ApiResult<Nothing>
}
```

## Agent 工作流（WorkFlow）

* **Trigger**: ViewModel 发起 Action。
* **Flow Stream**: Repository 调用 Service 并使用 flow { ... } 包装。
* **Catch**: 使用 .catch { ... } 操作符捕获网络 IO 异常并转换为 ApiResult.Error。
* **Transform**: 使用 .map { ... } 将 DTO 转换为 Domain Model。
* **Collect**: ViewModel 收集 Flow 并更新 UiState。

* ## 约束原则 (Hard Rules)
* **禁止硬编码**: 所有的 BaseURL 和 Timeout 必须通过 DI (Hilt/Koin) 注入。
* **拦截器规范**: 日志拦截器 (HttpLoggingInterceptor) 仅允许在 DEBUG 模式下启用。
* **类型安全**: 强制检查 response.isSuccessful，不得直接信任后端返回的 Body。
* **取消策略**: 必须利用 viewModelScope 确保网络请求随生命周期自动 Job Cancellation。
* **防抖过滤**: 对于搜索类请求，必须在 Flow 流中使用 debounce(300ms)。

## 常用指令参考 (Quick Prompts)

* **统一流转换模版**：

```kotlin
fun <T> safeApiCall(call: suspend () -> T): Flow<ApiResult<T>> = flow {
    emit(ApiResult.Loading)
    val response = call()
    emit(ApiResult.Success(response))
}.catch { e ->
    emit(ApiResult.Error(code = -1, message = e.message ?: "Unknown Error", throwable = e))
}.flowOn(Dispatchers.IO)
```

* **Repository 调用示例**：

```kotlin
class UserRepository(private val service: UserService) {
    fun fetchUser(id: String): Flow<ApiResult<User>> = safeApiCall {
        service.getUser(id).toDomain() // 这里的 .toDomain() 是扩展函数
    }
}
```