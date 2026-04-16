# 认证模块架构规范

# 设计稿地址

设计稿地址 Figma-MCP-Server：https://www.figma.com/design/HzJDwENzzrItN8SyQZwpbU/V6.2.20?node-id=105-6631&m=dev

## 包结构

```
login/src/main/java/com/milk/codebuddy/login/
├── data/
│   ├── local/
│   │   └── SessionManager.kt                  # 基于 AppPreferences 的会话管理
│   ├── model/
│   │   ├── AuthRequest.kt                     # 通用请求基类
│   │   ├── BaseResponse.kt                    # 通用响应包装
│   │   ├── EmailLoginRequest.kt               # 邮箱+密码登录请求 DTO（email, password）
│   │   ├── EmailLoginResponse.kt              # 邮箱登录响应 DTO（含 accessToken/refreshToken/userInfo）
│   │   ├── PhoneLoginRequest.kt               # 手机号+密码登录请求 DTO（areaCode, phoneNumber, password）
│   │   ├── PhoneLoginResponse.kt              # 手机号登录响应 DTO（含 accessToken/refreshToken/userInfo）
│   │   ├── SendCodeRequest.kt                 # 发送验证码请求 DTO（注册/找回密码用，含 email/phone/areaCode 字段）
│   │   ├── SendCodeResponse.kt                # 发送验证码响应 DTO
│   │   ├── RegisterVerifyEmailRequest.kt      # 注册第一步：验证邮箱请求 DTO
│   │   ├── RegisterVerifyEmailResponse.kt     # 注册第一步：验证邮箱响应 DTO（含 registerToken）
│   │   ├── RegisterSetPasswordRequest.kt      # 注册第二步：设置密码请求 DTO（注册完成）
│   │   ├── ForgotPasswordSendCodeRequest.kt   # 找回密码：发送验证码请求 DTO（type="email"|"phone", email/areaCode/phoneNumber）
│   │   ├── ForgotPasswordVerifyRequest.kt     # 找回密码：验证码校验请求 DTO（type, identifier, code）
│   │   ├── ForgotPasswordVerifyResponse.kt    # 找回密码：验证成功响应 DTO（含 resetToken）
│   │   ├── ResetPasswordRequest.kt            # 重置密码请求 DTO（resetToken, newPassword）
│   │   └── UserSession.kt                     # Domain Model（含 EMPTY 静态对象）
│   ├── remote/
│   │   └── LoginApi.kt                        # Retrofit suspend 接口
│   └── repository/
│       ├── AuthRepository.kt                  # 接口
│       ├── AuthRepositoryImpl.kt              # 实现
│       └── AuthRepositoryProvider.kt          # 手动 DI 单例，预留 Hilt 迁移
└── ui/
    ├── components/
    │   ├── CountdownButton.kt                 # 验证码倒计时按钮组件
    │   └── LoginTextField.kt                  # 统一输入框组件
    ├── navigation/
    │   ├── SplashNavigation.kt                # fun NavGraphBuilder.splashScreen()
    │   ├── GuideNavigation.kt                 # fun NavGraphBuilder.guideScreen()（未登录引导页）
    │   ├── EmailLoginNavigation.kt            # fun NavGraphBuilder.emailLoginScreen()（邮箱登录）
    │   ├── PhoneLoginNavigation.kt            # fun NavGraphBuilder.phoneLoginScreen()（手机号登录）
    │   ├── RegisterEnterNavigation.kt         # fun NavGraphBuilder.registerEnterScreen()（注册入口）
    │   ├── RegisterVerifyEmailNavigation.kt   # fun NavGraphBuilder.registerVerifyEmailScreen()
    │   ├── RegisterSetPasswordNavigation.kt   # fun NavGraphBuilder.registerSetPasswordScreen()
    │   ├── WelcomeNavigation.kt               # fun NavGraphBuilder.welcomeScreen()（注册完成欢迎页）
    │   ├── ForgotPasswordNavigation.kt        # fun NavGraphBuilder.forgotPasswordScreen()（邮箱/手机号切换验证）
    │   └── ResetPasswordNavigation.kt         # fun NavGraphBuilder.resetPasswordScreen()（接收 resetToken）
    ├── screen/
    │   ├── SplashScreen.kt
    │   ├── GuideScreen.kt                     # 未登录引导页，展示 Sign in / Sign up 入口
    │   ├── EmailLoginScreen.kt                # 邮箱+密码登录（Google登录 + 手机号登录入口 + 注册入口 + 忘记密码）
    │   ├── PhoneLoginScreen.kt                # 手机号+密码登录（区号弹窗选择 + 忘记密码入口）
    │   ├── RegisterEnterScreen.kt             # 注册入口页（邮箱注册 + Google注册 + 登录入口）
    │   ├── RegisterVerifyEmailScreen.kt       # 注册第一步：输入邮箱 + 验证码
    │   ├── RegisterSetPasswordScreen.kt       # 注册第二步：设置密码
    │   ├── WelcomeScreen.kt                   # 注册完成欢迎页（设置 / 跳过 → Main）
    │   ├── ForgotPasswordScreen.kt            # 找回密码（Tab 切换：邮箱验证 / 手机号验证）
    │   └── ResetPasswordScreen.kt             # 重置密码（接收 resetToken，输入新密码 + 确认密码）
    ├── state/
    │   ├── EmailLoginUiState.kt               # EmailLoginUiState + EmailLoginState + EmailLoginEffect
    │   ├── PhoneLoginUiState.kt               # PhoneLoginUiState（含 areaCode、phoneNumber、password、isAreaCodeDialogVisible）
    │   ├── RegisterEnterUiState.kt            # RegisterEnterUiState（入口页，无表单，仅导航）
    │   ├── RegisterVerifyEmailUiState.kt      # 邮箱 + 验证码字段、倒计时
    │   ├── RegisterSetPasswordUiState.kt      # 密码 + 密码规则校验状态
    │   ├── ForgotPasswordUiState.kt           # 邮箱/手机号切换 Tab + 各自输入字段 + 倒计时 + 弹窗
    │   └── ResetPasswordUiState.kt            # newPassword + confirmPassword + 密码规则校验状态
    └── viewmodel/
        ├── AuthViewModelFactory.kt            # ViewModelProvider.Factory（手动 DI）
        ├── EmailLoginViewModel.kt             # 邮箱+密码登录逻辑
        ├── PhoneLoginViewModel.kt             # 手机号+密码登录逻辑（含区号弹窗状态）
        ├── RegisterEnterViewModel.kt          # 入口页，无需网络调用（仅导航）
        ├── RegisterVerifyEmailViewModel.kt    # 发送/验证邮箱验证码
        ├── RegisterSetPasswordViewModel.kt    # 设置密码（接收 email + code 参数，成功后跳 Welcome）
        ├── ForgotPasswordViewModel.kt         # 找回密码（邮箱/手机号切换 + 发送验证码 + 校验，成功后跳 ResetPassword）
        └── ResetPasswordViewModel.kt          # 重置密码（接收 resetToken，成功后回到 EmailLogin）
```

