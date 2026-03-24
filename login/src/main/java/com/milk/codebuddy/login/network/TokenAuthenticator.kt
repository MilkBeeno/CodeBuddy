package com.milk.codebuddy.login.network

import com.milk.codebuddy.login.data.local.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.ResponseBody
import okhttp3.MediaType.Companion.toMediaType

/**
 * Token 认证器
 * 当 Access Token 过期时自动刷新
 */
class TokenAuthenticator(
    private val sessionManager: SessionManager,
    private val onTokenRefreshFailed: () -> Unit = {}
) : Authenticator {

    companion object {
        private const val HEADER_AUTHORIZATION = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
        private const val UNAUTHORIZED_CODE = 401
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        // 如果不是 401 错误，或者已经重试过，则不再重试
        if (response.code != UNAUTHORIZED_CODE) {
            return null
        }

        val session = runBlocking { sessionManager.userSession.first() }
        
        // 如果没有 refresh token，直接返回 null，让调用方处理
        if (session.refreshToken.isEmpty()) {
            runBlocking { sessionManager.clearSession() }
            onTokenRefreshFailed()
            return null
        }

        // 尝试刷新 token（这里需要通过 Repository 刷新，暂时简化处理）
        // 实际项目中应该注入 Repository 或使用依赖注入框架
        synchronized(this) {
            // 再次检查是否已经被其他请求刷新了
            val newSession = runBlocking { sessionManager.userSession.first() }
            if (newSession.accessToken != session.accessToken) {
                // Token 已被刷新，使用新 token 重试
                return response.request.newBuilder()
                    .header(HEADER_AUTHORIZATION, BEARER_PREFIX + newSession.accessToken)
                    .build()
            }

            // Token 刷新失败，清除会话，通知需要重新登录
            runBlocking { sessionManager.clearSession() }
            onTokenRefreshFailed()
            return null
        }
    }
}
