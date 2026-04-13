---
description: Android base 模块公共组件总规范，定义模块结构索引与各层核心约束，详细规范见各子文件
alwaysApply: true
enabled: true
---

# Android 公共组件规范（base 模块）

> 子规范：`tech.md` | `navigation.md`| `network.md` | `paging.md` | `datastore.md` | `room.md`

`base` 是全项目公用基础库，所有业务模块依赖此模块，**禁止**在业务模块中重复实现已有能力。

```
com.milk.codebuddy.base
├── network/                        # 网络层 → network.md
│   ├── interceptor/
│   │   ├── LoggingInterceptor.kt   # 日志（仅 Debug）
│   │   ├── HeaderInterceptor.kt    # 注入 Authorization / App-Version / Platform
│   │   └── AuthInterceptor.kt      # 401 自动刷新 Token
│   ├── RetrofitFactory.kt
│   └── ApiResult.kt
├── paging/                         # 分页层 → paging.md
│   ├── BasePagingSource.kt         # 通用 PagingSource 封装
│   └── PagingExt.kt                # Pager 构建扩展函数
├── datastore/                      # 存储层 → datastore.md / room.md
│   ├── AppPreferences.kt
│   ├── AppPreferencesKeys.kt
│   └── room/
│       ├── AppDatabase.kt
│       ├── BaseDao.kt
│       └── converter/CommonConverters.kt
├── utils/                          # 通用工具、扩展函数
└── ui/
    ├── navigation/                 # 导航 → navigation.md
    │   ├── NavControllerLocals.kt
    │   └── Screen.kt
    └── theme/
        ├── Color.kt                # 原始色板 + AppColors 语义层
        ├── Theme.kt                # MaterialTheme Light/Dark 配置
        └── Type.kt                 # 字体排版系统
```

---

## 一、UI 主题

- 颜色通过 `LocalAppColors.current` 访问，**禁止**硬编码 `Color(0xFF…)`
- 原始色板 `Color_XXXXXX` 仅在 `AppColors` 组装时引用，业务代码不得直接引用
- 字体通过 `MaterialTheme.typography` 引用，**禁止**硬编码 `fontSize` / `fontWeight`
- `Theme.kt` 支持 Light / Dark，`AppColors` 依据 `isSystemInDarkTheme()` 自动切换

---

## 二、导航

> 详见 **navigation.md**

- 路由定义在 `Screen.kt`，使用 `@Serializable`，**禁止**硬编码字符串路由
- 通过 `LocalNavController.current` 获取，**禁止**逐层参数透传
- ViewModel **禁止**持有 `NavController`，导航动作通过 `UiEffect` 传递

---

## 三、网络层

> 详见 **network.md**

- 所有请求通过 `safeApiCall` 包装，Repository 统一返回 `ApiResult`
- **禁止**业务模块自建 `OkHttpClient` / `Retrofit`
- DTO→Entity 转换在 Repository 层完成，`toDomain()` 定义在 `mapper` 包

---

## 四、分页（Paging 3）

> 详见 **paging.md**

- 纯网络继承 `BasePagingSource`；离线优先用 `RemoteMediator`
- `Pager` 必须通过 `buildPager` / `buildPagerWithMediator` 扩展函数构建
- 必须加 `cachedIn(viewModelScope)`；`LazyColumn` 的 `key` 使用 Entity 主键

---

## 五、存储

> DataStore 详见 **datastore.md**，Room 详见 **room.md**

- 键值存储用 `AppPreferences`，**禁止**使用 `SharedPreferences`
- 关系型数据用 Room，**禁止**业务模块自建 `Database` 实例
- ViewModel **禁止**直接依赖 DAO / `AppPreferences`，统一通过 Repository 访问

---

## 六、新增公共组件原则

1. **通用性**：至少被 2 个业务模块使用，单业务逻辑**禁止**放入 `base`
2. **无业务依赖**：`base` 不得依赖 `login`、`main` 等业务模块
3. **KDoc**：每个公共类/函数必须说明职责、参数、使用示例
4. **可测试**：公共工具类必须配套单元测试（正常路径 + 异常路径）
