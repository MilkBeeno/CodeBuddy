package com.milk.codebuddy.base.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.net.HttpURLConnection

/**
 * Token 刷新拦截器
 * 职责：当服务端返回 401 时，同步刷新 Token 并自动重试原请求（最多 1 次）
 *
 * @param tokenRefresher  同步刷新 Token 的回调，成功返回新 Token，失败返回 null
 * @param onTokenExpired  Token 刷新失败时的回调（如跳转登录页）
 */
class AuthInterceptor(
    private val tokenRefresher: () -> String?,
    private val onTokenExpired: () -> Unit
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (response.code != HttpURLConnection.HTTP_UNAUTHORIZED) {
            return response
        }

        // 关闭旧响应，避免资源泄漏
        response.close()

        val newToken = tokenRefresher()
        if (newToken.isNullOrBlank()) {
            onTokenExpired()
            return chain.proceed(request)
        }

        // 使用新 Token 重新发起请求
        val retryRequest = request.newBuilder()
            .header("Authorization", "Bearer $newToken")
            .build()
        return chain.proceed(retryRequest)
    }
}
