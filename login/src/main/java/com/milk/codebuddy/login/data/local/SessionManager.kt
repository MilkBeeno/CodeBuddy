package com.milk.codebuddy.login.data.local

import com.milk.codebuddy.base.datastore.AppPreferences
import com.milk.codebuddy.base.datastore.AppPreferencesKeys
import com.milk.codebuddy.login.data.model.UserSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/**
 * 会话管理器
 *
 * 基于 base 模块的 [AppPreferences] 实现，提供用户会话的读写能力。
 * 所有 Key 统一由 [AppPreferencesKeys] 管理，禁止在此处定义新 Key。
 *
 * 规范：
 * - 禁止在业务模块直接访问 context.dataStore
 * - 读写操作委托给 [AppPreferences]，不重复实现
 *
 * @param prefs base 模块的 [AppPreferences] 单例
 */
class SessionManager(private val prefs: AppPreferences) {

    /**
     * 用户会话流
     * 读取失败（IOException）时由 [AppPreferences] 内部处理，返回空会话
     */
    val userSession: Flow<UserSession> = combine(
        prefs.observe(AppPreferencesKeys.ACCESS_TOKEN, ""),
        prefs.observe(AppPreferencesKeys.REFRESH_TOKEN, ""),
        prefs.observe(AppPreferencesKeys.USER_ID, ""),
        prefs.observe(AppPreferencesKeys.USER_PHONE, ""),
        prefs.observe(AppPreferencesKeys.USER_NICKNAME, ""),
        prefs.observe(AppPreferencesKeys.USER_AVATAR, "")
    ) { values ->
        val accessToken = values[0]
        val refreshToken = values[1]
        val userId = values[2]
        val phone = values[3]
        val nickname = values[4]
        val avatar = values[5]
        if (accessToken.isNotEmpty()) {
            UserSession(
                accessToken = accessToken,
                refreshToken = refreshToken,
                userId = userId,
                phone = phone,
                nickname = nickname,
                avatar = avatar,
                isLoggedIn = true
            )
        } else {
            UserSession.EMPTY
        }
    }

    /**
     * 是否已登录的流
     */
    fun isLoggedIn(): Flow<Boolean> = userSession.map { it.isLoggedIn }

    /**
     * 保存完整会话信息
     *
     * @param session 用户会话
     */
    suspend fun saveSession(session: UserSession) = prefs.saveSession(
        access = session.accessToken,
        refresh = session.refreshToken,
        userId = session.userId,
        phone = session.phone,
        nickname = session.nickname,
        avatar = session.avatar
    )

    /**
     * 仅更新 Token（Token 刷新时使用）
     *
     * @param accessToken  新 AccessToken
     * @param refreshToken 新 RefreshToken
     */
    suspend fun updateTokens(accessToken: String, refreshToken: String) =
        prefs.updateTokens(accessToken, refreshToken)

    /**
     * 清除会话（退出登录）
     * 全量清理敏感信息，确保退出后无数据残留
     */
    suspend fun clearSession() = prefs.clearSession()
}
