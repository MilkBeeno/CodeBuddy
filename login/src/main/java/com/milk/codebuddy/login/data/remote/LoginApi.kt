package com.milk.codebuddy.login.data.remote

import com.milk.codebuddy.login.data.model.LoginRequest
import com.milk.codebuddy.login.data.model.LoginResponse
import com.milk.codebuddy.login.data.model.SendCodeRequest
import com.milk.codebuddy.login.data.model.SendCodeResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * 登录 API 接口
 * 
 * 技术栈规范：
 * - 声明式接口：所有 Service 接口使用 suspend 关键字
 * - 常量命名规范：URL 端点使用 SCREAMING_SNAKE_CASE
 */
interface LoginApi {

    companion object {
        // API 端点常量
        const val ENDPOINT_SEND_CODE = "api/v1/auth/send-code"
        const val ENDPOINT_LOGIN = "api/v1/auth/login"
        const val ENDPOINT_REFRESH_TOKEN = "api/v1/auth/refresh-token"
    }

    /**
     * 发送验证码
     */
    @POST(ENDPOINT_SEND_CODE)
    suspend fun sendCode(@Body request: SendCodeRequest): SendCodeResponse

    /**
     * 手机号验证码登录
     */
    @POST(ENDPOINT_LOGIN)
    suspend fun login(@Body request: LoginRequest): LoginResponse

    /**
     * 刷新 Token
     */
    @POST(ENDPOINT_REFRESH_TOKEN)
    suspend fun refreshToken(@Body refreshToken: Map<String, String>): LoginResponse
}
