package com.milk.codebuddy.login.ui.viewmodel

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.milk.codebuddy.login.data.local.SessionManager
import com.milk.codebuddy.login.data.remote.LoginApi
import com.milk.codebuddy.login.data.repository.AuthRepository
import com.milk.codebuddy.login.data.repository.AuthRepositoryImpl
import com.milk.codebuddy.login.network.NetworkException
import com.milk.codebuddy.login.network.NetworkResult
import com.milk.codebuddy.login.network.toNetworkException
import com.milk.codebuddy.login.ui.state.LoginEffect
import com.milk.codebuddy.login.ui.state.LoginState
import com.milk.codebuddy.login.ui.state.LoginUiState
import com.milk.codebuddy.resource.R as ResourceR
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.regex.Pattern

/**
 * 登录 ViewModel
 * 
 * 技术栈规范：
 * - 结构化并发：UI 逻辑绑定到 viewModelScope
 * - 单向数据流：UI 发出 Intent/Event，ViewModel 更新 State，UI 自动响应渲染
 * - Flow 操作：使用 .stateIn() 将冷流转为 StateFlow，设置 SharingStarted.WhileSubscribed(5000)
 * - 单次事件：对于 Toast 或导航等单次事件，使用 Channel 或 SharedFlow(replay = 0)
 */
