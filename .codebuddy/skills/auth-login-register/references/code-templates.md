# 认证模块代码模板

## 1. UiState + State + Effect 模板

```kotlin
package com.milk.codebuddy.login.ui.state

import androidx.compose.runtime.Immutable

@Immutable
data class XxxUiState(
    // 输入字段（邮箱场景用 email，密码场景用 password）
    val email: String = "",
    val code: String = "",

    // 字段错误（@StringRes Int?）
    val emailError: Int? = null,
    val codeError: Int? = null,

    // 验证码倒计时
    val countdownSeconds: Int = 0,
    val isCountingDown: Boolean = false,

    // 请求状态
    val isLoading: Boolean = false,
    val isSendingCode: Boolean = false,

    // 业务状态
    val xxxState: XxxState = XxxState.Idle
) {
    val canSendCode: Boolean
        get() = email.isValidEmail() && !isCountingDown && !isSendingCode

    val canSubmit: Boolean
        get() = email.isValidEmail() && code.length == 6 && !isLoading
}

@Immutable
sealed class XxxState {
    data object Idle : XxxState()
    data object Loading : XxxState()
    data object Success : XxxState()
    data class Error(val messageResId: Int) : XxxState()
}

@Immutable
sealed class XxxEffect {
    data class ShowToast(val messageResId: Int) : XxxEffect()
    data object NavigateToXxx : XxxEffect()
    data object NavigateBack : XxxEffect()
}
```

---

## 2. ViewModel 模板

```kotlin
package com.milk.codebuddy.login.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.milk.codebuddy.base.network.ApiResult
import com.milk.codebuddy.base.utils.DEFAULT_COUNTDOWN_SECONDS
import com.milk.codebuddy.base.utils.countdownFlow
import com.milk.codebuddy.base.utils.isValidEmail
import com.milk.codebuddy.login.data.repository.AuthRepository
import com.milk.codebuddy.login.ui.state.XxxEffect
import com.milk.codebuddy.login.ui.state.XxxState
import com.milk.codebuddy.login.ui.state.XxxUiState
import com.milk.codebuddy.resource.R as ResourceR
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class XxxViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    companion object {
        private const val COUNTDOWN_SECONDS = DEFAULT_COUNTDOWN_SECONDS
        private const val PASSWORD_MIN_LENGTH = 3
        private const val PASSWORD_MAX_LENGTH = 30
    }

    private val _uiState = MutableStateFlow(XxxUiState())
    val uiState: StateFlow<XxxUiState> = _uiState.asStateFlow()

    private val _effect = Channel<XxxEffect>()
    val effect = _effect.receiveAsFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email.trim(), emailError = null) }
    }

    fun onCodeChange(code: String) {
        _uiState.update {
            it.copy(code = code.filter { c -> c.isDigit() }.take(6), codeError = null)
        }
    }

    fun onSendCodeClick() {
        val email = _uiState.value.email
        if (!email.isValidEmail()) {
            _uiState.update { it.copy(emailError = ResourceR.string.xxx_email_error_format) }
            return
        }
        if (_uiState.value.isCountingDown) return

        viewModelScope.launch {
            authRepository.sendCode(email).collect { result ->
                when (result) {
                    is ApiResult.Loading -> _uiState.update { it.copy(isSendingCode = true) }
                    is ApiResult.Success -> {
                        _uiState.update { it.copy(isSendingCode = false) }
                        startCountdown()
                    }
                    is ApiResult.Error -> _uiState.update {
                        it.copy(
                            isSendingCode = false,
                            xxxState = XxxState.Error(getErrorMessageRes(result.code))
                        )
                    }
                }
            }
        }
    }

    fun onSubmitClick() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            authRepository.someMethod(/* params */).collect { result ->
                when (result) {
                    is ApiResult.Loading -> _uiState.update {
                        it.copy(isLoading = true, xxxState = XxxState.Loading)
                    }
                    is ApiResult.Success -> {
                        _uiState.update {
                            it.copy(isLoading = false, xxxState = XxxState.Success)
                        }
                        _effect.send(XxxEffect.NavigateToXxx)
                    }
                    is ApiResult.Error -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            xxxState = XxxState.Error(getErrorMessageRes(result.code))
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

    private fun getErrorMessageRes(code: Int): Int = when (code) {
        401 -> ResourceR.string.xxx_error_unauthorized
        403 -> ResourceR.string.xxx_error_forbidden
        in 500..599 -> ResourceR.string.xxx_error_server
        -1 -> ResourceR.string.xxx_error_timeout
        -2, -3 -> ResourceR.string.xxx_error_network
        else -> ResourceR.string.xxx_error_unknown
    }
}
```

