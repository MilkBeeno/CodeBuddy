package com.milk.codebuddy.login.data.remote

import com.milk.codebuddy.login.data.model.LoginRequest
import com.milk.codebuddy.login.data.model.LoginResponse
import com.milk.codebuddy.login.data.model.SendCodeRequest
import com.milk.codebuddy.login.data.model.SendCodeResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * 登录 API 接口
 */
interface LoginApi {
    /**
     * 发送验证码
     */
    @POST("api/v1/auth/send-code")
    suspend fun sendCode(@Body request: SendCodeRequest): SendCodeResponse

    /**
     * 手机号验证码登录
     */
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    /**
     * 刷新 Token
     */
    @POST("api/v1/auth/refresh-token")
    suspend fun refreshToken(@Body refreshToken: Map<String, String>): LoginResponse
}