@OptIn(FlowPreview::class)
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        // 手机号正则（中国大陆）
        private const val PHONE_REGEX = "^1[3-9]\\d{9}$"
        private const val COUNTDOWN_SECONDS = 60
        private const val DEBOUNCE_TIME = 300L
    }

    private val sessionManager = SessionManager(application)
    private val authRepository: AuthRepository

    // UI 状态
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // 单次事件（副作用）- 用于 Toast、导航等单次事件
    private val _effect = Channel<LoginEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        // 初始化仓库（实际项目中应该使用依赖注入）
        authRepository = AuthRepositoryImpl(
            loginApi = createMockLoginApi(),
            sessionManager = sessionManager
        )
    }

    /**
     * 创建模拟登录 API（实际项目中应该使用真实的 Retrofit 实例）
     */
    private fun createMockLoginApi(): LoginApi {
        return object : LoginApi {
            override suspend fun sendCode(request: com.milk.codebuddy.login.data.model.SendCodeRequest): com.milk.codebuddy.login.data.model.SendCodeResponse {
                // 模拟网络请求
                delay(1000)
                return com.milk.codebuddy.login.data.model.SendCodeResponse(
                    code = 200,
                    message = "success",
                    data = true
                )
            }

            override suspend fun login(request: com.milk.codebuddy.login.data.model.LoginRequest): com.milk.codebuddy.login.data.model.LoginResponse {
                // 模拟网络请求
                delay(1000)
                return com.milk.codebuddy.login.data.model.LoginResponse(
                    code = 200,
                    message = "success",
                    data = com.milk.codebuddy.login.data.model.TokenData(
                        accessToken = "mock_access_token",
                        refreshToken = "mock_refresh_token",
                        userInfo = com.milk.codebuddy.login.data.model.UserInfo(
                            userId = "1",
                            phone = request.phone,
                            nickname = "User",
                            avatar = null
                        )
                    )
                )
            }

            override suspend fun refreshToken(refreshToken: Map<String, String>): com.milk.codebuddy.login.data.model.LoginResponse {
                delay(500)
                return com.milk.codebuddy.login.data.model.LoginResponse(
                    code = 200,
                    message = "success",
                    data = com.milk.codebuddy.login.data.model.TokenData(
                        accessToken = "new_access_token",
                        refreshToken = "new_refresh_token",
                        userInfo = null
                    )
                )
            }
        }
    }

    /**
     * 手机号输入变化
     * 使用 debounce 防抖处理
     */
    fun onPhoneChange(phone: String) {
        _uiState.update { 
            it.copy(
                phone = phone.filter { c -> c.isDigit() }.take(11),
                phoneError = null
            )
        }
    }

    /**
     * 验证码输入变化
     */
    fun onCodeChange(code: String) {
        _uiState.update { 
            it.copy(
                code = code.filter { c -> c.isDigit() }.take(6),
                codeError = null
            )
        }
    }

    /**
     * 发送验证码
     * 带防抖处理，避免快速重复点击
     */
    fun onSendCodeClick() {
        val currentPhone = _uiState.value.phone
        
        // 验证手机号
        if (!validatePhone(currentPhone)) {
            _uiState.update { it.copy(phoneError = ResourceR.string.login_phone_error_format) }
            return
        }

        // 检查是否正在倒计时
        if (_uiState.value.isCountingDown) {
            return
        }

        _uiState.update { it.copy(isSendingCode = true) }

        viewModelScope.launch {
            when (val result = authRepository.sendCode(currentPhone)) {
                is NetworkResult.Success -> {
                    // 开始倒计时
                    startCountdown()
                    _uiState.update { it.copy(isSendingCode = false) }
                }
                is NetworkResult.Error -> {
                    _uiState.update { 
                        it.copy(
                            isSendingCode = false,
                            loginState = LoginState.Error(getErrorMessage(result.exception))
                        )
                    }
                }
                else -> {}
            }
        }
    }

    /**
     * 开始验证码倒计时
     * 使用 Flow 实现倒计时
     */
    private fun startCountdown() {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    countdownSeconds = COUNTDOWN_SECONDS,
                    isCountingDown = true
                )
            }
            
            // 使用 Flow 实现倒计时
            flow {
                repeat(COUNTDOWN_SECONDS) { seconds ->
                    emit(COUNTDOWN_SECONDS - seconds - 1)
                    delay(1000)
                }
            }
            .flowOn(Dispatchers.Default)
            .catch { e -> 
                // 异常处理
            }
            .collect { remainingSeconds ->
                _uiState.update { it.copy(countdownSeconds = remainingSeconds) }
            }
            
            _uiState.update { it.copy(isCountingDown = false) }
        }
    }

    /**
     * 登录点击
     * 带防抖处理，避免快速重复点击
     */
    fun onLoginClick() {
        val currentPhone = _uiState.value.phone
        val currentCode = _uiState.value.code

        // 验证手机号
        if (!validatePhone(currentPhone)) {
            _uiState.update { it.copy(phoneError = ResourceR.string.login_phone_error_format) }
            return
        }

        // 验证验证码
        if (currentCode.length != 6) {
            _uiState.update { it.copy(codeError = ResourceR.string.login_code_error_length) }
            return
        }

        // 检查是否正在加载
        if (_uiState.value.isLoading) {
            return
        }

        _uiState.update { 
            it.copy(
                isLoading = true,
                loginState = LoginState.Loading
            )
        }

        viewModelScope.launch {
            when (val result = authRepository.login(currentPhone, currentCode)) {
                is NetworkResult.Success -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            loginState = LoginState.Success
                        )
                    }
                    // 发送导航事件（单次事件）
                    _effect.send(LoginEffect.NavigateToMain)
                }
                is NetworkResult.Error -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            loginState = LoginState.Error(getErrorMessage(result.exception))
                        )
                    }
                }
                else -> {}
            }
        }
    }

    /**
     * 验证手机号格式
     */
    private fun validatePhone(phone: String): Boolean {
        return phone.isNotEmpty() && Pattern.matches(PHONE_REGEX, phone)
    }

    /**
     * 获取错误消息资源 ID
     * 统一处理网络异常，转换为友好的中文提示
     */
    @StringRes
    private fun getErrorMessage(exception: NetworkException): Int {
        return when (exception) {
            is NetworkException.Unauthorized -> ResourceR.string.login_error_unauthorized
            is NetworkException.Forbidden -> ResourceR.string.login_error_forbidden
            is NetworkException.NotFound -> ResourceR.string.login_error_network
            is NetworkException.Timeout -> ResourceR.string.login_error_timeout
            is NetworkException.ConnectionError -> ResourceR.string.login_error_network
            is NetworkException.ServerError -> ResourceR.string.login_error_server
            is NetworkException.BusinessError -> ResourceR.string.login_error_unknown
            is NetworkException.Unknown -> ResourceR.string.login_error_unknown
        }
    }

    /**
     * 清除错误状态
     */
    fun clearErrors() {
        _uiState.update {
            it.copy(
                phoneError = null,
                codeError = null,
                loginState = LoginState.Idle
            )
        }
    }
}
