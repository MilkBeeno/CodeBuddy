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
ForgotPasswordScreen（页内 Tab 切换：邮箱验证 / 手机号验证）
  ├─ Tab「Email」（默认选中）
  │    ├─ 输入邮箱
  │    ├─ 点击「Send Code」→ authRepository.forgotPasswordSendCode(type="email", email=...)
  │    │    成功 → 按钮进入倒计时（60s），可 Resend
  │    ├─ 输入验证码
  │    └─ 点击「Next」→ authRepository.forgotPasswordVerify(type="email", identifier=email, code)
  │         成功（返回 resetToken）→ ForgotPasswordEffect.NavigateToResetPassword(resetToken)
  │              → navigate(ResetPassword(resetToken)) { launchSingleTop = true }
  │
  └─ Tab「Phone」
       ├─ 区号选择器（默认 +86），点击触发区号弹窗（同手机号登录页弹窗）
       ├─ 输入手机号（纯数字，不含区号）
       ├─ 点击「Send Code」→ authRepository.forgotPasswordSendCode(type="phone", areaCode=..., phoneNumber=...)
       │    成功 → 按钮进入倒计时（60s），可 Resend
       ├─ 输入验证码
       └─ 点击「Next」→ authRepository.forgotPasswordVerify(type="phone", identifier=areaCode+phoneNumber, code)
            成功（返回 resetToken）→ ForgotPasswordEffect.NavigateToResetPassword(resetToken)
                 → navigate(ResetPassword(resetToken)) { launchSingleTop = true }

ResetPasswordScreen（接收 resetToken 参数）
  ├─ ViewModel 通过 savedStateHandle.toRoute<ResetPassword>().resetToken 获取 resetToken
  ├─ 输入新密码（含显示/隐藏切换）
  ├─ 输入确认密码（含显示/隐藏切换）
  ├─ 实时展示密码规则提示（同注册设置密码页规则：3-30位，2种字符类型）
  ├─ 两次密码不一致时展示错误提示
  └─ 点击「Reset Password」→ authRepository.resetPassword(resetToken, newPassword)
       成功 → ResetPasswordEffect.ShowToast + ResetPasswordEffect.NavigateToEmailLogin
            → navigate(EmailLogin) { popUpTo(ForgotPassword) { inclusive = true }; launchSingleTop = true }
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

---

## 13. ForgotPasswordUiState（找回密码，页内邮箱/手机号切换）

找回密码页通过 Tab 在邮箱验证和手机号验证间切换，两种方式共用同一个 ViewModel 和 UiState，通过 `verifyType` 区分当前激活的验证方式。

```kotlin
@Immutable
data class ForgotPasswordUiState(
    // 当前验证方式
    val verifyType: ForgotPasswordVerifyType = ForgotPasswordVerifyType.Email,

    // 邮箱验证字段
    val email: String = "",
    val emailCode: String = "",
    val emailError: Int? = null,
    val emailCodeError: Int? = null,
    val isEmailCountingDown: Boolean = false,
    val emailCountdownSeconds: Int = 0,
    val isSendingEmailCode: Boolean = false,

    // 手机号验证字段
    val areaCode: String = "+86",
    val phoneNumber: String = "",
    val phoneCode: String = "",
    val phoneNumberError: Int? = null,
    val phoneCodeError: Int? = null,
    val isPhoneCountingDown: Boolean = false,
    val phoneCountdownSeconds: Int = 0,
    val isSendingPhoneCode: Boolean = false,

    // 区号弹窗可见性（手机号验证 Tab 使用）
    val isAreaCodeDialogVisible: Boolean = false,

    // 请求状态
    val isLoading: Boolean = false,
    val forgotPasswordState: ForgotPasswordState = ForgotPasswordState.Idle
) {
    val canSendEmailCode: Boolean
        get() = email.isValidEmail() && !isEmailCountingDown && !isSendingEmailCode

    val canSendPhoneCode: Boolean
        get() = phoneNumber.isValidPhone() && !isPhoneCountingDown && !isSendingPhoneCode

    val canSubmitEmail: Boolean
        get() = email.isValidEmail() && emailCode.length == 6 && !isLoading

    val canSubmitPhone: Boolean
        get() = phoneNumber.isValidPhone() && phoneCode.length == 6 && !isLoading
}

enum class ForgotPasswordVerifyType { Email, Phone }

sealed class ForgotPasswordState {
    data object Idle : ForgotPasswordState()
    data object Loading : ForgotPasswordState()
    data object Success : ForgotPasswordState()
    data class Error(val messageResId: Int) : ForgotPasswordState()
}

sealed class ForgotPasswordEffect {
    data class NavigateToResetPassword(val resetToken: String) : ForgotPasswordEffect()
    data class ShowToast(val messageResId: Int) : ForgotPasswordEffect()
}
```

