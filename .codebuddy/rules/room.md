---
description: Android Room 数据库封装规范，定义 Entity、DAO、Database、Migration、Repository 的准则与代码模板，所有业务模块必须遵守
alwaysApply: false
enabled: true
---

# Room 数据库开发规范 (Modern Android Development)

本规范定义了在现代 Android 项目中如何构建高性能、健壮且易于测试的持久化层。作为架构师，我们坚持 **SSOT (Single Source of Truth)** 原则，将数据库视为应用状态的唯一合法来源。

---

## 一、依赖配置

```toml
# libs.versions.toml
[versions]
room = "2.6.1"
ksp = "2.0.21-1.0.28"

[libraries]
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
androidx-room-testing = { group = "androidx.room", name = "room-testing", version.ref = "room" }

[plugins]
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

```kotlin
// base/build.gradle.kts
plugins { alias(libs.plugins.ksp) }
dependencies {
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    testImplementation(libs.androidx.room.testing)
}
```

---

## 二、核心技术规范

### 响应式架构 (Reactive Stream)

- **Flow 为核心**：所有查询操作 (`Query`) 必须返回 `Flow<T>`。确保底层数据库变化时，UI 自动感知，符合单向数据流 (UDF)。
- **禁止使用 `LiveData`**：在 `Data` 层与 `Domain` 层统一使用 `Kotlin Flow`。

### 异步与并发

- **挂起函数 `(Suspend Functions)`**：所有 `Insert/Update/Delete` 操作必须声明为 `suspend`。
- **Dispatcher 约束**：虽然 Room 会自动处理线程，但在调用层应确保处于非主线程环境。

### 编译优化

- **KSP 支持**：必须使用 `KSP (Kotlin Symbol Processing)` 替代 `Kapt` 以获得更快的构建速度。

---

## 三、约束与原则

### 实体与模型解耦

- **Entity 隔离**：`@Entity` 类仅用于表结构，不得直接传递至 UI 层。
- **Mapper 机制**：`Repository` 负责将 `Entity` 转换为 `Domain Model`。

### 数据库完整性

- **事务性**：涉及多表操作必须使用 `@Transaction`。
- **索引**：对频繁查询和排序的字段必须建立 `@Index`。
- **迁移**：严禁生产环境使用 `fallbackToDestructiveMigration()`，必须显式定义 `Migration` 或 `AutoMigration`。

---

### 四、Agent 工作流

在生成 Room 相关代码时，AI Agent 必须遵循以下步骤：

1. **定义 Entity**：声明主键、字段类型及索引。
2. **编写 DAO**：定义 `Flow` 读取和 `suspend` 写入方法。
3. **构建 Database**：指定版本、实体清单和 `TypeConverters`。
4. **Hilt 注入**：提供单例 `Database` 和 `DAO` 实例。
5. **单元测试**：使用 `Room.inMemoryDatabaseBuilder` 进行逻辑验证。

---

## 五、见指令参考

### Entity 模版

- 类名以 `Entity` 结尾，表名用 `snake_case`
- 主键：`@PrimaryKey(autoGenerate = true) val id: Long = 0`
- 业务唯一键加 `@Index(unique = true)`
- 所有字段提供默认值；
- 禁止在 `Entity` 中放业务逻辑
- `Entity` 字段存储不超 4 KB JSON

```kotlin
@Entity(tableName = "user", indices = [Index(value = ["uid"], unique = true)])
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "uid") val uid: String,
    @ColumnInfo(name = "name") val name: String = "",
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)
```

### DAO 模板

- UI 层不直接操作 `DAO`、破坏分层
- `DAO` 方法不返回非 `Flow` / 非 `suspend`、阻赛主线程

```kotlin
@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun queryAll(): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: ProductEntity)
}

```

### 数据库配置与类型转换

* **类型转换器 (TypeConverters)**：Room 不支持存自定义对象（如 `Date` 或 `List`），你需要将其序列化为基础类型。

```kotlin
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time
}
```

* **数据库声明**：配置中必须包含所有的实体、版本号以及类型转换器。
```kotlin
@Database(entities = [TaskEntity::class], version = 1, exportSchema = true)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}
```

---

## 六、性能与测试

* **测试**：开启 `room.schemaLocation` 导出 JSON。
* **单例模式**：禁止多处调用 `databaseBuilder`。
* **用户数据丢失**：禁止 `fallbackToDestructiveMigration()` 用于线上。
* **索引 (Indexing)**：对经常出现在 `WHERE` 子句或 `ORDER BY` 里的字段务必添加索引，否则全表扫描会拖慢 UI 响应。
* **DAO 投影**：如果 UI 只需要 `title` 字段，不要查询 SELECT *。定义一个只包含所需字段的小类，Room 会自动映射。
* **批量处理**：如果你要插入 100 条数据，不要写循环调用 `insert()`，请在 `DAO` 中定义 `suspend fun insertAll(list: List<T>)`。
