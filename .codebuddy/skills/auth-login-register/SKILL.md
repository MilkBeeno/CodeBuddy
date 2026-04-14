---
name: auth-login-register
description: This skill should be used when the user needs to implement, modify, or extend authentication-related features in this Android project, including login, registration, forgot password, reset password, splash screen routing, session management, or any new screen/ViewModel/navigation that follows the existing auth module patterns (MVI + safeApiCall + AuthRepositoryProvider + AuthViewModelFactory).
---

# 认证模块开发指南（login 模块）

## 项目技术栈

- **UI**：Jetpack Compose + Material 3
- **架构**：MVI（UiState + UiEffect Channel + UiEvent 函数）
- **网络**：Retrofit + `safeApiCall`（base 模块）→ `Flow<ApiResult<T>>`
- **存储**：DataStore via `AppPreferences`（base 模块）
- **DI**：手动单例 `AuthRepositoryProvider`（预留 Hilt 迁移，目前无 Hilt）
- **导航**：类型安全路由 `@Serializable`，`LocalNavController.current`

## 参考文档

- `references/architecture.md`：包结构、分层规范、路由表、API 端点、base 模块依赖
- `references/code-templates.md`：UiState/ViewModel/Screen/Navigation 代码模板，以及流程总览

## 开发工作流

### 新增认证页面的完整步骤

**Step 1：在 `Screen.kt`（base 模块）中注册路由**

```kotlin
// 无参路由
@Serializable object NewPage

// 带参路由（参数限定为基本类型）
@Serializable data class NewPage(val email: String)
```

**Step 2：在 `state/` 中创建 `NewPageUiState.kt`**

- `data class` 加 `@Immutable`
- 错误字段类型为 `@StringRes Int?`（字符串资源 ID，非字符串）
- 业务状态密封类：`Idle / Loading / Success / Error(messageResId: Int)`
- Effect 密封类：导航事件、Toast 事件
- 参考 `references/code-templates.md` 第 1 节

**Step 3：在 `viewmodel/` 中创建 `NewPageViewModel.kt`**

- 通过构造器接收 `AuthRepository`（不持有 Context）
- 若需带参路由参数，额外接收 `SavedStateHandle`，用 `savedStateHandle.toRoute<NewPage>()` 提取
- `_uiState`：`MutableStateFlow`；`_effect`：`Channel<NewPageEffect>()`
- 邮箱过滤：`.trim()`，验证码：`.filter { c -> c.isDigit() }.take(6)`
- 手机号过滤：`.filter { c -> c.isDigit() }.take(15)`；区号弹窗状态通过 `isAreaCodeDialogVisible` 控制
- `sendCode` 成功后调用 `startCountdown()`
- 提交请求通过 `authRepository.xxx()` → `safeApiCall` → 在 `Success` 分支 `send(effect)`
- 错误码映射到字符串资源 ID（-1=超时，-2/-3=网络，500-599=服务器）
- 参考 `references/code-templates.md` 第 2 节

**Step 4：将 ViewModel 注册到 `AuthViewModelFactory`**

在 `when` 块中追加：

```kotlin
modelClass.isAssignableFrom(NewPageViewModel::class.java) ->
    NewPageViewModel(authRepository) as T
// 带 SavedStateHandle：
    NewPageViewModel(authRepository, extras.createSavedStateHandle()) as T
```

**Step 5：在 `screen/` 中创建 `NewPageScreen.kt`**

- 函数签名：`fun NewPageScreen(viewModel: NewPageViewModel, modifier: Modifier = Modifier, onNavigateToXxx: () -> Unit = {}, ...)`
- 用 `collectAsStateWithLifecycle()` 订阅 `uiState`（禁止 `collectAsState()`）
- 在 `LaunchedEffect(Unit)` 中消费 `viewModel.effect`
- 颜色用 `LocalAppColors.current`，字体用 `MaterialTheme.typography`
- 错误文本：`uiState.xxxError?.let { stringResource(it) }`
- 单个 Composable 不超过 80 行，拆分为 `NewPageContent()` 等子函数
- 参考 `references/code-templates.md` 第 3 节

**Step 6：在 `navigation/` 中创建 `NewPageNavigation.kt`**

