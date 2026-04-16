package com.milk.codebuddy.login.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.milk.codebuddy.base.network.ApiResult
import com.milk.codebuddy.base.ui.navigation.ResetPassword
import com.milk.codebuddy.login.data.repository.AuthRepository
import com.milk.codebuddy.login.ui.state.ResetPasswordEffect
import com.milk.codebuddy.login.ui.state.ResetPasswordState
import com.milk.codebuddy.login.ui.state.ResetPasswordUiState
import com.milk.codebuddy.resource.R as ResourceR
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 重置密码 ViewModel（MVI 架构）
 *
 * - 依赖通过 Hilt 注入
 * - 通过 [SavedStateHandle] 获取导航传入的 phone 参数，无需持有 Application / Context
 *
 * @param authRepository  认证仓库，统一处理网络请求
 * @param savedStateHandle 导航参数持有者，从 [ResetPassword] 路由中提取 phone
 */
@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val PASSWORD_MIN_LENGTH = 6
        private const val PASSWORD_MAX_LENGTH = 20
    }

    private val phone: String = savedStateHandle.toRoute<ResetPassword>().phone

    private val _uiState = MutableStateFlow(ResetPasswordUiState(phone = phone))
    val uiState: StateFlow<ResetPasswordUiState> = _uiState.asStateFlow()

    private val _effect = Channel<ResetPasswordEffect>()
    val effect = _effect.receiveAsFlow()

    fun onNewPasswordChange(password: String) {
        _uiState.update { it.copy(newPassword = password, newPasswordError = null) }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword, confirmPasswordError = null) }
    }

    fun onNewPasswordVisibilityToggle() {
        _uiState.update { it.copy(newPasswordVisible = !it.newPasswordVisible) }
    }

    fun onConfirmPasswordVisibilityToggle() {
        _uiState.update { it.copy(confirmPasswordVisible = !it.confirmPasswordVisible) }
    }

    fun onBackClick() {
        viewModelScope.launch {
            _effect.send(ResetPasswordEffect.NavigateBack)
        }
    }

    fun onSubmitClick() {
        val newPassword = _uiState.value.newPassword
        val confirmPassword = _uiState.value.confirmPassword

        if (newPassword.length < PASSWORD_MIN_LENGTH || newPassword.length > PASSWORD_MAX_LENGTH) {
            _uiState.update { it.copy(newPasswordError = ResourceR.string.reset_password_error_length) }
            return
        }
        if (newPassword != confirmPassword) {
            _uiState.update { it.copy(confirmPasswordError = ResourceR.string.reset_password_error_mismatch) }
            return
        }
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            authRepository.resetPassword(phone, newPassword, confirmPassword).collect { result ->
                when (result) {
                    is ApiResult.Loading -> _uiState.update {
                        it.copy(isLoading = true, resetPasswordState = ResetPasswordState.Loading)
                    }
                    is ApiResult.Success -> {
                        _uiState.update {
                            it.copy(isLoading = false, resetPasswordState = ResetPasswordState.Success)
                        }
                        _effect.send(ResetPasswordEffect.ShowToast(ResourceR.string.reset_password_success))
                        _effect.send(ResetPasswordEffect.NavigateToLogin)
                    }
                    is ApiResult.Error -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            resetPasswordState = ResetPasswordState.Error(getErrorMessageRes(result.code))
                        )
                    }
                }
            }
        }
    }

    private fun getErrorMessageRes(code: Int): Int = when (code) {
        in 500..599 -> ResourceR.string.reset_password_error_server
        -1 -> ResourceR.string.reset_password_error_timeout
        -2, -3 -> ResourceR.string.reset_password_error_network
        else -> ResourceR.string.reset_password_error_unknown
    }
}
