package com.milk.codebuddy.login.network

import androidx.compose.runtime.Immutable

/**
 * 网络异常类型
 * 统一处理 401（未授权）、403（禁止访问）和网络超时等异常
 */
@Immutable
sealed class NetworkException : Exception() {
    /**
     * 未授权（401）- Token 过期或无效
     */
    data class Unauthorized(override val message: String = "Unauthorized") : NetworkException()

    /**
     * 禁止访问（403）
     */
    data class Forbidden(override val message: String = "Forbidden") : NetworkException()

    /**
     * 资源未找到（404）
     */
    data class NotFound(override val message: String = "Resource not found") : NetworkException()

    /**
     * 网络超时
     */
    data class Timeout(override val message: String = "Network timeout") : NetworkException()

    /**
     * 网络连接错误
     */
    data class ConnectionError(override val message: String = "Network connection error") : NetworkException()

    /**
     * 服务器错误
     */
    data class ServerError(val code: Int, override val message: String = "Server error") : NetworkException()

    /**
     * 业务错误（后端返回的错误）
     */
    data class BusinessError(val code: Int, override val message: String) : NetworkException()

    /**
     * 未知错误
     */
    data class Unknown(override val message: String = "Unknown error", override val cause: Throwable? = null) : NetworkException()
}

/**
 * 将异常转换为 NetworkException
 * 必须识别后端通用的 BaseResponse<T> 结构，并将 code != 200 转换为自定义异常
 */
fun Throwable.toNetworkException(): NetworkException {
    return when (this) {
        is NetworkException -> this
        is java.net.SocketTimeoutException -> NetworkException.Timeout()
        is java.net.UnknownHostException -> NetworkException.ConnectionError()
        is java.net.ConnectException -> NetworkException.ConnectionError()
        else -> NetworkException.Unknown(message = message ?: "Unknown error", cause = this)
    }
}

/**
 * 将 HTTP 状态码转换为 NetworkException
 */
fun Int.toNetworkException(message: String = "HTTP Error"): NetworkException {
    return when (this) {
        401 -> NetworkException.Unauthorized(message)
        403 -> NetworkException.Forbidden(message)
        404 -> NetworkException.NotFound(message)
        in 500..599 -> NetworkException.ServerError(this, message)
        else -> NetworkException.Unknown(message)
    }
}
