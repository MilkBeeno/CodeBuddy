---
description: Android DataStore 封装规范，定义 Preferences DataStore 的封装模板、Key 管理、Hilt 注入及禁止事项，所有业务模块必须遵守
alwaysApply: false
enabled: true
---

# Android DataStore 核心开发规范 (MAD Architecture)

本规范定义了在现代 Android 开发中如何正确、高性能、类型安全地使用 DataStore。作为资深架构师，我们坚持 **单向数据流 (UDF)** 和 **响应式编程** 原则，拒绝过度设计，追求极高的可测试性。

---

## 一、依赖配置

```toml
# libs.versions.toml
[versions]
datastore = "1.1.1"
kotlinxSerialization = "1.6.3"
protobuf = "3.25.1"
protobufPlugin = "0.9.4"

[libraries]
# Preferences DataStore (简单键值对)
androidx-datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }

# Proto DataStore (强类型，核心依赖)
androidx-datastore = { group = "androidx.datastore", name = "datastore", version.ref = "datastore" }
androidx-datastore-core = { group = "androidx.datastore", name = "datastore-core", version.ref = "datastore" }

# Kotlin Serialization (推荐用于 JSON 格式存储)
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "kotlinxSerialization" }

# Protobuf (推荐用于二进制格式存储)
protobuf-javalite = { group = "com.google.protobuf", name = "protobuf-javalite", version.ref = "protobuf" }
protobuf-kotlin-lite = { group = "com.google.protobuf", name = "protobuf-kotlin-lite", version.ref = "protobuf" }
```

---

## 二、核心技术规范

### 选型标准
- **不再使用 SharedPreferences**：禁止在任何新功能中使用 SP。
- **Proto DataStore 优先**：对于复杂数据结构、强类型需求和多模块项目，**必须**使用 Proto DataStore (基于 Protocol Buffers)。
- **Preferences DataStore 仅限简单场景**：仅在简单的 Key-Value (如：`is_first_launch`) 且不需要 Schema 安全的情况下使用。

### 并发与作用域
- **单例模式**：DataStore 实例必须在 `AppScope` 下作为单例存在（推荐通过 Hilt 注入）。
- **非阻塞式 I/O**：所有读写操作必须在 `Dispatchers.IO` 中执行。
- **流式暴露**：数据必须以 `Flow<T>` 形式暴露，严禁在 UI 层同步等待结果。

---

## 三、约束与原则

### 类型安全 (Type Safety)
- 必须通过 Kotlin Serialization 或 Protobuf 定义数据模型。
- 禁止在 Repository 层以外暴露 DataStore 的 `Edit` 键值。

### 响应式原则 (Reactive)
- **只读流**：ViewModel 只能观察数据流，不能持有本地状态副本，除非是为了 UI 转换。
- **原子性操作**：所有写操作必须使用 `edit` 或 `updateData` 块，确保事务性。

### 异常处理
- 必须处理 `IOException`（通常由磁盘故障引起）。
- 在 `dataStore.data.catch` 中捕获异常，并发送默认值或处理错误状态。

---

## 四、Agent 工作流 (代码生成规范)

在生成 DataStore 相关代码时，AI Agent 必须遵循以下步骤：

1.  **定义 Schema**：编写 `.proto` 文件或 `@Serializable` 数据类。
2.  **创建 Serializer**：实现 `Serializer<T>` 接口，包含默认值定义。
3.  **Hilt 模块注入**：在 `DataModule` 中定义 `DataStore<T>` 提供者，指定文件名。
4.  **Repository 封装**：
    - 暴露 `val data: Flow<T>`。
    - 提供针对特定字段更新的 `suspend fun updateXxx()` 方法。
5.  **单元测试**：生成基于 `TestScope` 和临时文件的单元测试代码。

---

## 五、常见指令参考 (Best Practices)

### 定义 Proto DataStore (推荐)

```kotlin
// 1. 定义数据结构
@Serializable
data class UserSettings(
    val theme: Theme = Theme.SYSTEM,
    val useDynamicColors: Boolean = true
)

// 2. 实现 Serializer
object UserSettingsSerializer : Serializer<UserSettings> {
    override val defaultValue: UserSettings = UserSettings()
    override suspend fun readFrom(input: InputStream): UserSettings {
        return try {
            Json.decodeFromString(UserSettings.serializer(), input.readBytes().decodeToString())
        } catch (e: SerializationException) {
            defaultValue
        }
    }
    override suspend fun writeTo(t: UserSettings, output: OutputStream) {
        output.write(Json.encodeToString(UserSettings.serializer(), t).encodeToByteArray())
    }
}
```

### Repository 实现模式

```kotlin
class SettingsRepository @Inject constructor(
    private val userSettingsStore: DataStore<UserSettings>
) {
    // 高性能流式读取
    val settings: Flow<UserSettings> = userSettingsStore.data
        .catch { exception ->
            if (exception is IOException) emit(UserSettings()) else throw exception
        }

    // 类型安全的写操作
    suspend fun updateTheme(newTheme: Theme) {
        userSettingsStore.updateData { current ->
            current.copy(theme = newTheme)
        }
    }
}
```

---

## 六、性能与测试约束

1. **禁止在 init 块中调用 DataStore**：避免阻塞主线程初始化。
2. **禁止过度使用 map**：如果 UI 只需要一个字段，在 ViewModel 中进行 distinctUntilChanged() 过滤，减少无效刷新。
3. **单元测试**：必须使用 Job 或 TestScope 确保测试完成后清理 DataStore 临时文件。
4. **严禁阻塞式读取**：禁止使用 runBlocking 调用 DataStore。 DataStore 依赖于协程，同步读取会导致 UI 卡顿甚至 ANR。请始终使用 collect 或转换为 StateFlow
