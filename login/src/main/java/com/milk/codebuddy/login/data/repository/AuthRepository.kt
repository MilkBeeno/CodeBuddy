package com.milk.codebuddy.login.data.repository

import com.milk.codebuddy.login.data.model.UserSession
import com.milk.codebuddy.login.network.NetworkResult
import kotlinx.coroutines.flow.Flow

/**
 * 认证仓库接口
 * 
 * 技术栈规范：
 * - 单一数据源 (SSOT)：Repository 必须作为数据的唯一出口
 * - 响应式查询：DAO 的返回值必须使用 Flow
 */
interface AuthRepository {
    /**
     * 发送验证码
     * @param phone 手机号
     * @return 网络请求结果
     */
    suspend fun sendCode(phone: String): NetworkResult<Unit>

    /**
     * 手机号验证码登录
     * @param phone 手机号
     * @param code 验证码
     * @return 网络请求结果
     */
    suspend fun login(phone: String, code: String): NetworkResult<UserSession>

    /**
     * 刷新 Token
     * @return 网络请求结果
     */
    suspend fun refreshToken(): NetworkResult<UserSession>

    /**
     * 登出
     */
    suspend fun logout()

    /**
     * 获取当前用户会话
     * UI 仅观察来自 Repository 的 Flow
     */
    fun observeUserSession(): Flow<UserSession>

    /**
     * 检查是否已登录
     */
    fun observeisLoggedIn(): Flow<Boolean>
}
