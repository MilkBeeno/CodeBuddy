---
description: Android Room 数据库封装规范，定义 Entity、DAO、Database、Migration、Repository 的准则与代码模板，所有业务模块必须遵守
alwaysApply: false
enabled: true
---

# Compose 网络交互开发规范 (MAD Architecture)

本规范定义了在 Jetpack Compose 中处理网络请求、加载状态及错误呈现的标准流程。

---

## 一、依赖配置

```toml
# libs.versions.toml
[versions]
composeBom = "2024.10.01"
activityCompose = "1.12.4"
navigationCompose = "2.8.5"
lifecycleViewmodelKtx = "2.10.0"
lifecycleViewmodelCompose = "2.10.0"
lifecycleRuntimeCompose = "2.10.0"
coil = "2.6.0"

[libraries]
androidx-lifecycle-viewmodel-ktx = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-ktx", version.ref = "lifecycleViewmodelKtx" }
androidx-lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycleRuntimeCompose" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleViewmodelCompose" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-compose-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-compose-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest" }
androidx-compose-ui-test-junit4 = { group = "androidx.compose.ui", name = "ui-test-junit4" }
androidx-compose-material3 = { group = "androidx.compose.material3", name = "material3", version.ref = "material3" }
androidx-compose-material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended", version.ref = "materialIconsExtended" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }
coil-compose = { group = "io.coil-kt", name = "coil-compose", version.ref = "coil" }
```

---

## 二、核心技术规范

### UI 状态建模 (UI State Modeling)

- **单一状态源**：每个屏幕应对应一个密封类（`Sealed Class`）或数据类，涵盖 `Idle`（空闲）、`Loading`（加载中）、`Success`（成功）和 `Error`（错误）四种基本状态。
- **Immutable**：UI 状态必须是不可变的，确保 `Compose` 的重组机制能够高效运行。

### 状态收集 (State Collection)

- **生命周期感知**：在 `Compose` 中收集网络 `Flow` 时，必须使用 `collectAsStateWithLifecycle()`。
- **防止重复请求**：进入 `Composable` 时，网络请求应由 `LaunchedEffect` 或 `ViewModel` 的 `init` 触发，避免重组导致请求无限循环。

### 列表与图片加载

- **异步图片**：必须使用 Coil 库处理网络图片，并配置 `crossfade` 动画。
- **分页集成**：分页网络数据应配合 `paging-compose` 库，使用 `collectAsLazyPagingItems()`、详细见 `paging.md` 文件。

---

## 三、约束与原则

### 副作用控制 (Side Effects)

- **禁止在 Composable 中发起请求**：严禁在 `Compose` 函数体中直接调用 `Repository` 或 `Service` 逻辑。
- **事件单向传递**：UI 产生的交互事件（如点击刷新）应通过回调传递给 `ViewModel`，由 `ViewModel` 触发网络调用。

### 离线优先与占位

- **骨架屏 (Shimmer)**：加载过程中应优先使用骨架屏占位图，提升视觉流畅度，而非单一的转圈加载（`CircularProgressIndicator`）。
- **错误捕获反馈**：网络错误应通过 `Scaffold` 的 `SnackbarHost` 或全屏错误页展示。

---

### 四、Agent 工作流

1. **定义 UiState**：创建 `Sealed Interface` 描述 UI 状态机。
2. **ViewModel 逻辑**： 
    - 持有 MutableStateFlow。
    - 启动 viewModelScope.launch 执行网络请求并更新状态。
3. **Compose 布局**：
    - 顶层使用 `when(uiState)` 模式分发不同布局（`LoadingView/ErrorView/ContentView`）。
    - 绑定 `PullRefresh`（下拉刷新）交互。
4. **图片渲染**：生成 AsyncImage 配置代码。

---

## 五、见指令参考

### UI状态机模版

```kotlin
sealed interface UserUiState {
    object Loading : UserUiState
    data class Success(val user: User) : UserUiState
    data class Error(val message: String) : UserUiState
}
```

### Compose 消费端标准代码

```kotlin
@Composable
fun UserProfileScreen(viewModel: UserViewModel = hiltViewModel()) {
    // 1. 生命周期感知地收集状态
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold {
        // 2. 根据状态渲染不同 UI
        when (val state = uiState) {
            is UserUiState.Loading -> LoadingSkeleton()
            is UserUiState.Error -> ErrorPlaceholder(retry = { viewModel.fetchUser() })
            is UserUiState.Success -> UserDetail(state.user)
        }
    }
}
}
```

---

## 六、性能与测试

* 复杂 Data Class 加 `@Immutable` / `@Stable`；耗时计算放 `remember` / `derivedStateOf`
* 优先 Material 3，颜色 / 字体通过 `MaterialTheme` 引用，**禁止**硬编码
* 必须处理 `WindowInsets`；单个 Composable 不超过 80 行
