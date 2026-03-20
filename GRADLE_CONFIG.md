# Gradle 统一配置管理

## 概述

本项目使用 Gradle Version Catalog 和 buildSrc 实现统一的依赖和配置管理。

## 配置结构

```
CodeBuddy/
├── build.gradle.kts                      # 顶层构建配置
├── settings.gradle.kts                   # 项目设置和仓库配置
├── gradle/
│   └── libs.versions.toml             # 版本目录(Version Catalog)
├── buildSrc/                           # 自定义构建逻辑
│   ├── build.gradle.kts
│   └── src/main/kotlin/
│       └── CommonConfig.kt             # 通用配置函数
├── app/build.gradle.kts                 # 应用模块配置
├── base/build.gradle.kts                # 基础模块配置
├── main/build.gradle.kts                # 主模块配置
└── login/build.gradle.kts              # 登录模块配置
```

## 版本目录 (libs.versions.toml)

所有依赖版本集中在 `gradle/libs.versions.toml` 中管理:

```toml
[versions]
# 编译 SDK 版本
compileSdk = "36"
minSdk = "24"
targetSdk = "36"

# AndroidX 和 Compose 版本
coreKtx = "1.17.0"
lifecycleRuntimeKtx = "2.10.0"
activityCompose = "1.12.4"
composeBom = "2024.10.01"
material3 = "1.1.2"
navigationCompose = "2.8.5"

# 插件版本
agp = "9.0.1"
kotlin = "2.0.21"

[libraries]
# 依赖库定义
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
# ... 更多依赖

[plugins]
# 插件定义
android-application = { id = "com.android.application", version.ref = "agp" }
# ... 更多插件
```

## 通用配置 (CommonConfig.kt)

`buildSrc/src/main/kotlin/CommonConfig.kt` 提供了两个通用配置函数:

### `configureCommonLibrary()`

用于 Library 模块(base, main, login),自动配置:
- SDK 版本(compileSdk, minSdk, targetSdk)
- 编译选项(Java 11)
- Compose 配置
- ProGuard 规则
- 测试配置

### `configureCommonApplication()`

用于 Application 模块(app),包含 Library 的所有配置,额外支持:
- Application ID 配置
- 版本号配置

## 使用方式

### 在模块中应用通用配置

**Library 模块:**
```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.milk.codebuddy.module"
    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}

configureCommonLibrary()

dependencies {
    // 使用 libs 版本目录引用依赖
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.compose.material3)
}
```

**Application 模块:**
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.milk.codebuddy"
    defaultConfig {
        applicationId = "com.milk.codebuddy"
        versionCode = 1
        versionName = "1.0"
    }
}

configureCommonApplication()

dependencies {
    // 使用 libs 版本目录引用依赖
    implementation(project(":base"))
    implementation(libs.androidx.compose.material3)
}
```

## 优势

1. **统一版本管理**: 所有依赖版本在一个地方定义,避免版本冲突
2. **减少重复代码**: 通用配置自动应用到所有模块
3. **易于维护**: 更新依赖版本只需修改 `libs.versions.toml`
4. **类型安全**: 使用 DSL 访问依赖,避免拼写错误
5. **IDE 支持**: Android Studio 提供自动补全和跳转

## 更新依赖版本

1. 在 `gradle/libs.versions.toml` 中更新版本号
2. 在模块的 `build.gradle.kts` 中使用 `libs.xxx` 引用
3. Gradle 会自动应用新版本

```kotlin
// 只需更新版本目录中的版本
[versions]
material3 = "1.2.0"  // 从 1.1.2 更新到 1.2.0

// 所有模块自动使用新版本
implementation(libs.androidx.compose.material3)
```

## 添加新依赖

1. 在 `gradle/libs.versions.toml` 中添加版本和库定义:
```toml
[versions]
newLibrary = "1.0.0"

[libraries]
new-dependency = { group = "com.example", name = "library", version.ref = "newLibrary" }
```

2. 在模块中使用:
```kotlin
implementation(libs.new.dependency)
```

## 添加新模块

1. 创建新的模块目录
2. 创建 `build.gradle.kts`:
```kotlin
plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.milk.codebuddy.newmodule"
}

configureCommonLibrary()
```

3. 在 `settings.gradle.kts` 中注册模块:
```kotlin
include(":newmodule")
```

## 注意事项

- buildSrc 中的更改需要同步 Gradle
- 通用配置函数会覆盖默认配置,只需指定特定配置
- Version Catalog 的库名称使用 kebab-case(连字符分隔)
- 在 Kotlin DSL 中访问时使用点号替换连字符:`libs.androidx.core.ktx`
