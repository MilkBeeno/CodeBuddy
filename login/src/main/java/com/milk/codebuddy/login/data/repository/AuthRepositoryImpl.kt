package com.milk.codebuddy.login.data.repository

import com.milk.codebuddy.base.network.ApiResult
import com.milk.codebuddy.base.network.safeApiCall
import com.milk.codebuddy.login.data.local.SessionManager
import com.milk.codebuddy.login.data.model.LoginRequest
import com.milk.codebuddy.login.data.model.SendCodeRequest
import com.milk.codebuddy.login.data.model.UserSession
import com.milk.codebuddy.login.data.remote.LoginApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * 认证仓库实现
 *
 * 技术栈规范：
 * - 使用 base 模块的 safeApiCall 统一封装请求，不再手写 try-catch
 * - 线程调度：safeApiCall 内部已通过 flowOn(Dispatchers.IO) 保证 IO 线程执行
 * - 数据模型分离：在 Repository 层完成 DTO → DomainModel 的 map 转换
 * - 业务状态码：code != 200 时抛出 RuntimeException，由 safeApiCall 的 catch 捕获
 * - SSOT：登录成功后先写入 SessionManager，再向上发射成功结果
 */
class AuthRepositoryImpl(
    private val loginApi: LoginApi,
    private val sessionManager: SessionManager
) : AuthRepository {

    companion object {
        private const val SUCCESS_CODE = 200
    }

    override fun sendCode(phone: String): Flow<ApiResult<Unit>> = safeApiCall {
        val response = loginApi.sendCode(SendCodeRequest(phone))
        if (response.code != SUCCESS_CODE) {
            throw RuntimeException(response.message)
        }
    }

    override fun login(phone: String, code: String): Flow<ApiResult<UserSession>> = safeApiCall {
        val response = loginApi.login(LoginRequest(phone, code))
        if (response.code != SUCCESS_CODE || response.data == null) {
            throw RuntimeException(response.message)
        }

        val tokenData = response.data
        val userInfo = tokenData.userInfo
        val session = UserSession(
            accessToken = tokenData.accessToken,
            refreshToken = tokenData.refreshToken,
            userId = userInfo?.userId.orEmpty(),
            phone = userInfo?.phone ?: phone,
            nickname = userInfo?.nickname.orEmpty(),
            avatar = userInfo?.avatar.orEmpty(),
            isLoggedIn = true
        )
        // SSOT：网络结果先写入本地存储
        sessionManager.saveSession(session)
        session
    }

    override fun refreshToken(): Flow<ApiResult<UserSession>> = safeApiCall {
        val currentSession = sessionManager.userSession.first()

        if (currentSession.refreshToken.isEmpty()) {
            sessionManager.clearSession()
            throw RuntimeException("No refresh token, please login again")
        }

        val response = loginApi.refreshToken(mapOf("refresh_token" to currentSession.refreshToken))
        if (response.code != SUCCESS_CODE || response.data == null) {
            sessionManager.clearSession()
            throw RuntimeException(response.message)
        }

        val tokenData = response.data
        sessionManager.updateTokens(tokenData.accessToken, tokenData.refreshToken)

        currentSession.copy(
            accessToken = tokenData.accessToken,
            refreshToken = tokenData.refreshToken
        )
    }

    override suspend fun logout() {
        sessionManager.clearSession()
    }

    override fun observeUserSession(): Flow<UserSession> = sessionManager.userSession

    override fun observeIsLoggedIn(): Flow<Boolean> = sessionManager.isLoggedIn()
}
