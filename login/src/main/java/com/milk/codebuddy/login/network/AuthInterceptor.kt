package com.milk.codebuddy.login.network

import com.milk.codebuddy.login.data.local.SessionManager
import kotlinx.coroutines.flow.first
import okhttp3.Interceptor
import okhttp3.Response

/**
 * 认证拦截器
 * 为请求自动添加 Access Token
 */
class AuthInterceptor(
    private val sessionManager: SessionManager
) : Interceptor {

    companion object {
        private const val HEADER_AUTHORIZATION = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        // 同步获取会话信息
        val session = runBlocking {
            sessionManager.userSession.first()
        }
        val request = chain.request().newBuilder()

        // 如果有 access token，添加到请求头
        if (session.accessToken.isNotEmpty()) {
            request.addHeader(HEADER_AUTHORIZATION, BEARER_PREFIX + session.accessToken)
        }

        return chain.proceed(request.build())
    }

    // 注意：在实际项目中，Interceptor 的 intercept 方法是同步的，
    // 所以无法完全避免 runBlocking，除非使用 OkHttp 的异步 API
    private fun <T> runBlocking(block: suspend () -> T): T {
        return kotlinx.coroutines.runBlocking(kotlinx.coroutines.Dispatchers.IO) {
            block()
        }
    }
}
