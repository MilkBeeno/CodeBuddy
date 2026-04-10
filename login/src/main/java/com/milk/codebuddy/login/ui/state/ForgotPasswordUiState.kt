package com.milk.codebuddy.login.ui.state

import androidx.compose.runtime.Immutable

/**
 * 忘记密码 UI 状态（MVI 模式）
 */
@Immutable
data class ForgotPasswordUiState(
    val phone: String = "",
    val code: String = "",

    val phoneError: Int? = null,
    val codeError: Int? = null,

    val countdownSeconds: Int = 0,
    val isCountingDown: Boolean = false,

    val isLoading: Boolean = false,
    val isSendingCode: Boolean = false,

    val forgotPasswordState: ForgotPasswordState = ForgotPasswordState.Idle
) {
    val canSendCode: Boolean
        get() = phone.length == 11 && !isCountingDown && !isSendingCode

    val canNext: Boolean
        get() = phone.length == 11 && code.length == 6 && !isLoading
}

@Immutable
sealed class ForgotPasswordState {
    data object Idle : ForgotPasswordState()
    data object Loading : ForgotPasswordState()
    data object Success : ForgotPasswordState()
    data class Error(val messageResId: Int) : ForgotPasswordState()
}

@Immutable
sealed class ForgotPasswordEffect {
    data class ShowToast(val messageResId: Int) : ForgotPasswordEffect()
    data class NavigateToResetPassword(val phone: String) : ForgotPasswordEffect()
    data object NavigateBack : ForgotPasswordEffect()
}
