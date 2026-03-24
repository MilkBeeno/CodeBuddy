package com.milk.codebuddy.login.data.repository

import com.milk.codebuddy.login.data.local.SessionManager
import com.milk.codebuddy.login.data.model.LoginRequest
import com.milk.codebuddy.login.data.model.SendCodeRequest
import com.milk.codebuddy.login.data.model.UserSession
import com.milk.codebuddy.login.data.remote.LoginApi
import kotlinx.coroutines.flow.Flow

/**
 * 认证仓库实现
 */
class AuthRepositoryImpl(
    private val loginApi: LoginApi,
    private val sessionManager: SessionManager
) : AuthRepository {

    companion object {
        private const val SUCCESS_CODE = 200
    }

    override suspend fun sendCode(phone: String): Result<Unit> {
        return try {
            val response = loginApi.sendCode(SendCodeRequest(phone))
            if (response.code == SUCCESS_CODE) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(phone: String, code: String): Result<UserSession> {
        return try {
            val response = loginApi.login(LoginRequest(phone, code))
            if (response.code == SUCCESS_CODE && response.data != null) {
                val tokenData = response.data
                val userInfo = tokenData.userInfo
                val session = UserSession(
                    accessToken = tokenData.accessToken,
                    refreshToken = tokenData.refreshToken,
                    userId = userInfo?.userId ?: "",
                    phone = userInfo?.phone ?: phone,
                    nickname = userInfo?.nickname ?: "",
                    avatar = userInfo?.avatar ?: "",
                    isLoggedIn = true
                )
                sessionManager.saveSession(session)
                Result.success(session)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refreshToken(): Result<UserSession> {
        return try {
            val currentSession = sessionManager.userSession
            // 获取当前 refresh token
            var refreshToken = ""
            currentSession.collect { session ->
                refreshToken = session.refreshToken
            }
            
            val response = loginApi.refreshToken(mapOf("refresh_token" to refreshToken))
            if (response.code == SUCCESS_CODE && response.data != null) {
                val tokenData = response.data
                sessionManager.updateTokens(
                    tokenData.accessToken,
                    tokenData.refreshToken
                )
                val session = UserSession(
                    accessToken = tokenData.accessToken,
                    refreshToken = tokenData.refreshToken,
                    isLoggedIn = true
                )
                Result.success(session)
            } else {
                // Refresh Token 过期，需要重新登录
                sessionManager.clearSession()
                Result.failure(Exception("Refresh token expired"))
            }
        } catch (e: Exception) {
            sessionManager.clearSession()
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        sessionManager.clearSession()
    }

    override fun getUserSession(): Flow<UserSession> {
        return sessionManager.userSession
    }

    override fun isLoggedIn(): Flow<Boolean> {
        return sessionManager.isLoggedIn()
    }
}
