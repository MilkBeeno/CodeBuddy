---
description: Android base 模块公共组件总规范，定义模块结构索引、UI 主题、新增组件原则，详细规范见各子文件
alwaysApply: true
enabled: true
---

# Android 公共组件规范（base 模块）

`base` 是全项目公用基础库，所有业务模块依赖此模块，**禁止**在业务模块中重复实现已有能力。

```
com.milk.codebuddy.base
├── network/                            # 网络层，详见 network.md
│   ├── interceptor/
│   │   ├── LoggingInterceptor.kt       # 日志（仅 Debug）
│   │   ├── HeaderInterceptor.kt        # 注入 Authorization / App-Version / Platform
│   │   └── AuthInterceptor.kt          # 401 自动刷新 Token
│   ├── RetrofitFactory.kt
│   └── ApiResult.kt
├── datastore/                          # 存储层
│   ├── AppPreferences.kt               # Preferences DataStore，详见 datastore.md
│   ├── AppPreferencesKeys.kt
│   └── room/                           # Room 封装，详见 room.md
├── utils/                              # 通用工具、扩展函数
└── ui/
    ├── navigation/                     # 导航，详见 navigation.md
    │   ├── NavControllerLocals.kt      # LocalNavController + ProvideNavHostController
    │   └── Screen.kt                   # 全局类型安全路由（@Serializable）
    └── theme/
        ├── Color.kt                    # 原始色板 + AppColors 语义层
        ├── Theme.kt                    # MaterialTheme Light/Dark 配置
        └── Type.kt                     # 字体排版系统
```

---

## 一、UI 主题

**颜色**（`Color.kt`）
- 原始色板 `Color_XXXXXX`：仅在组装 `AppColors` 时引用
- 语义层 `AppColors`（`primaryTextColor` / `primaryBackgroundColor` 等）：业务代码唯一入口
- **禁止**在 Composable 中硬编码 `Color(0xFF…)`，通过 `LocalAppColors.current` 访问

**字体**：通过 `MaterialTheme.typography` 引用，**禁止**硬编码 `fontSize` / `fontWeight`

**主题**：`Theme.kt` 支持 Light / Dark，`AppColors` 依据 `isSystemInDarkTheme()` 切换

---

## 二、导航

> 详见 **navigation.md**

核心约束：
- 路由必须定义在 `Screen.kt`，使用 `@Serializable`，禁止硬编码字符串
- 通过 `LocalNavController.current` 获取，禁止逐层传参
- ViewModel 禁止持有 `NavController`，通过 `UiEffect` 解耦

---

## 三、网络层

> 详见 **network.md**

核心约束：
- 所有请求通过 `safeApiCall` 包装，Repository 统一返回 `ApiResult`
- 禁止业务模块自建 `OkHttpClient` / `Retrofit`
- DTO→Entity 转换在 Repository 层完成，`toDomain()` 定义在 `mapper` 包

---

## 四、新增公共组件原则

1. **通用性**：至少被 2 个业务模块使用，单业务逻辑禁止放入 `base`
2. **无业务依赖**：`base` 不得依赖 `login`、`main` 等业务模块
3. **KDoc**：每个公共类/函数必须说明职责、参数、使用示例
4. **可测试**：公共工具类必须配套单元测试（正常路径 + 异常路径）
