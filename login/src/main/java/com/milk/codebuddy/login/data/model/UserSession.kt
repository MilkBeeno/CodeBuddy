package com.milk.codebuddy.login.data.model

/**
 * 用户会话数据类
 * 用于存储用户的登录状态和 Token 信息
 */
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
