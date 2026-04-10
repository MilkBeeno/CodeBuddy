package com.milk.codebuddy.login.data.model

import com.google.gson.annotations.SerializedName

/**
 * 注册请求
 */
data class RegisterRequest(
    @SerializedName("phone")
    val phone: String,
    @SerializedName("code")
    val code: String,
    @SerializedName("password")
    val password: String
)

/**
 * 忘记密码 - 验证手机号请求（复用 SendCodeRequest）
 */
data class ForgotPasswordVerifyRequest(
    @SerializedName("phone")
    val phone: String,
    @SerializedName("code")
    val code: String
)

/**
 * 重置密码请求
 */
data class ResetPasswordRequest(
    @SerializedName("phone")
    val phone: String,
    @SerializedName("new_password")
    val newPassword: String,
    @SerializedName("confirm_password")
    val confirmPassword: String
)
