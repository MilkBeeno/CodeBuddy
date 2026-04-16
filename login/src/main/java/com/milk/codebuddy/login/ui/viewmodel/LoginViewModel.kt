package com.milk.codebuddy.login.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.milk.codebuddy.base.network.ApiResult
import com.milk.codebuddy.base.utils.DEFAULT_COUNTDOWN_SECONDS
import com.milk.codebuddy.base.utils.countdownFlow
import com.milk.codebuddy.base.utils.isValidPhone
import com.milk.codebuddy.login.data.repository.AuthRepository
import com.milk.codebuddy.login.ui.state.LoginEffect
import com.milk.codebuddy.login.ui.state.LoginState
import com.milk.codebuddy.login.ui.state.LoginUiState
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
 * 登录 ViewModel（MVI 架构）
 *
 * - 依赖通过 Hilt 注入
 * - 单向数据流：UI 发出 Intent → ViewModel 更新 [uiState] → UI 响应渲染
 * - 单次事件：导航/Toast 通过 [effect] Channel（replay=0）发送，防止重复消费
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    companion object {
        private const val COUNTDOWN_SECONDS = DEFAULT_COUNTDOWN_SECONDS
    }

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _effect = Channel<LoginEffect>()
    val effect = _effect.receiveAsFlow()

    // ─────────────────────────── UI Events ───────────────────────────────

    fun onPhoneChange(phone: String) {
        _uiState.update {
            it.copy(
                phone = phone.filter { c -> c.isDigit() }.take(11),
                phoneError = null
            )
        }
    }

    fun onCodeChange(code: String) {
        _uiState.update {
            it.copy(
                code = code.filter { c -> c.isDigit() }.take(6),
                codeError = null
            )
        }
    }

    fun onSendCodeClick() {
        val phone = _uiState.value.phone
        if (!validatePhone(phone)) {
            _uiState.update { it.copy(phoneError = ResourceR.string.login_phone_error_format) }
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
                            loginState = LoginState.Error(getErrorMessageRes(result.code))
                        )
                    }
                }
            }
        }
    }

    fun onLoginClick() {
        val phone = _uiState.value.phone
        val code = _uiState.value.code

        if (!validatePhone(phone)) {
            _uiState.update { it.copy(phoneError = ResourceR.string.login_phone_error_format) }
            return
        }
        if (code.length != 6) {
            _uiState.update { it.copy(codeError = ResourceR.string.login_code_error_length) }
            return
        }
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            authRepository.login(phone, code).collect { result ->
                when (result) {
                    is ApiResult.Loading -> _uiState.update {
                        it.copy(isLoading = true, loginState = LoginState.Loading)
                    }
                    is ApiResult.Success -> {
                        _uiState.update {
                            it.copy(isLoading = false, loginState = LoginState.Success)
                        }
                        _effect.send(LoginEffect.NavigateToMain)
                    }
                    is ApiResult.Error -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            loginState = LoginState.Error(getErrorMessageRes(result.code))
                        )
                    }
                }
            }
        }
    }

    fun clearErrors() {
        _uiState.update {
            it.copy(phoneError = null, codeError = null, loginState = LoginState.Idle)
        }
    }

    // ─────────────────────────── Private ─────────────────────────────────

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
        401 -> ResourceR.string.login_error_unauthorized
        403 -> ResourceR.string.login_error_forbidden
        in 500..599 -> ResourceR.string.login_error_server
        -1 -> ResourceR.string.login_error_timeout
        -2, -3 -> ResourceR.string.login_error_network
        else -> ResourceR.string.login_error_unknown
    }
}
