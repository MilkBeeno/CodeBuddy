package com.milk.codebuddy.login.data.repository

import com.milk.codebuddy.login.data.model.UserSession
import kotlinx.coroutines.flow.Flow

/**
 * 认证仓库接口
 */
interface AuthRepository {
    /**
     * 发送验证码
     * @param phone 手机号
     * @return 发送结果
     */
    suspend fun sendCode(phone: String): Result<Unit>

    /**
     * 手机号验证码登录
     * @param phone 手机号
     * @param code 验证码
     * @return 登录结果
     */
    suspend fun login(phone: String, code: String): Result<UserSession>

    /**
     * 刷新 Token
     * @return 刷新结果
     */
    suspend fun refreshToken(): Result<UserSession>

    /**
     * 登出
     */
    suspend fun logout()

    /**
     * 获取当前用户会话
     */
    fun getUserSession(): Flow<UserSession>

    /**
     * 检查是否已登录
     */
    fun isLoggedIn(): Flow<Boolean>
}