---

## 3. Screen 模板

```kotlin
package com.milk.codebuddy.login.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.milk.codebuddy.login.ui.state.XxxEffect
import com.milk.codebuddy.login.ui.viewmodel.XxxViewModel

@Composable
fun XxxScreen(
    viewModel: XxxViewModel,
    modifier: Modifier = Modifier,
    onNavigateToXxx: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 消费单次事件
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is XxxEffect.NavigateToXxx -> onNavigateToXxx()
                is XxxEffect.NavigateBack -> onNavigateBack()
                is XxxEffect.ShowToast -> { /* context.toast(effect.messageResId) */ }
            }
        }
    }

    // UI 内容：根据 uiState 渲染
    XxxContent(
        modifier = modifier,
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onCodeChange = viewModel::onCodeChange,
        onSendCodeClick = viewModel::onSendCodeClick,
        onSubmitClick = viewModel::onSubmitClick
    )
}

@Composable
private fun XxxContent(
    modifier: Modifier = Modifier,
    uiState: com.milk.codebuddy.login.ui.state.XxxUiState,
    onEmailChange: (String) -> Unit,
    onCodeChange: (String) -> Unit,
    onSendCodeClick: () -> Unit,
    onSubmitClick: () -> Unit
) {
    // 布局实现
    // 颜色：LocalAppColors.current.xxx（禁止硬编码 Color()）
    // 字体：MaterialTheme.typography.xxx（禁止硬编码 fontSize）
    // 错误提示：uiState.emailError?.let { stringResource(it) }
}
```

---

## 4. Navigation 扩展函数模板

```kotlin
package com.milk.codebuddy.login.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.milk.codebuddy.base.ui.navigation.LocalNavController
import com.milk.codebuddy.base.ui.navigation.Xxx          // 目标路由
import com.milk.codebuddy.base.ui.navigation.Yyy          // 下一页路由
import com.milk.codebuddy.login.data.repository.AuthRepositoryProvider
import com.milk.codebuddy.login.ui.screen.XxxScreen
import com.milk.codebuddy.login.ui.viewmodel.AuthViewModelFactory
import com.milk.codebuddy.login.ui.viewmodel.XxxViewModel

fun NavGraphBuilder.xxxScreen() {
    composable<Xxx> {
        val controller = LocalNavController.current
        val factory = AuthViewModelFactory(AuthRepositoryProvider.get())
        XxxScreen(
            viewModel = viewModel<XxxViewModel>(factory = factory),
            modifier = Modifier.fillMaxSize(),
            onNavigateToYyy = {
                controller.navigate(Yyy) {
                    launchSingleTop = true
                }
            },
            onNavigateBack = {
                controller.popBackStack()
            }
        )
    }
}
```

**注意**：每新增一个页面，必须在 `app` 模块的根 `NavHost` 中调用对应扩展函数注册路由。

---

## 5. AuthViewModelFactory 新增 ViewModel

当新增 ViewModel 时，在 `AuthViewModelFactory.create()` 的 `when` 中添加分支：

```kotlin
modelClass.isAssignableFrom(XxxViewModel::class.java) ->
    XxxViewModel(authRepository) as T
```

