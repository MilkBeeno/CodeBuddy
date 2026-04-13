package com.milk.codebuddy.login.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.milk.codebuddy.base.network.ApiResult
import com.milk.codebuddy.base.utils.DEFAULT_COUNTDOWN_SECONDS
import com.milk.codebuddy.base.utils.countdownFlow
import com.milk.codebuddy.base.utils.isValidPhone
import com.milk.codebuddy.login.data.repository.AuthRepository
import com.milk.codebuddy.login.ui.state.ForgotPasswordEffect
import com.milk.codebuddy.login.ui.state.ForgotPasswordState
import com.milk.codebuddy.login.ui.state.ForgotPasswordUiState
import com.milk.codebuddy.resource.R as ResourceR
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 忘记密码 ViewModel（MVI 架构）
 * 流程：输入手机号 → 发送验证码 → 验证通过 → 跳转重置密码页
 *
 * - 依赖通过构造器注入，由 [AuthViewModelFactory] 提供
 */
class ForgotPasswordViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    companion object {
        private const val COUNTDOWN_SECONDS = DEFAULT_COUNTDOWN_SECONDS
    }

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    private val _effect = Channel<ForgotPasswordEffect>()
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

    fun onSendCodeClick() {
        val phone = _uiState.value.phone
        if (!validatePhone(phone)) {
            _uiState.update { it.copy(phoneError = ResourceR.string.forgot_password_phone_error_format) }
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
                            forgotPasswordState = ForgotPasswordState.Error(getErrorMessageRes(result.code))
                        )
                    }
                }
            }
        }
    }

    fun onNextClick() {
        val phone = _uiState.value.phone
        val code = _uiState.value.code

        if (!validatePhone(phone)) {
            _uiState.update { it.copy(phoneError = ResourceR.string.forgot_password_phone_error_format) }
            return
        }
        if (code.length != 6) {
            _uiState.update { it.copy(codeError = ResourceR.string.forgot_password_code_error_length) }
            return
        }
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            authRepository.forgotPasswordVerify(phone, code).collect { result ->
                when (result) {
                    is ApiResult.Loading -> _uiState.update {
                        it.copy(isLoading = true, forgotPasswordState = ForgotPasswordState.Loading)
                    }
                    is ApiResult.Success -> {
                        _uiState.update {
                            it.copy(isLoading = false, forgotPasswordState = ForgotPasswordState.Success)
                        }
                        _effect.send(ForgotPasswordEffect.NavigateToResetPassword(phone))
                    }
                    is ApiResult.Error -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            forgotPasswordState = ForgotPasswordState.Error(getErrorMessageRes(result.code))
                        )
                    }
                }
            }
        }
    }

    fun onBackClick() {
        viewModelScope.launch {
            _effect.send(ForgotPasswordEffect.NavigateBack)
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
        in 500..599 -> ResourceR.string.forgot_password_error_server
        -1 -> ResourceR.string.forgot_password_error_timeout
        -2, -3 -> ResourceR.string.forgot_password_error_network
        else -> ResourceR.string.forgot_password_error_unknown
    }
}
