package com.milk.codebuddy.base.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response

/**
 * 请求头拦截器
 * 职责：向每一个请求统一注入 Token、版本号、平台标识等通用 Header
 *
 * @param tokenProvider 动态获取 Token 的 Lambda，避免持有过期引用
 * @param appVersion    App 版本名，建议从 BuildConfig.VERSION_NAME 注入
 */
class HeaderInterceptor(
    private val tokenProvider: () -> String?,
    private val appVersion: String
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = tokenProvider()

        val requestBuilder = originalRequest.newBuilder()
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .header("App-Version", appVersion)
            .header("Platform", "Android")

        if (!token.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        return chain.proceed(requestBuilder.build())
    }
}