```kotlin
fun NavGraphBuilder.newPageScreen() {
    composable<NewPage> {
        val controller = LocalNavController.current
        val factory = AuthViewModelFactory(AuthRepositoryProvider.get())
        NewPageScreen(
            viewModel = viewModel<NewPageViewModel>(factory = factory),
            modifier = Modifier.fillMaxSize(),
            onNavigateToXxx = { controller.navigate(Xxx) { launchSingleTop = true } },
            onNavigateBack = { controller.popBackStack() }
        )
    }
}
```

**Step 7：在 `app` 模块的根 `NavHost` 中注册**

```kotlin
NavHost(nav, startDestination = Splash) {
    splashScreen()
    guideScreen()               // 未登录引导页（Sign in / Sign up 入口）
    emailLoginScreen()          // 邮箱+密码登录（Google / 手机号 / 注册 / 忘记密码入口）
    phoneLoginScreen()          // 手机号+密码登录（区号弹窗 / 忘记密码入口）
    registerEnterScreen()       // 注册入口（邮箱注册 + Google注册 + 登录入口）
    registerVerifyEmailScreen() // 注册第一步：输入邮箱 + 验证码
    registerSetPasswordScreen() // 注册第二步：设置密码（注册完成）
    welcomeScreen()             // 注册完成欢迎页（设置 / 跳过 → Main）
    forgotPasswordScreen()
    resetPasswordScreen()
    newPageScreen()   // 追加
}
```

---

### 在 AuthRepository 中新增接口方法

1. `AuthRepository` 接口声明：`fun newMethod(param: String): Flow<ApiResult<Unit>>`
2. `AuthRepositoryImpl` 实现：`safeApiCall { val r = loginApi.newMethod(Req(param)); if (r.code != 200) throw RuntimeException(r.message) }`
3. `LoginApi` 接口追加 `@POST("...") suspend fun newMethod(@Body req: Req): SendCodeResponse`
4. 若需新建 DTO，在 `data/model/` 下创建，命名以 `Request` / `Response` 结尾

---

## 强制规范（违反将导致编译或运行时错误）

| 场景 | 正确做法 | 禁止 |
|---|---|---|
| 订阅 StateFlow | `collectAsStateWithLifecycle()` | `collectAsState()` |
| 颜色 | `LocalAppColors.current.xxx` | `Color(0xFF...)` 硬编码 |
| 字体 | `MaterialTheme.typography.xxx` | 硬编码 `fontSize` / `fontWeight` |
| 路由定义 | `Screen.kt` 中 `@Serializable` | 字符串路由 `"login"` |
| 获取 NavController | `LocalNavController.current` | 函数参数透传 |
| 导航 | 加 `launchSingleTop = true` | 不加（快速点击重复进栈） |
| 登录后跳主页 | `popUpTo(Guide) { inclusive = true }` | 不清栈（返回键回到引导页） |
| 注册完成跳主页 | WelcomeScreen 两按钮均 `popUpTo(Guide) { inclusive = true }` | 不清栈（返回键回注册页） |
| 手机号登录后跳主页 | `popUpTo(Guide) { inclusive = true }`（同邮箱登录规则） | 不清栈 |
| ViewModel 持有导航 | 通过 `UiEffect` → UI 层执行 | ViewModel 持有 NavController |
| 网络异常 | `safeApiCall` 统一处理 | ViewModel `try-catch` |
| DI 获取 Repository | `AuthRepositoryProvider.get()` | 业务层 `new AuthRepositoryImpl()` |
| 错误消息 | `@StringRes Int`（资源 ID） | 直接传字符串 |

---

## AuthRepositoryProvider 初始化

`MainActivity.onCreate` 中调用（已存在，新页面无需重复）：

```kotlin
AuthRepositoryProvider.init(
    context = this,
    baseUrl = BuildConfig.BASE_URL,
    isDebug = BuildConfig.DEBUG
)
```

---

## 验证码倒计时用法

```kotlin
// ViewModel 中
private fun startCountdown() {
    viewModelScope.launch {
        _uiState.update { it.copy(countdownSeconds = COUNTDOWN_SECONDS, isCountingDown = true) }
        countdownFlow(COUNTDOWN_SECONDS).collect { remaining ->
            _uiState.update { it.copy(countdownSeconds = remaining) }
        }
        _uiState.update { it.copy(isCountingDown = false) }
    }
}
```

UI 层使用现有 `CountdownButton` 组件，传入 `isCountingDown`、`countdownSeconds`、`onSendCode` 回调。
