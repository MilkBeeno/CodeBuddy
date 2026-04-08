package com.milk.codebuddy.login.data.model

import androidx.compose.runtime.Immutable

/**
 * 用户会话数据类
 * 用于存储用户的登录状态和 Token 信息
 * 
 * 技术栈规范：
 * - 稳定类型：对于复杂 Data Class，建议使用 @Immutable 或 @Stable 标注
 */
@Immutable
data class UserSession(
    val accessToken: String = "",
    val refreshToken: String = "",
    val userId: String = "",
    val phone: String = "",
    val nickname: String = "",
    val avatar: String = "",
    val isLoggedIn: Boolean = false
) {
    companion object {
        val EMPTY = UserSession()
    }
}
