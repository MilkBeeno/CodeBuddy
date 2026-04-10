package com.milk.codebuddy.base.network

/**
 * 网络请求结果统一封装密封类
 *
 * 使用规范：
 * - Repository 层必须将所有网络响应包装为此密封类，严禁 try-catch 散落在 ViewModel
 * - UI 层通过 collect 观察 Flow<ApiResult<T>> 并按状态渲染界面
 *
 * 示例用法：
 * ```
 * viewModel.uiState.collect { result ->
 *     when (result) {
 *         is ApiResult.Loading -> showLoading()
 *         is ApiResult.Success -> render(result.data)
 *         is ApiResult.Error   -> showError(result.message)
 *     }
 * }
 * ```
 */
sealed interface ApiResult<out T> {

    /** 请求成功，携带业务数据 */
    data class Success<T>(val data: T) : ApiResult<T>

    /** 请求失败，携带错误码、描述及可选原始异常 */
    data class Error(
        val code: Int,
        val message: String,
        val throwable: Throwable? = null
    ) : ApiResult<Nothing>

    /** 请求进行中 */
    data object Loading : ApiResult<Nothing>
}
