package com.milk.codebuddy.login.ui.state

import androidx.compose.runtime.Immutable

/**
 * 注册 UI 状态（MVI 模式）
 */
@Immutable
data class RegisterUiState(
    val phone: String = "",
    val code: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val passwordVisible: Boolean = false,
    val confirmPasswordVisible: Boolean = false,

    val phoneError: Int? = null,
    val codeError: Int? = null,
    val passwordError: Int? = null,
    val confirmPasswordError: Int? = null,

    val countdownSeconds: Int = 0,
    val isCountingDown: Boolean = false,

    val isLoading: Boolean = false,
    val isSendingCode: Boolean = false,

    val registerState: RegisterState = RegisterState.Idle
) {
    val canSendCode: Boolean
        get() = phone.length == 11 && !isCountingDown && !isSendingCode

    val canRegister: Boolean
        get() = phone.length == 11 && code.length == 6 &&
                password.length in 6..20 && confirmPassword.isNotEmpty() && !isLoading
}

@Immutable
sealed class RegisterState {
    data object Idle : RegisterState()
    data object Loading : RegisterState()
    data object Success : RegisterState()
    data class Error(val messageResId: Int) : RegisterState()
}

@Immutable
sealed class RegisterEffect {
    data class ShowToast(val messageResId: Int) : RegisterEffect()
    data object NavigateToLogin : RegisterEffect()
}
