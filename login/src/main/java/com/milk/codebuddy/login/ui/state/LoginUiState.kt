package com.milk.codebuddy.login.ui.state

import androidx.compose.runtime.Immutable

/**
 * 登录 UI 状态（MVI 模式）
 * 使用 @Immutable 标注以优化 Compose 重组性能
 */
@Immutable
data class LoginUiState(
    // 输入状态
    val phone: String = "",
    val code: String = "",
    
    // 错误状态（使用字符串资源 ID）
    val phoneError: Int? = null,
    val codeError: Int? = null,
    
    // 验证码倒计时
    val countdownSeconds: Int = 0,
    val isCountingDown: Boolean = false,
    
    // 加载状态
    val isLoading: Boolean = false,
    val isSendingCode: Boolean = false,
    
    // 登录状态
    val loginState: LoginState = LoginState.Idle
) {
    /**
     * 检查是否可以发送验证码
     */
    val canSendCode: Boolean
        get() = phone.length == 11 && !isCountingDown && !isSendingCode
    
    /**
     * 检查是否可以登录
     */
    val canLogin: Boolean
        get() = phone.length == 11 && code.length == 6 && !isLoading
}

/**
 * 登录状态
 * 使用密封类确保类型安全的状态管理
 */
@Immutable
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

/**
 * UI 副作用（单次事件）
 * 用于处理 Toast、导航等单次事件
 */
@Immutable
sealed class LoginEffect {
    /**
     * 显示 Toast
     */
    data class ShowToast(val messageResId: Int) : LoginEffect()
    
    /**
     * 导航到主页
     */
    data object NavigateToMain : LoginEffect()
    
    /**
     * 导航到登录页
     */
    data object NavigateToLogin : LoginEffect()
}
