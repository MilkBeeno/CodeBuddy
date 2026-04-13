package com.milk.codebuddy.login.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.milk.codebuddy.base.network.ApiResult
import com.milk.codebuddy.base.utils.DEFAULT_COUNTDOWN_SECONDS
import com.milk.codebuddy.base.utils.countdownFlow
import com.milk.codebuddy.base.utils.isValidPhone
import com.milk.codebuddy.login.data.repository.AuthRepository
import com.milk.codebuddy.login.ui.state.RegisterEffect
import com.milk.codebuddy.login.ui.state.RegisterState
import com.milk.codebuddy.login.ui.state.RegisterUiState
import com.milk.codebuddy.resource.R as ResourceR
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 注册 ViewModel（MVI 架构）
 *
 * - 依赖通过构造器注入，由 [AuthViewModelFactory] 提供
 */
class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    companion object {
        private const val COUNTDOWN_SECONDS = DEFAULT_COUNTDOWN_SECONDS
        private const val PASSWORD_MIN_LENGTH = 6
        private const val PASSWORD_MAX_LENGTH = 20
    }

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _effect = Channel<RegisterEffect>()
    val effect = _effect.receiveAsFlow()

    fun onPhoneChange(phone: String) {
        _uiState.update {
            it.copy(phone = phone.filter { c -> c.isDigit() }.take(11), phoneError = null)
        }
    }

    fun onCodeChange(code: String) {
        _uiState.update {
            it.copy(code = code.filter { c -> c.isDigit() }.take(6), codeError = null)
        }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null) }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword, confirmPasswordError = null) }
    }

    fun onPasswordVisibilityToggle() {
        _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    fun onConfirmPasswordVisibilityToggle() {
        _uiState.update { it.copy(confirmPasswordVisible = !it.confirmPasswordVisible) }
    }

    fun onSendCodeClick() {
        val phone = _uiState.value.phone
        if (!validatePhone(phone)) {
            _uiState.update { it.copy(phoneError = ResourceR.string.register_phone_error_format) }
            return
        }
        if (_uiState.value.isCountingDown) return

        viewModelScope.launch {
            authRepository.sendCode(phone).collect { result ->
                when (result) {
                    is ApiResult.Loading -> _uiState.update { it.copy(isSendingCode = true) }
                    is ApiResult.Success -> {
                        _uiState.update { it.copy(isSendingCode = false) }
                        startCountdown()
                    }
                    is ApiResult.Error -> _uiState.update {
                        it.copy(
                            isSendingCode = false,
                            registerState = RegisterState.Error(getErrorMessageRes(result.code))
                        )
                    }
                }
            }
        }
    }

    fun onRegisterClick() {
        val phone = _uiState.value.phone
        val code = _uiState.value.code
        val password = _uiState.value.password
        val confirmPassword = _uiState.value.confirmPassword

        if (!validatePhone(phone)) {
            _uiState.update { it.copy(phoneError = ResourceR.string.register_phone_error_format) }
            return
        }
        if (code.length != 6) {
            _uiState.update { it.copy(codeError = ResourceR.string.register_code_error_length) }
            return
        }
        if (password.length < PASSWORD_MIN_LENGTH || password.length > PASSWORD_MAX_LENGTH) {
            _uiState.update { it.copy(passwordError = ResourceR.string.register_password_error_length) }
            return
        }
        if (password != confirmPassword) {
            _uiState.update { it.copy(confirmPasswordError = ResourceR.string.register_password_error_mismatch) }
            return
        }
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            authRepository.register(phone, code, password).collect { result ->
                when (result) {
                    is ApiResult.Loading -> _uiState.update {
                        it.copy(isLoading = true, registerState = RegisterState.Loading)
                    }
                    is ApiResult.Success -> {
                        _uiState.update {
                            it.copy(isLoading = false, registerState = RegisterState.Success)
                        }
                        _effect.send(RegisterEffect.ShowToast(ResourceR.string.register_success))
                        _effect.send(RegisterEffect.NavigateToLogin)
                    }
                    is ApiResult.Error -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            registerState = RegisterState.Error(getErrorMessageRes(result.code))
                        )
                    }
                }
            }
        }
    }

    private fun startCountdown() {
        viewModelScope.launch {
            _uiState.update { it.copy(countdownSeconds = COUNTDOWN_SECONDS, isCountingDown = true) }
            countdownFlow(COUNTDOWN_SECONDS).collect { remaining ->
                _uiState.update { it.copy(countdownSeconds = remaining) }
            }
            _uiState.update { it.copy(isCountingDown = false) }
        }
    }

    private fun validatePhone(phone: String): Boolean = phone.isValidPhone()

    private fun getErrorMessageRes(code: Int): Int = when (code) {
        in 500..599 -> ResourceR.string.register_error_server
        -1 -> ResourceR.string.register_error_timeout
        -2, -3 -> ResourceR.string.register_error_network
        else -> ResourceR.string.register_error_unknown
    }
}
