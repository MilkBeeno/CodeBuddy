package com.milk.codebuddy.login.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.milk.codebuddy.base.network.ApiResult
import com.milk.codebuddy.login.data.local.SessionManager
import com.milk.codebuddy.login.data.model.ForgotPasswordVerifyRequest
import com.milk.codebuddy.login.data.model.LoginRequest
import com.milk.codebuddy.login.data.model.LoginResponse
import com.milk.codebuddy.login.data.model.RegisterRequest
import com.milk.codebuddy.login.data.model.ResetPasswordRequest
import com.milk.codebuddy.login.data.model.SendCodeRequest
import com.milk.codebuddy.login.data.model.SendCodeResponse
import com.milk.codebuddy.login.data.remote.LoginApi
import com.milk.codebuddy.login.data.repository.AuthRepository
import com.milk.codebuddy.login.data.repository.AuthRepositoryImpl
import com.milk.codebuddy.login.ui.state.ForgotPasswordEffect
import com.milk.codebuddy.login.ui.state.ForgotPasswordState
import com.milk.codebuddy.login.ui.state.ForgotPasswordUiState
import com.milk.codebuddy.resource.R as ResourceR
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.regex.Pattern

/**
 * 忘记密码 ViewModel（MVI 架构）
 * 流程：输入手机号 → 发送验证码 → 验证通过 → 跳转重置密码页
 */
class ForgotPasswordViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val PHONE_REGEX = "^1[3-9]\\d{9}$"
        private const val COUNTDOWN_SECONDS = 60
    }

    private val sessionManager = SessionManager(application)
    private val authRepository: AuthRepository

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    private val _effect = Channel<ForgotPasswordEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        authRepository = AuthRepositoryImpl(createMockLoginApi(), sessionManager)
    }

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
            flow {
                repeat(COUNTDOWN_SECONDS) { elapsed ->
                    emit(COUNTDOWN_SECONDS - elapsed - 1)
                    delay(1000L)
                }
            }
                .flowOn(Dispatchers.Default)
                .catch { /* 静默处理 */ }
                .collect { remaining ->
                    _uiState.update { it.copy(countdownSeconds = remaining) }
                }
            _uiState.update { it.copy(isCountingDown = false) }
        }
    }

    private fun validatePhone(phone: String): Boolean =
        phone.isNotEmpty() && Pattern.matches(PHONE_REGEX, phone)

    private fun getErrorMessageRes(code: Int): Int = when (code) {
        in 500..599 -> ResourceR.string.forgot_password_error_server
        -1 -> ResourceR.string.forgot_password_error_timeout
        -2, -3 -> ResourceR.string.forgot_password_error_network
        else -> ResourceR.string.forgot_password_error_unknown
    }

    private fun createMockLoginApi(): LoginApi = object : LoginApi {
        override suspend fun sendCode(request: SendCodeRequest): SendCodeResponse {
            delay(1000)
            return SendCodeResponse(code = 200, message = "success", data = true)
        }

        override suspend fun login(request: LoginRequest): LoginResponse =
            LoginResponse(code = 200, message = "success", data = null)

        override suspend fun refreshToken(refreshToken: Map<String, String>): LoginResponse =
            LoginResponse(code = 200, message = "success", data = null)

        override suspend fun register(request: RegisterRequest): SendCodeResponse {
            delay(1000)
            return SendCodeResponse(code = 200, message = "success", data = true)
        }

        override suspend fun forgotPasswordVerify(request: ForgotPasswordVerifyRequest): SendCodeResponse {
            delay(1000)
            return SendCodeResponse(code = 200, message = "success", data = true)
        }

        override suspend fun resetPassword(request: ResetPasswordRequest): SendCodeResponse {
            delay(1000)
            return SendCodeResponse(code = 200, message = "success", data = true)
        }
    }
}
