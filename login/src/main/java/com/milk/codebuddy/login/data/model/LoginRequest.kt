package com.milk.codebuddy.login.data.model

import com.google.gson.annotations.SerializedName

/**
 * 登录请求
 */
data class LoginRequest(
    @SerializedName("phone")
    val phone: String,
    @SerializedName("code")
    val code: String
)

/**
 * 发送验证码请求
 */
data class SendCodeRequest(
    @SerializedName("phone")
    val phone: String
)
