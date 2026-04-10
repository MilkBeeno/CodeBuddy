package com.milk.codebuddy.login.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.milk.codebuddy.base.network.ApiResult
import com.milk.codebuddy.base.ui.navigation.ResetPassword
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
import com.milk.codebuddy.login.ui.state.ResetPasswordEffect
import com.milk.codebuddy.login.ui.state.ResetPasswordState
import com.milk.codebuddy.login.ui.state.ResetPasswordUiState
import com.milk.codebuddy.resource.R as ResourceR
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 重置密码 ViewModel（MVI 架构）
 * 通过 SavedStateHandle 获取导航传入的 phone 参数
 */
class ResetPasswordViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    companion object {
        private const val PASSWORD_MIN_LENGTH = 6
        private const val PASSWORD_MAX_LENGTH = 20
    }

    private val route: ResetPassword = savedStateHandle.toRoute()
    private val phone: String = route.phone

    private val sessionManager = SessionManager(application)
    private val authRepository: AuthRepository

    private val _uiState = MutableStateFlow(ResetPasswordUiState(phone = phone))
    val uiState: StateFlow<ResetPasswordUiState> = _uiState.asStateFlow()

    private val _effect = Channel<ResetPasswordEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        authRepository = AuthRepositoryImpl(createMockLoginApi(), sessionManager)
    }

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
