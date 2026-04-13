---
description: Android Navigation Compose 导航规范，定义类型安全路由、NavController 访问、导航图拆分及 ViewModel 解耦准则，所有业务模块必须遵守
alwaysApply: true
enabled: true
---

# 导航规范（base 模块）

导航基础设施统一封装在 `base/ui/navigation/`，**禁止**业务模块自行声明路由或持有 `NavController`。

```
ui/navigation/
├── NavControllerLocals.kt  # LocalNavController + ProvideNavHostController
└── Screen.kt               # 全局类型安全路由（@Serializable）
```

---

## 一、路由定义（Screen.kt）

```kotlin
@Serializable object Splash
@Serializable object Login
@Serializable object Register
@Serializable object Main

// 带参路由用 data class，参数限定为基本类型
@Serializable data class ResetPassword(val phone: String)
```

- 新增页面统一追加到 `Screen.kt`，**禁止**在业务模块自行声明路由
- **禁止**硬编码字符串路由（如 `"login"` / `"main/{id}"`）

---

## 二、NavController 访问

通过 `CompositionLocal` 获取，**禁止**逐层参数透传。

```kotlin
// 根节点
ProvideNavHostController(rememberNavController()) { AppNavHost() }

// 任意子节点
val navController = LocalNavController.current
```

---

## 三、导航图拆分

按业务模块拆分为扩展函数，在根 `NavHost` 统一注册。

```kotlin
// app 模块
@Composable
fun AppNavHost() {
    val nav = LocalNavController.current
    NavHost(nav, startDestination = Splash) {
        authGraph()
        mainGraph()
    }
}

// login 模块
fun NavGraphBuilder.authGraph() {
    composable<Splash>  { SplashScreen() }
    composable<Login>   { LoginScreen() }
    composable<Register>{ RegisterScreen() }
}
```

---

## 四、导航动作

```kotlin
// 普通跳转
navController.navigate(Login) { launchSingleTop = true }

// 登录成功清栈跳主页
navController.navigate(Main) {
    popUpTo<Splash> { inclusive = true }
    launchSingleTop = true
}

// 带参跳转
navController.navigate(ResetPassword(phone = "138xxxx")) { launchSingleTop = true }
```

- 所有导航动作必须加 `launchSingleTop = true`
- 登录后跳主页必须 `popUpTo` 清栈

---

## 五、ViewModel 与导航解耦

ViewModel **禁止**持有 `NavController`，通过 `UiEffect` 通知 UI 层执行导航。

```kotlin
// ViewModel
sealed interface LoginEffect {
    data object ToMain : LoginEffect
}
val uiEffect = Channel<LoginEffect>(Channel.BUFFERED)
fun onLoginSuccess() = viewModelScope.launch { uiEffect.send(LoginEffect.ToMain) }

// UI
LaunchedEffect(Unit) {
    viewModel.uiEffect.receiveAsFlow().collect { effect ->
        when (effect) {
            is LoginEffect.ToMain -> navController.navigate(Main) {
                popUpTo<Login> { inclusive = true }
                launchSingleTop = true
            }
        }
    }
}
```

---

## 六、禁止事项

| 禁止行为                         | 原因                        |
|------------------------------|---------------------------|
| 硬编码字符串路由                     | 无类型安全，重构易出错               |
| 业务模块自行声明路由类                  | 路由分散，难以管理                 |
| ViewModel 持有 `NavController` | 内存泄漏，破坏 UDF               |
| 逐层透传 `NavController`         | 用 `LocalNavController` 代替 |
| 导航不加 `launchSingleTop`       | 快速点击导致重复入栈                |
| 登录后不清栈                       | 返回键会回到登录页                 |