**ViewModel 关键操作：**
```kotlin
// 切换验证方式（Tab）
fun onVerifyTypeChange(type: ForgotPasswordVerifyType) {
    _uiState.update { it.copy(verifyType = type) }
}

// 邮箱输入
fun onEmailChange(email: String) {
    _uiState.update { it.copy(email = email.trim(), emailError = null) }
}

// 邮箱验证码输入
fun onEmailCodeChange(code: String) {
    _uiState.update { it.copy(emailCode = code.filter { c -> c.isDigit() }.take(6), emailCodeError = null) }
}

// 发送邮箱验证码
fun onSendEmailCodeClick() {
    val email = _uiState.value.email
    if (!email.isValidEmail()) {
        _uiState.update { it.copy(emailError = ResourceR.string.forgot_email_error_format) }
        return
    }
    viewModelScope.launch {
        authRepository.forgotPasswordSendCode(type = "email", email = email).collect { result ->
            when (result) {
                is ApiResult.Loading -> _uiState.update { it.copy(isSendingEmailCode = true) }
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isSendingEmailCode = false) }
                    startEmailCountdown()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSendingEmailCode = false, forgotPasswordState = ForgotPasswordState.Error(getErrorMessageRes(result.code)))
                }
            }
        }
    }
}

// 手机号验证码 + 区号弹窗操作（与 PhoneLoginViewModel 模式相同）
fun onShowAreaCodeDialog() { _uiState.update { it.copy(isAreaCodeDialogVisible = true) } }
fun onDismissAreaCodeDialog() { _uiState.update { it.copy(isAreaCodeDialogVisible = false) } }
fun onAreaCodeSelected(areaCode: String) {
    _uiState.update { it.copy(areaCode = areaCode, isAreaCodeDialogVisible = false) }
}
fun onPhoneNumberChange(phone: String) {
    _uiState.update { it.copy(phoneNumber = phone.filter { c -> c.isDigit() }.take(15), phoneNumberError = null) }
}
fun onPhoneCodeChange(code: String) {
    _uiState.update { it.copy(phoneCode = code.filter { c -> c.isDigit() }.take(6), phoneCodeError = null) }
}

// 提交（根据当前 verifyType 调用对应接口）
fun onSubmitClick() {
    if (_uiState.value.isLoading) return
    viewModelScope.launch {
        val state = _uiState.value
        val flow = when (state.verifyType) {
            ForgotPasswordVerifyType.Email ->
                authRepository.forgotPasswordVerify(type = "email", identifier = state.email, code = state.emailCode)
            ForgotPasswordVerifyType.Phone ->
                authRepository.forgotPasswordVerify(type = "phone", identifier = "${state.areaCode}${state.phoneNumber}", code = state.phoneCode)
        }
        flow.collect { result ->
            when (result) {
                is ApiResult.Loading -> _uiState.update { it.copy(isLoading = true, forgotPasswordState = ForgotPasswordState.Loading) }
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, forgotPasswordState = ForgotPasswordState.Success) }
                    _effect.send(ForgotPasswordEffect.NavigateToResetPassword(result.data.resetToken))
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, forgotPasswordState = ForgotPasswordState.Error(getErrorMessageRes(result.code)))
                }
            }
        }
    }
}
```

---

## 14. ResetPasswordUiState（重置密码）

重置密码页通过路由接收服务端返回的 `resetToken`，密码规则与注册设置密码页相同（3-30位，2种字符类型），额外校验两次密码一致性。

```kotlin
@Immutable
data class ResetPasswordUiState(
    val newPassword: String = "",
    val newPasswordVisible: Boolean = false,
    val confirmPassword: String = "",
    val confirmPasswordVisible: Boolean = false,

    // 字段错误
    val newPasswordError: Int? = null,
    val confirmPasswordError: Int? = null,

    // 请求状态
    val isLoading: Boolean = false,
    val resetPasswordState: ResetPasswordState = ResetPasswordState.Idle
) {
    // 密码规则（与注册设置密码一致）
    val isLengthValid: Boolean get() = newPassword.length in 3..30
    val charTypeCount: Int get() = listOf(
        newPassword.any { it.isUpperCase() },
        newPassword.any { it.isLowerCase() },
        newPassword.any { it.isDigit() },
        newPassword.any { "~!$#%@^&*()_+-=[]{}|;':\",./<>?".contains(it) }
    ).count { it }
    val isCharTypeValid: Boolean get() = charTypeCount >= 2
    val isPasswordValid: Boolean get() = isLengthValid && isCharTypeValid
    val isConfirmMatch: Boolean get() = newPassword == confirmPassword && confirmPassword.isNotBlank()
    val canSubmit: Boolean get() = isPasswordValid && isConfirmMatch && !isLoading
}

sealed class ResetPasswordState {
    data object Idle : ResetPasswordState()
    data object Loading : ResetPasswordState()
    data object Success : ResetPasswordState()
    data class Error(val messageResId: Int) : ResetPasswordState()
}

sealed class ResetPasswordEffect {
    data object NavigateToEmailLogin : ResetPasswordEffect()
    data class ShowToast(val messageResId: Int) : ResetPasswordEffect()
}
```

**ViewModel（带参路由）：**
```kotlin
class ResetPasswordViewModel(
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val resetToken: String = savedStateHandle.toRoute<ResetPassword>().resetToken

    fun onSubmitClick() {
        val state = _uiState.value
        if (!state.isPasswordValid) {
            _uiState.update { it.copy(newPasswordError = ResourceR.string.reset_password_error_rule) }
            return
        }
        if (!state.isConfirmMatch) {
            _uiState.update { it.copy(confirmPasswordError = ResourceR.string.reset_password_error_mismatch) }
            return
        }
        viewModelScope.launch {
            authRepository.resetPassword(resetToken, state.newPassword).collect { result ->
                when (result) {
                    is ApiResult.Loading -> _uiState.update { it.copy(isLoading = true, resetPasswordState = ResetPasswordState.Loading) }
                    is ApiResult.Success -> {
                        _uiState.update { it.copy(isLoading = false, resetPasswordState = ResetPasswordState.Success) }
                        _effect.send(ResetPasswordEffect.ShowToast(ResourceR.string.reset_password_success))
                        _effect.send(ResetPasswordEffect.NavigateToEmailLogin)
                    }
                    is ApiResult.Error -> _uiState.update {
                        it.copy(isLoading = false, resetPasswordState = ResetPasswordState.Error(getErrorMessageRes(result.code)))
                    }
                }
            }
        }
    }
}
```