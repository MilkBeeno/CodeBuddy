package com.milk.codebuddy.login.data.repository

import com.milk.codebuddy.login.data.local.SessionManager
import com.milk.codebuddy.login.data.model.LoginRequest
import com.milk.codebuddy.login.data.model.SendCodeRequest
import com.milk.codebuddy.login.data.model.UserSession
import com.milk.codebuddy.login.data.remote.LoginApi
import com.milk.codebuddy.login.network.NetworkException
import com.milk.codebuddy.login.network.NetworkResult
import com.milk.codebuddy.login.network.toNetworkException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * 认证仓库实现
 * 
 * 技术栈规范：
 * - 单一数据源 (SSOT)：网络请求结果必须先写入本地存储
 * - 线程调度：所有的网络请求必须强制运行在 Dispatchers.IO
 * - 异常处理：统一处理网络异常并转换为 NetworkResult
 */
class AuthRepositoryImpl(
    private val loginApi: LoginApi,
    private val sessionManager: SessionManager
) : AuthRepository {

    companion object {
        private const val SUCCESS_CODE = 200
    }

    override suspend fun sendCode(phone: String): NetworkResult<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = loginApi.sendCode(SendCodeRequest(phone))
            if (response.code == SUCCESS_CODE) {
                NetworkResult.Success(Unit)
            } else {
                NetworkResult.Error(NetworkException.BusinessError(response.code, response.message))
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.toNetworkException())
        }
    }

    override suspend fun login(phone: String, code: String): NetworkResult<UserSession> = withContext(Dispatchers.IO) {
        return@withContext try {
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
                // 网络请求结果必须先写入本地存储（SSOT）
                sessionManager.saveSession(session)
                NetworkResult.Success(session)
            } else {
                NetworkResult.Error(NetworkException.BusinessError(response.code, response.message))
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.toNetworkException())
        }
    }

    override suspend fun refreshToken(): NetworkResult<UserSession> = withContext(Dispatchers.IO) {
        return@withContext try {
            // 获取当前 refresh token
            val currentSession = sessionManager.userSession.first()
            val refreshToken = currentSession.refreshToken
            
            if (refreshToken.isEmpty()) {
                // 没有 refresh token，需要重新登录
                sessionManager.clearSession()
                return@withContext NetworkResult.Error(NetworkException.Unauthorized("No refresh token"))
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
                    userId = currentSession.userId,
                    phone = currentSession.phone,
                    nickname = currentSession.nickname,
                    avatar = currentSession.avatar,
                    isLoggedIn = true
                )
                NetworkResult.Success(session)
            } else {
                // Refresh Token 过期，需要重新登录
                sessionManager.clearSession()
                NetworkResult.Error(NetworkException.Unauthorized("Refresh token expired"))
            }
        } catch (e: Exception) {
            sessionManager.clearSession()
            NetworkResult.Error(e.toNetworkException())
        }
    }

    override suspend fun logout() {
        sessionManager.clearSession()
    }

    override fun observeUserSession(): Flow<UserSession> {
        return sessionManager.userSession
    }

    override fun observeisLoggedIn(): Flow<Boolean> {
        return sessionManager.isLoggedIn()
    }
}
