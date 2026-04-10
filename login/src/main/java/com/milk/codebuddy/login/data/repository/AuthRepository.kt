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

    /**
     * 发送验证码
     * @param phone 手机号
     */
    fun sendCode(phone: String): Flow<ApiResult<Unit>>

    /**
     * 手机号验证码登录
     * @param phone 手机号
     * @param code  验证码
     */
    fun login(phone: String, code: String): Flow<ApiResult<UserSession>>

    /**
     * 刷新 Token
     */
    fun refreshToken(): Flow<ApiResult<UserSession>>

    /**
     * 登出：清除本地会话
     */
    suspend fun logout()

    /**
     * 观察当前用户会话（响应式，持续监听）
     */
    fun observeUserSession(): Flow<UserSession>

    /**
     * 观察登录状态（响应式，持续监听）
     */
    fun observeIsLoggedIn(): Flow<Boolean>
}