若 ViewModel 需要 `SavedStateHandle`（带参路由）：

```kotlin
modelClass.isAssignableFrom(XxxViewModel::class.java) ->
    XxxViewModel(authRepository, extras.createSavedStateHandle()) as T
```

---

## 6. 带参路由的 ViewModel

```kotlin
class XxxViewModel(
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    // 从路由中提取参数（路由定义：@Serializable data class Xxx(val email: String)）
    private val email: String = savedStateHandle.toRoute<Xxx>().email

    private val _uiState = MutableStateFlow(XxxUiState(email = email))
    // ...
}
```

---

## 7. AuthRepositoryImpl 新增接口方法

```kotlin
// 1. 在 AuthRepository 接口中声明
fun newMethod(param: String): Flow<ApiResult<Unit>>

// 2. 在 AuthRepositoryImpl 实现
override fun newMethod(param: String): Flow<ApiResult<Unit>> = safeApiCall {
    val response = loginApi.newMethod(NewMethodRequest(param))
    if (response.code != SUCCESS_CODE) throw RuntimeException(response.message)
}

// 3. 在 LoginApi 接口中添加 suspend 方法
@POST("api/v1/auth/new-endpoint")
suspend fun newMethod(@Body request: NewMethodRequest): SendCodeResponse
```

---

## 8. SessionManager / UserSession

```kotlin
// UserSession DomainModel
data class UserSession(
    val accessToken: String = "",
    val refreshToken: String = "",
    val userId: String = "",
    val email: String = "",
    val nickname: String = "",
    val avatar: String = "",
    val isLoggedIn: Boolean = false
) {
    companion object {
        val EMPTY = UserSession()
    }
}

// 登录成功后保存会话
sessionManager.saveSession(
    UserSession(
        accessToken = tokenData.accessToken,
        refreshToken = tokenData.refreshToken,
        userId = userInfo?.userId.orEmpty(),
        email = userInfo?.email ?: email,
        nickname = userInfo?.nickname.orEmpty(),
        avatar = userInfo?.avatar.orEmpty(),
        isLoggedIn = true
    )
)
```

---

## 9. 错误码映射规范

```kotlin
private fun getErrorMessageRes(code: Int): Int = when (code) {
    401   -> ResourceR.string.xxx_error_unauthorized   // 未授权
    403   -> ResourceR.string.xxx_error_forbidden      // 禁止访问
    in 500..599 -> ResourceR.string.xxx_error_server   // 服务端错误
    -1    -> ResourceR.string.xxx_error_timeout        // 超时（ApiResult.Error.TIMEOUT）
    -2, -3 -> ResourceR.string.xxx_error_network       // 无网络 / IO 错误
    else  -> ResourceR.string.xxx_error_unknown        // 未知
}
```

---

## 10. 密码规则校验（注册/重置密码通用）

