package com.milk.codebuddy.login.data.repository

import com.milk.codebuddy.base.network.ApiResult
import com.milk.codebuddy.login.data.model.UserSession
import kotlinx.coroutines.flow.Flow

/**
 * 认证仓库接口
 *
 * 技术栈规范：
 * - 单一数据源 (SSOT)：Repository 作为数据的唯一出口
 * - 流式处理：所有网络操作返回 Flow<ApiResult<T>>，由 ViewModel 统一订阅
 * - 严禁在 Repository 以上层级处理原始 Response
 */
interface AuthRepository {

    fun sendCode(phone: String): Flow<ApiResult<Unit>>

    fun login(phone: String, code: String): Flow<ApiResult<UserSession>>

    fun refreshToken(): Flow<ApiResult<UserSession>>

    suspend fun logout()

    fun observeUserSession(): Flow<UserSession>

    fun observeIsLoggedIn(): Flow<Boolean>

    /**
     * 手机号注册（含密码）
     * @param phone    手机号
     * @param code     验证码
     * @param password 密码
     */
    fun register(phone: String, code: String, password: String): Flow<ApiResult<Unit>>

    /**
     * 忘记密码 - 验证手机号和验证码
     * @param phone 手机号
     * @param code  验证码
     */
    fun forgotPasswordVerify(phone: String, code: String): Flow<ApiResult<Unit>>

    /**
     * 重置密码
     * @param phone           手机号
     * @param newPassword     新密码
     * @param confirmPassword 确认密码
     */
    fun resetPassword(phone: String, newPassword: String, confirmPassword: String): Flow<ApiResult<Unit>>
}

