package com.milk.codebuddy.login.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.milk.codebuddy.base.network.ApiResult
import com.milk.codebuddy.login.data.local.SessionManager
import com.milk.codebuddy.login.data.model.LoginResponse
import com.milk.codebuddy.login.data.model.SendCodeResponse
import com.milk.codebuddy.login.data.model.TokenData
import com.milk.codebuddy.login.data.model.UserInfo
import com.milk.codebuddy.login.data.remote.LoginApi
import com.milk.codebuddy.login.data.repository.AuthRepository
import com.milk.codebuddy.login.data.repository.AuthRepositoryImpl
import com.milk.codebuddy.login.ui.state.LoginEffect
import com.milk.codebuddy.login.ui.state.LoginState
import com.milk.codebuddy.login.ui.state.LoginUiState
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
 * 登录 ViewModel
 *
 * 技术栈规范：
 * - 结构化并发：所有协程绑定到 viewModelScope
 * - 单向数据流：UI 发出 Intent，ViewModel collect Flow<ApiResult<T>> 更新 UiState
 * - 单次事件：导航/Toast 通过 Channel（replay=0）发送，防止重复消费
 * - 零 try-catch：所有网络异常由 base 的 safeApiCall 内部 .catch 捕获并转为 ApiResult.Error
 */
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val PHONE_REGEX = "^1[3-9]\\d{9}$"
        private const val COUNTDOWN_SECONDS = 60
    }

    private val sessionManager = SessionManager(application)
    private val authRepository: AuthRepository

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /** 单次副作用事件（导航、Toast 等），Channel 保证只消费一次 */
    private val _effect = Channel<LoginEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        authRepository = AuthRepositoryImpl(
            loginApi = createMockLoginApi(),
            sessionManager = sessionManager
        )
    }

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
                            loginState = LoginState.Error(
                                getErrorMessageRes(result.code, result.message)
                            )
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
                            loginState = LoginState.Error(
                                getErrorMessageRes(result.code, result.message)
                            )
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
            flow {
                repeat(COUNTDOWN_SECONDS) { elapsed ->
                    emit(COUNTDOWN_SECONDS - elapsed - 1)
                    delay(1000L)
                }
            }
                .flowOn(Dispatchers.Default)
                .catch { /* 倒计时异常静默处理 */ }
                .collect { remaining ->
                    _uiState.update { it.copy(countdownSeconds = remaining) }
                }
            _uiState.update { it.copy(isCountingDown = false) }
        }
    }

    private fun validatePhone(phone: String): Boolean =
        phone.isNotEmpty() && Pattern.matches(PHONE_REGEX, phone)

    /** 根据错误码映射友好的中文提示资源 ID */
    private fun getErrorMessageRes(code: Int, message: String): Int = when (code) {
        401 -> ResourceR.string.login_error_unauthorized
        403 -> ResourceR.string.login_error_forbidden
        in 500..599 -> ResourceR.string.login_error_server
        -1 -> ResourceR.string.login_error_timeout
        -2, -3 -> ResourceR.string.login_error_network
        else -> ResourceR.string.login_error_unknown
    }

    // ─────────────────────── Mock（接入真实 API 时替换此处）─────────────────

    private fun createMockLoginApi(): LoginApi {
        return object : LoginApi {
            override suspend fun sendCode(
                request: com.milk.codebuddy.login.data.model.SendCodeRequest
            ): SendCodeResponse {
                delay(1000)
                return SendCodeResponse(code = 200, message = "success", data = true)
            }

            override suspend fun login(
                request: com.milk.codebuddy.login.data.model.LoginRequest
            ): LoginResponse {
                delay(1000)
                return LoginResponse(
                    code = 200,
                    message = "success",
                    data = TokenData(
                        accessToken = "mock_access_token",
                        refreshToken = "mock_refresh_token",
                        userInfo = UserInfo(
                            userId = "1",
                            phone = request.phone,
                            nickname = "User",
                            avatar = null
                        )
                    )
                )
            }

            override suspend fun refreshToken(
                refreshToken: Map<String, String>
            ): LoginResponse {
                delay(500)
                return LoginResponse(
                    code = 200,
                    message = "success",
                    data = TokenData(
                        accessToken = "new_access_token",
                        refreshToken = "new_refresh_token",
                        userInfo = null
                    )
                )
            }

            override suspend fun register(
                request: com.milk.codebuddy.login.data.model.RegisterRequest
            ): SendCodeResponse {
                delay(1000)
                return SendCodeResponse(code = 200, message = "success", data = true)
            }

            override suspend fun forgotPasswordVerify(
                request: com.milk.codebuddy.login.data.model.ForgotPasswordVerifyRequest
            ): SendCodeResponse {
                delay(1000)
                return SendCodeResponse(code = 200, message = "success", data = true)
            }

            override suspend fun resetPassword(
                request: com.milk.codebuddy.login.data.model.ResetPasswordRequest
            ): SendCodeResponse {
                delay(1000)
                return SendCodeResponse(code = 200, message = "success", data = true)
            }
        }
    }
}