Figma 设计规范：密码长度 3–30 位，包含以下至少 2 种字符类型：大写字母(A-Z)、小写字母(a-z)、数字(0-9)、特殊字符(~!$#% 等)。

```kotlin
// 在 UiState 中加密码规则状态
@Immutable
data class RegisterSetPasswordUiState(
    val password: String = "",
    val passwordVisible: Boolean = false,
    val passwordError: Int? = null,
    val isLoading: Boolean = false,
    val setPasswordState: RegisterSetPasswordState = RegisterSetPasswordState.Idle
) {
    val isLengthValid: Boolean get() = password.length in 3..30
    val charTypeCount: Int get() = listOf(
        password.any { it.isUpperCase() },
        password.any { it.isLowerCase() },
        password.any { it.isDigit() },
        password.any { "~!$#%@^&*()_+-=[]{}|;':\",./<>?".contains(it) }
    ).count { it }
    val isCharTypeValid: Boolean get() = charTypeCount >= 2
    val isPasswordValid: Boolean get() = isLengthValid && isCharTypeValid
    val canSubmit: Boolean get() = isPasswordValid && !isLoading
}

// Effect：设置密码成功后跳 WelcomeScreen
sealed class RegisterSetPasswordEffect {
    data object NavigateToWelcome : RegisterSetPasswordEffect()
    data class ShowToast(val messageResId: Int) : RegisterSetPasswordEffect()
}
```

---

## 11. 认证流程总览

### Splash 路由判断
```
SplashScreen → 检查 SessionManager.isLoggedIn()
  ├─ true  → navigate(Main)  { popUpTo(Splash) { inclusive = true } }
  └─ false → navigate(Guide) { popUpTo(Splash) { inclusive = true } }
```

### 引导页（GuideScreen）
```
GuideScreen（未登录引导页，展示品牌信息 + 登录/注册入口）
  ├─ 点击「Sign in」  → navigate(EmailLogin)   { launchSingleTop = true }
  └─ 点击「Sign up」  → navigate(RegisterEnter) { launchSingleTop = true }
```

### 登录流程
```
EmailLoginScreen（邮箱+密码登录）
  ├─ 输入邮箱 + 密码（含显示/隐藏切换）
  ├─ 点击「Sign in」→ EmailLoginViewModel.onLoginClick() → authRepository.emailLogin(email, password)
  │    成功 → sessionManager.saveSession() → EmailLoginEffect.NavigateToMain
  │         → navigate(Main) { popUpTo(Guide) { inclusive = true } }
  ├─ 点击「Sign in with Google」→ 触发 Google OAuth 流程
  ├─ 点击「Sign in with phone number」→ navigate(PhoneLogin) { launchSingleTop = true }
  ├─ 点击「Sign up」→ navigate(RegisterEnter) { launchSingleTop = true }
  └─ 点击「Forgot Password?」→ navigate(ForgotPassword) { launchSingleTop = true }

PhoneLoginScreen（手机号+密码登录）
  ├─ 区号选择器（默认 +86），点击触发区号弹窗（BottomSheet 或 AlertDialog）
  │    弹窗列表展示国家/区号，选择后更新 PhoneLoginUiState.areaCode
  ├─ 输入手机号（纯数字，不含区号）+ 密码（含显示/隐藏切换）
  ├─ 点击「Sign in」→ PhoneLoginViewModel.onLoginClick()
  │    → authRepository.phoneLogin(areaCode, phoneNumber, password)
  │    成功 → sessionManager.saveSession() → PhoneLoginEffect.NavigateToMain
  │         → navigate(Main) { popUpTo(Guide) { inclusive = true } }
  └─ 点击「Forgot Password?」→ navigate(ForgotPassword) { launchSingleTop = true }
```

### 注册流程（两步骤）
```
RegisterEnterScreen（注册入口页）
  ├─ 点击「Sign up with email」→ navigate(RegisterVerifyEmail())  { launchSingleTop = true }
  ├─ 点击「Sign up with Google」→ 触发 Google OAuth 流程
  └─ 点击「Already have an account? Sign in」→ navigate(Login) { launchSingleTop = true }

RegisterVerifyEmailScreen（第一步：输入邮箱 + 验证码）
  ├─ 输入邮箱，点击「Send Code」→ authRepository.sendCode(email)
  │    成功 → 按钮进入倒计时（59s），可 Resend
  ├─ 输入验证码
  ├─ 按钮「Next」：邮箱空或验证码未填 → opacity=0.4 禁用
  └─ 点击「Next」→ authRepository.registerVerifyEmail(email, code)
       成功（返回 registerToken）→ RegisterVerifyEmailEffect.NavigateToSetPassword(email, code)
            → navigate(RegisterSetPassword(email, code)) { launchSingleTop = true }

RegisterSetPasswordScreen（第二步：设置密码，注册完成）
  ├─ ViewModel 通过 savedStateHandle.toRoute<RegisterSetPassword>() 获取 email/code
  ├─ 输入密码（含显示/隐藏切换）
  ├─ 实时展示密码规则提示：
  │    · Must be 3–30 characters
  │    · Include 2 types from: A-Z · a-z · 0-9 · ~!$#% etc.
  └─ 点击「Create Account」→ authRepository.registerSetPassword(email, code, password)
       成功（注册完成）→ RegisterSetPasswordEffect.NavigateToWelcome
            → navigate(Welcome) { launchSingleTop = true }

WelcomeScreen（注册完成欢迎页）
  ├─ 展示欢迎信息（无需网络，无 ViewModel）
  ├─ 点击「Set up」→ navigate(Main) { popUpTo(Guide) { inclusive = true } }
  └─ 点击「Skip」→ navigate(Main) { popUpTo(Guide) { inclusive = true } }
```

### 忘记密码流程
```
ForgotPasswordScreen
  ├─ 输入邮箱 + 验证码
  └─ 点击「下一步」→ authRepository.forgotPasswordVerify(email, code)
       成功 → ForgotPasswordEffect.NavigateToResetPassword(email)
            → navigate(ResetPassword(email)) { launchSingleTop = true }

ResetPasswordScreen（接收 email 参数）
  ├─ ViewModel 通过 savedStateHandle.toRoute<ResetPassword>().email 获取 email
  ├─ 输入新密码 + 确认密码（同密码规则：3-30位，2种字符类型）
  └─ 点击「提交」→ authRepository.resetPassword(email, newPassword, confirmPassword)
       成功 → ResetPasswordEffect.ShowToast + ResetPasswordEffect.NavigateToEmailLogin
            → navigate(EmailLogin) { popUpTo(ForgotPassword) { inclusive = true } }
```

---

## 12. PhoneLoginUiState（手机号登录）

手机号登录页 UiState 包含区号弹窗状态，区号通过弹窗（BottomSheet/Dialog）选择后写入 `areaCode`。

```kotlin
@Immutable
data class PhoneLoginUiState(
    // 区号（含 + 号，如 "+86"）
    val areaCode: String = "+86",
    // 手机号（纯数字，不含区号）
    val phoneNumber: String = "",
    // 密码
    val password: String = "",
    val passwordVisible: Boolean = false,

    // 字段错误
    val phoneNumberError: Int? = null,
    val passwordError: Int? = null,

    // 区号弹窗可见性
    val isAreaCodeDialogVisible: Boolean = false,

    // 请求状态
    val isLoading: Boolean = false,
    val loginState: PhoneLoginState = PhoneLoginState.Idle
) {
    val canSubmit: Boolean
        get() = phoneNumber.isValidPhone() && password.isNotBlank() && !isLoading
}

sealed class PhoneLoginState {
    data object Idle : PhoneLoginState()
    data object Loading : PhoneLoginState()
    data object Success : PhoneLoginState()
    data class Error(val messageResId: Int) : PhoneLoginState()
}

sealed class PhoneLoginEffect {
    data object NavigateToMain : PhoneLoginEffect()
    data object NavigateToForgotPassword : PhoneLoginEffect()
    data class ShowToast(val messageResId: Int) : PhoneLoginEffect()
}
```

**ViewModel 中区号弹窗操作：**
```kotlin
fun onShowAreaCodeDialog() { _uiState.update { it.copy(isAreaCodeDialogVisible = true) } }
fun onDismissAreaCodeDialog() { _uiState.update { it.copy(isAreaCodeDialogVisible = false) } }
fun onAreaCodeSelected(areaCode: String) {
    _uiState.update { it.copy(areaCode = areaCode, isAreaCodeDialogVisible = false) }
}
fun onPhoneNumberChange(phone: String) {
    _uiState.update { it.copy(phoneNumber = phone.filter { c -> c.isDigit() }.take(15), phoneNumberError = null) }
}
fun onPasswordChange(password: String) {
    _uiState.update { it.copy(password = password, passwordError = null) }
}
```
