package com.milk.codebuddy.login.data.model

import androidx.compose.runtime.Immutable
import com.google.gson.annotations.SerializedName

/**
 * 基础响应结构
 * 必须识别后端通用的 BaseResponse<T> 结构（如 {code, data, message}）
 * 
 * 技术栈规范：
 * - 业务状态码：必须识别后端通用的 BaseResponse<T> 结构，并将 code != 200 转换为自定义异常
 * - 空安全：对于后端可能返回的 null 字段，必须在 Data Class 中定义为可空类型
 * - 稳定类型：对于复杂 Data Class，建议使用 @Immutable 或 @Stable 标注
 */
@Immutable
data class BaseResponse<T>(
    @SerializedName("code")
    val code: Int,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: T?
) {
    companion object {
        const val SUCCESS_CODE = 200
    }
    
    /**
     * 检查是否成功
     */
    val isSuccess: Boolean
        get() = code == SUCCESS_CODE
    
    /**
     * 获取数据或抛出异常
     */
    fun getDataOrThrow(): T {
        if (isSuccess && data != null) {
            return data
        }
        throw RuntimeException(message)
    }
}
