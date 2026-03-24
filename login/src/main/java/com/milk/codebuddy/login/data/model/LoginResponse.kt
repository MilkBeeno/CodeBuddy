package com.milk.codebuddy.login.data.model

import com.google.gson.annotations.SerializedName

/**
 * 登录响应
 */
data class LoginResponse(
    @SerializedName("code")
    val code: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: TokenData?
)

/**
 * Token 数据
 */
data class TokenData(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("refresh_token")
    val refreshToken: String,
    @SerializedName("user_info")
    val userInfo: UserInfo?
)

/**
 * 用户信息
 */
data class UserInfo(
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("nickname")
    val nickname: String?,
    @SerializedName("avatar")
    val avatar: String?
)

/**
 * 发送验证码响应
 */
data class SendCodeResponse(
    @SerializedName("code")
    val code: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: Boolean?
)
