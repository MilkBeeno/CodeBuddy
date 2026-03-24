package com.milk.codebuddy.login.network

import androidx.compose.runtime.Immutable

/**
 * 网络请求结果密封类
 * 统一包装网络请求结果，通过 Flow 逐层向上传递至 UI 层
 */
@Immutable
sealed class NetworkResult<out T> {
    /**
     * 加载中
     */
    data object Loading : NetworkResult<Nothing>()
    
    /**
     * 成功
     */
    data class Success<T>(val data: T) : NetworkResult<T>()
    
    /**
     * 失败
     */
    data class Error(val exception: NetworkException) : NetworkResult<Nothing>()
    
    /**
     * 检查是否成功
     */
    val isSuccess: Boolean
        get() = this is Success
    
    /**
     * 检查是否加载中
     */
    val isLoading: Boolean
        get() = this is Loading
    
    /**
     * 检查是否失败
     */
    val isError: Boolean
        get() = this is Error
    
    /**
     * 获取数据（仅 Success 时有效）
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }
    
    /**
     * 获取错误（仅 Error 时有效）
     */
    fun exceptionOrNull(): NetworkException? = when (this) {
        is Error -> exception
        else -> null
    }
    
    /**
     * 映射数据
     */
    inline fun <R> map(transform: (T) -> R): NetworkResult<R> = when (this) {
        is Loading -> Loading
        is Success -> Success(transform(data))
        is Error -> this
    }
    
    /**
     * 成功时执行
     */
    inline fun onSuccess(action: (T) -> Unit): NetworkResult<T> {
        if (this is Success) {
            action(data)
        }
        return this
    }
    
    /**
     * 失败时执行
     */
    inline fun onError(action: (NetworkException) -> Unit): NetworkResult<T> {
        if (this is Error) {
            action(exception)
        }
        return this
    }
    
    /**
     * 加载时执行
     */
    inline fun onLoading(action: () -> Unit): NetworkResult<T> {
        if (this is Loading) {
            action()
        }
        return this
    }
}
