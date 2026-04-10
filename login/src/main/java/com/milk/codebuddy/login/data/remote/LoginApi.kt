package com.milk.codebuddy.login.data.remote

import com.milk.codebuddy.login.data.model.ForgotPasswordVerifyRequest
import com.milk.codebuddy.login.data.model.LoginRequest
import com.milk.codebuddy.login.data.model.LoginResponse
import com.milk.codebuddy.login.data.model.RegisterRequest
import com.milk.codebuddy.login.data.model.ResetPasswordRequest
import com.milk.codebuddy.login.data.model.SendCodeRequest
import com.milk.codebuddy.login.data.model.SendCodeResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * 登录/注册 API 接口
 *
 * 技术栈规范：
 * - 声明式接口：所有 Service 接口使用 suspend 关键字
 * - 常量命名规范：URL 端点使用 SCREAMING_SNAKE_CASE
 */
interface LoginApi {

    companion object {
        const val ENDPOINT_SEND_CODE = "api/v1/auth/send-code"
        const val ENDPOINT_LOGIN = "api/v1/auth/login"
        const val ENDPOINT_REFRESH_TOKEN = "api/v1/auth/refresh-token"
        const val ENDPOINT_REGISTER = "api/v1/auth/register"
        const val ENDPOINT_FORGOT_PASSWORD_VERIFY = "api/v1/auth/forgot-password/verify"
        const val ENDPOINT_RESET_PASSWORD = "api/v1/auth/reset-password"
    }

    @POST(ENDPOINT_SEND_CODE)
    suspend fun sendCode(@Body request: SendCodeRequest): SendCodeResponse

    @POST(ENDPOINT_LOGIN)
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST(ENDPOINT_REFRESH_TOKEN)
    suspend fun refreshToken(@Body refreshToken: Map<String, String>): LoginResponse

    @POST(ENDPOINT_REGISTER)
    suspend fun register(@Body request: RegisterRequest): SendCodeResponse

    /**
     * 忘记密码 - 验证手机号和验证码（返回重置 Token）
     */
    @POST(ENDPOINT_FORGOT_PASSWORD_VERIFY)
    suspend fun forgotPasswordVerify(@Body request: ForgotPasswordVerifyRequest): SendCodeResponse

    /**
     * 重置密码
     */
    @POST(ENDPOINT_RESET_PASSWORD)
    suspend fun resetPassword(@Body request: ResetPasswordRequest): SendCodeResponse
}

