package com.milk.codebuddy.login.ui.state

/**
 * 登录 UI 状态（MVI 模式）
 */
data class LoginUiState(
    // 输入状态
    val phone: String = "",
    val code: String = "",
    
    // 错误状态
    val phoneError: Int? = null,  // 使用字符串资源 ID
    val codeError: Int? = null,   // 使用字符串资源 ID
    
    // 验证码倒计时
    val countdownSeconds: Int = 0,
    val isCountingDown: Boolean = false,
    
    // 加载状态
    val isLoading: Boolean = false,
    val isSendingCode: Boolean = false,
    
    // 登录状态
    val loginState: LoginState = LoginState.Idle
)

/**
 * 登录状态
 */
sealed class LoginState {
    /**
     * 空闲状态
     */
    data object Idle : LoginState()
    
    /**
     * 加载中
     */
    data object Loading : LoginState()
    
    /**
     * 需要验证码
     */
    data object NeedOTP : LoginState()
    
    /**
     * 登录成功
     */
    data object Success : LoginState()
    
    /**
     * 登录失败
     */
    data class Error(val messageResId: Int) : LoginState()
}
