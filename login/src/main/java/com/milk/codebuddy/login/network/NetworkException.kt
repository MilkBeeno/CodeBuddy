package com.milk.codebuddy.login.network

/**
 * 网络异常类型
 */
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
     * 未知错误
     */
    data class Unknown(override val message: String = "Unknown error", override val cause: Throwable? = null) : NetworkException()
}

/**
 * 将异常转换为 NetworkException
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
