package com.milk.codebuddy.login.ui.state

import androidx.compose.runtime.Immutable

/**
 * 重置密码 UI 状态（MVI 模式）
 */
@Immutable
data class ResetPasswordUiState(
    val phone: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val newPasswordVisible: Boolean = false,
    val confirmPasswordVisible: Boolean = false,

    val newPasswordError: Int? = null,
    val confirmPasswordError: Int? = null,

    val isLoading: Boolean = false,

    val resetPasswordState: ResetPasswordState = ResetPasswordState.Idle
) {
    val canSubmit: Boolean
        get() = newPassword.length in 6..20 && confirmPassword.isNotEmpty() && !isLoading
}

@Immutable
sealed class ResetPasswordState {
    data object Idle : ResetPasswordState()
    data object Loading : ResetPasswordState()
    data object Success : ResetPasswordState()
    data class Error(val messageResId: Int) : ResetPasswordState()
}

@Immutable
sealed class ResetPasswordEffect {
    data class ShowToast(val messageResId: Int) : ResetPasswordEffect()
    data object NavigateToLogin : ResetPasswordEffect()
    data object NavigateBack : ResetPasswordEffect()
}