---

## 分层规范

### 数据层

- `LoginApi`：所有方法必须 `suspend`，禁止返回 `Call<T>`
- `AuthRepositoryImpl`：所有网络调用用 `safeApiCall` 包装，`code != 200` 抛 `RuntimeException`
- DTO → DomainModel 转换在 Repository 层完成（UserSession 构建）
- `AuthRepositoryProvider`：手动 DI 单例，`init(context, baseUrl, isDebug)` 在 MainActivity 调用一次

### UI 层

- UiState 用 `@Immutable data class`，Error 消息用 `@StringRes Int`（资源 ID）
- State 密封类：`Idle / Loading / Success / Error(messageResId: Int)`
- Effect 密封类用 `Channel()`（非 `Channel.BUFFERED`，replay=0），`receiveAsFlow()` 暴露
- ViewModel 通过构造器注入 `AuthRepository`（不持有 Context）
- 带参路由的 ViewModel 额外接收 `SavedStateHandle`，用 `savedStateHandle.toRoute<Xxx>()` 提取参数
- Screen 通过参数接收 `viewModel`（便于 Preview）
- 导航回调作为 lambda 参数传入 Screen，Screen 内部在 `LaunchedEffect` 消费 Effect

### 导航层

- 每个页面一个 `NavGraphBuilder.xxxScreen()` 扩展函数
- 在扩展函数内部用 `AuthViewModelFactory(AuthRepositoryProvider.get())` 创建 factory
- 通过 `LocalNavController.current` 获取 navController，禁止 Screen 直接持有
- 所有导航动作必须加 `launchSingleTop = true`
- Splash 判断完成后必须 `popUpTo(Splash) { inclusive = true }` 清除 Splash 本身
- 未登录跳引导页：`navigate(Guide) { popUpTo(Splash) { inclusive = true } }`
- 已登录跳主页：`navigate(Main) { popUpTo(Splash) { inclusive = true } }`
- 登录成功跳主页必须 `popUpTo(Guide) { inclusive = true }` 清除引导页及其以上的栈（邮箱/手机号登录均适用）
- 注册设置密码成功跳欢迎页：`navigate(Welcome) { launchSingleTop = true }`
- WelcomeScreen 点击任意按钮跳主页：`navigate(Main) { popUpTo(Guide) { inclusive = true } }`
- 忘记密码入口（EmailLogin / PhoneLogin）跳转：`navigate(ForgotPassword) { launchSingleTop = true }`
- ForgotPasswordScreen 验证成功跳重置密码：`navigate(ResetPassword(resetToken)) { launchSingleTop = true }`
- ResetPasswordScreen 重置成功回邮箱登录：`navigate(EmailLogin) { popUpTo(ForgotPassword) { inclusive = true }; launchSingleTop = true }`

