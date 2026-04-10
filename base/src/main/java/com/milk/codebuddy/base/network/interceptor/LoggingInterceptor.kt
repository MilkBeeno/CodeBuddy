package com.milk.codebuddy.base.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor

/**
 * 日志拦截器
 * 规范：仅允许在 DEBUG 模式下启用，防止生产环境泄露敏感信息
 */
class LoggingInterceptor(private val isDebug: Boolean) : Interceptor {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (isDebug) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        return loggingInterceptor.intercept(chain)
    }
}
