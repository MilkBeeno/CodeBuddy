package com.milk.codebuddy.base.network

import com.milk.codebuddy.base.network.interceptor.AuthInterceptor
import com.milk.codebuddy.base.network.interceptor.HeaderInterceptor
import com.milk.codebuddy.base.network.interceptor.LoggingInterceptor
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

/**
 * Retrofit 网络工厂
 *
 * 配置规范：
 * - BaseUrl 必须通过构造参数注入，禁止硬编码
 * - 日志拦截器仅 DEBUG 模式启用
 * - 所有请求强制在 Dispatchers.IO 执行
 *
 * @param baseUrl        服务器基础地址，建议从 BuildConfig 注入
 * @param isDebug        是否为调试模式，控制日志拦截器开关
 * @param tokenProvider  动态获取当前 Token 的 Lambda
 * @param tokenRefresher 同步刷新 Token 的 Lambda，失败返回 null
 * @param onTokenExpired Token 失效时的全局回调（如触发退出登录）
 */
class RetrofitFactory(
    private val baseUrl: String,
    private val isDebug: Boolean,
    private val tokenProvider: () -> String?,
    private val tokenRefresher: () -> String?,
    private val onTokenExpired: () -> Unit
) {

    private val gson = GsonBuilder()
        .setLenient()
        .serializeNulls()
        .create()

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(HeaderInterceptor(tokenProvider, APP_VERSION))
            .addInterceptor(AuthInterceptor(tokenRefresher, onTokenExpired))
            .addInterceptor(LoggingInterceptor(isDebug))
            .build()
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    /** 创建 Service 实例 */
    inline fun <reified T> createService(): T = retrofit.create(T::class.java)

    companion object {
        private const val CONNECT_TIMEOUT_SECONDS = 15L
        private const val READ_TIMEOUT_SECONDS = 30L
        private const val WRITE_TIMEOUT_SECONDS = 30L
        private const val APP_VERSION = "1.0.0"
    }
}

/**
 * 通用安全网络请求封装
 *
 * 将挂起函数包装为 Flow<ApiResult<T>>，统一处理：
 * - Loading 状态发射
 * - 网络异常分类转换（超时、无网络等）
 * - 业务异常透传
 * - 强制在 Dispatchers.IO 执行
 *
 * 使用示例：
 * ```kotlin
 * fun fetchUser(id: String): Flow<ApiResult<User>> = safeApiCall {
 *     service.getUser(id).toDomain()
 * }
 * ```
 */
fun <T> safeApiCall(call: suspend () -> T): Flow<ApiResult<T>> = flow {
    emit(ApiResult.Loading)
    val result = call()
    emit(ApiResult.Success(result))
}.catch { throwable ->
    val error = when (throwable) {
        is SocketTimeoutException -> ApiResult.Error(
            code = ERROR_CODE_TIMEOUT,
            message = "请求超时，请检查网络后重试",
            throwable = throwable
        )
        is UnknownHostException -> ApiResult.Error(
            code = ERROR_CODE_NO_NETWORK,
            message = "无法连接网络，请检查网络设置",
            throwable = throwable
        )
        is IOException -> ApiResult.Error(
            code = ERROR_CODE_IO,
            message = "网络异常，请稍后重试",
            throwable = throwable
        )
        else -> ApiResult.Error(
            code = ERROR_CODE_UNKNOWN,
            message = throwable.message ?: "未知错误",
            throwable = throwable
        )
    }
    emit(error)
}.flowOn(Dispatchers.IO)

private const val ERROR_CODE_TIMEOUT = -1
private const val ERROR_CODE_NO_NETWORK = -2
private const val ERROR_CODE_IO = -3
private const val ERROR_CODE_UNKNOWN = -99