---

## 路由表（Screen.kt）

```kotlin
@Serializable object Splash
@Serializable object Guide                                           // 未登录引导页（Sign in / Sign up 入口）
@Serializable object EmailLogin                                      // 邮箱+密码登录（含社交/手机号/注册/忘记密码入口）
@Serializable object PhoneLogin                                      // 手机号+密码登录（含区号弹窗/忘记密码入口）
@Serializable object RegisterEnter                                   // 注册入口（邮箱注册 + Google注册 + 登录入口）
@Serializable data class RegisterVerifyEmail(val email: String = "") // 注册第一步：输入邮箱 + 验证码
@Serializable data class RegisterSetPassword(                        // 注册第二步：设置密码
    val email: String,
    val code: String
)
@Serializable object Welcome                                         // 注册完成欢迎页（设置 / 跳过）
@Serializable object ForgotPassword                                     // 找回密码（页内切换邮箱/手机号验证）
@Serializable data class ResetPassword(val resetToken: String)           // 重置密码（携带服务端返回的 resetToken）
@Serializable object Main
```

---

## API 端点（LoginApi.kt）

```
POST api/v1/auth/email-login                emailLogin(EmailLoginRequest)             # 邮箱+密码登录
POST api/v1/auth/phone-login                phoneLogin(PhoneLoginRequest)             # 手机号+密码登录（含区号）
POST api/v1/auth/refresh-token              refreshToken(Map<String,String>)
POST api/v1/auth/register/verify-email      registerVerifyEmail(RegisterVerifyEmailRequest)   # 验证邮箱+验证码，返回 registerToken
POST api/v1/auth/register/set-password      registerSetPassword(RegisterSetPasswordRequest)   # 设置密码，注册完成
POST api/v1/auth/forgot-password/send-code  forgotPasswordSendCode(ForgotPasswordSendCodeRequest)  # 发送验证码（邮箱或手机号）
POST api/v1/auth/forgot-password/verify     forgotPasswordVerify(ForgotPasswordVerifyRequest)       # 验证码校验，返回 resetToken
POST api/v1/auth/reset-password             resetPassword(ResetPasswordRequest)                     # 重置密码（携带 resetToken）
```

---

## base 模块依赖

| 类                                     | 说明                                                                                                        |
|---------------------------------------|-----------------------------------------------------------------------------------------------------------|
| `safeApiCall { ... }`                 | 网络请求统一包装，返回 `Flow<ApiResult<T>>`，内部 `flowOn(Dispatchers.IO)`                                              |
| `ApiResult.Loading / Success / Error` | 网络结果密封类，`Error.code`：-1=超时，-2/-3=无网络                                                                      |
| `AppPreferences`                      | DataStore 封装，提供 `observe/put/remove/saveSession/clearSession/updateTokens`                                |
| `AppPreferencesKeys`                  | Key 常量：`ACCESS_TOKEN / REFRESH_TOKEN / IS_LOGGED_IN / USER_ID / USER_EMAIL / USER_NICKNAME / USER_AVATAR` |
| `countdownFlow(totalSeconds)`         | 倒计时冷流，每秒发射剩余秒数，完成后结束                                                                                      |
| `DEFAULT_COUNTDOWN_SECONDS`           | 默认 60 秒                                                                                                   |
| `String.isValidEmail()`               | 邮箱格式校验                                                                                                    |
| `String.isValidPhone()`               | 手机号格式校验（纯数字，长度 5-15 位，不含区号）                                                                               |
| `LocalNavController.current`          | 获取 NavController                                                                                          |
| `LocalAppColors.current`              | 获取颜色，禁止硬编码 Color()                                                                                        |
| `MaterialTheme.typography`            | 字体，禁止硬编码 fontSize/fontWeight                                                                              |
