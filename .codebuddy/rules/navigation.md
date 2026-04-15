---
description: Android Navigation Compose 导航规范，定义类型安全路由、NavController 访问、导航图拆分及 ViewModel 解耦准则，所有业务模块必须遵守
alwaysApply: false
enabled: true
---

# Navigation 架构开发规范 (MAD Architecture)

本规范旨在解决 Android 导航中的硬编码字符串、DeepLink 混乱以及 `Fragment/Composable` 重建状态丢失问题。

---

## 一、依赖配置

```toml
[versions]
navigation = "2.8.5" # 必须 2.8.0+ 以支持类型安全

[libraries]
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation" }
androidx-navigation-fragment = { group = "androidx.navigation", name = "navigation-fragment-ktx", version.ref = "navigation" }
androidx-navigation-ui = { group = "androidx.navigation", name = "navigation-ui-ktx", version.ref = "navigation" }
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version = "1.7.1" }
```

---

## 二、核心技术规范

### 类型安全导航 (Type-Safety)

* **拒绝硬编码**： `Route` 严禁使用 `String` 拼接路径。必须使用 `Navigation 2.8.0+` 引入的 `Kotlin Serialization` 进行类型安全导航。
* **定义路由类**：每个页面必须对应一个 `@Serializable` 的 `data class` 或 `object`。

### 导航控制器管理

* **单例 NavHost**：每个 `Activity` 原则上只持有一个 `NavHost`。
* **依赖注入**：`NavController` 不应直接注入到 `Repository` 或 `Domain` 层，它属于 UI 层的导航组件。

### 状态管理

* **ViewModel 作用域**：利用 `navGraphViewModels(regId)` 在多个关联页面间共享 `ViewModel`。
* **返回栈控制**：必须明确使用 `popUpTo` 和 `inclusive` 来清理不必要的返回栈，防止 OOM 和逻辑错误。

---

## 三、约束与原则

### 导航逻辑位置

- 逻辑在 ViewModel：执行在 UI：`ViewModel` 负责决定“去哪”，通过 `Channel` 或 `SharedFlow` 发送事件，`Fragment/Composable` 负责执行 `navController.navigate()`。

### DeepLink 规范

- **统一入口**：所有外部跳转必须通过 `navDeepLink` 统一注册。
- **参数校验**：`DeepLink` 进入页面后，必须在 `ViewModel` 初始化时对参数进行合法性校验。

### 动画与交互

- **统一转场**：必须在 `NavOptions` 中统一定义全局的 `enter/exit` 动画，保持视觉一致性。

---

## 四、Agent 工作流 (代码生成规范)

1. **定义路由**：使用 `@Serializable` 创建目标页面模型。
2. **构建图谱**：在 `NavHost` 中使用 `composable<T>` 或 `fragment<T>` 注册页面。
3. **参数传递**：利用 `navArgs` 委托属性在目标页面获取参数。
4. **跳转实现**：生成带参数模型的 `Maps(RouteObject)` 代码。
5. **单元测试**：验证 `TestNavHostController` 的当前目的地。

___

## 五、常见指令参考 (Best Practices)

### 类型安全路由定义

```kotlin
// 1. 定义目的地
@Serializable
sealed class Screen {
    @Serializable object Home : Screen()
    @Serializable data class Profile(val userId: String) : Screen()
}

// 2. 导航配置
NavHost(navController, startDestination = Screen.Home) {
    composable<Screen.Home> { HomeScreen(...) }
    composable<Screen.Profile> { backStackEntry ->
        val profile: Screen.Profile = backStackEntry.toRoute()
        ProfileScreen(profile.userId)
    }
}
```

* **清空栈跳转**：`navController.navigate(route) { popUpTo(graph.startDestinationId) { inclusive = true } }`
* **获取返回参数**：使用 `savedStateHandle` 在 `previousBackStackEntry` 中存取数据。
* **多模块导航**：将路由类定义在 `common-ui` 模块，各功能模块依赖它实现解耦导航。

---

## 六、性能与测试约束

1. **重复点击保护**:：在导航执行前检查 `currentBackStackEntry` 的目的地，防止快速双击打开两个重复页面。