---
description: Android Room 数据库封装规范，定义 Entity、DAO、Database、Migration、Repository 的准则与代码模板，所有业务模块必须遵守
alwaysApply: true
enabled: true
---

# Room 数据库封装规范（base 模块）

Room 统一封装在 `base/datastore/room/`，**禁止**业务模块自行创建独立 Database 实例。

```
datastore/room/
├── AppDatabase.kt          # RoomDatabase 单例
├── BaseDao.kt              # 通用 CRUD 接口
└── converter/
    └── CommonConverters.kt # TypeConverter（List/Date 等）
```

---

## 一、依赖配置

```toml
# libs.versions.toml
[versions]
room = "2.6.1"
ksp  = "2.0.21-1.0.28"

[libraries]
androidx-room-runtime  = { group = "androidx.room", name = "room-runtime",  version.ref = "room" }
androidx-room-ktx      = { group = "androidx.room", name = "room-ktx",      version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
androidx-room-testing  = { group = "androidx.room", name = "room-testing",  version.ref = "room" }

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

## 二、Entity 规范

- 类名以 `Entity` 结尾，表名用 `snake_case`
- 主键：`@PrimaryKey(autoGenerate = true) val id: Long = 0`
- 业务唯一键加 `@Index(unique = true)`
- 所有字段提供默认值；禁止在 Entity 中放业务逻辑

```kotlin
@Entity(tableName = "user", indices = [Index(value = ["uid"], unique = true)])
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "uid") val uid: String,
    @ColumnInfo(name = "name") val name: String = "",
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)
```

---

## 三、BaseDao

```kotlin
interface BaseDao<T> {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: T): Long

    @Update suspend fun update(entity: T)
    @Delete suspend fun delete(entity: T)

    @Transaction
    suspend fun upsert(entity: T) {
        if (insert(entity) == -1L) update(entity)
    }
}
```

### 业务 DAO

```kotlin
@Dao
interface UserDao : BaseDao<UserEntity> {
    @Query("SELECT * FROM user WHERE uid = :uid")
    fun observeByUid(uid: String): Flow<UserEntity?>   // observe 前缀 = 持续监听

    @Query("SELECT * FROM user ORDER BY created_at DESC")
    fun observeAll(): Flow<List<UserEntity>>

    @Query("SELECT * FROM user WHERE uid = :uid LIMIT 1")
    suspend fun findByUid(uid: String): UserEntity?    // find 前缀 = 一次性读取

    @Query("DELETE FROM user")
    suspend fun clearAll()
}
```

- DAO 方法全部返回 `Flow<T>` 或 `suspend`，**禁止**同步方法
- 多表关联加 `@Transaction`

---

## 四、TypeConverter

```kotlin
@TypeConverters(CommonConverters::class)
class CommonConverters {
    private val gson = Gson()
    @TypeConverter fun fromStringList(v: List<String>?): String = gson.toJson(v ?: emptyList<String>())
    @TypeConverter fun toStringList(v: String): List<String> =
        gson.fromJson(v, object : TypeToken<List<String>>() {}.type) ?: emptyList()
    @TypeConverter fun fromDate(d: Date?): Long? = d?.time
    @TypeConverter fun toDate(t: Long?): Date? = t?.let { Date(it) }
}
```

新增 Converter 追加到 `CommonConverters.kt`，禁止分散到业务模块。

---

## 五、AppDatabase

```kotlin
@Database(entities = [UserEntity::class], version = 1, exportSchema = true)
@TypeConverters(CommonConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    companion object { const val NAME = "app_database" }
}

@Module @InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideDb(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, AppDatabase.NAME)
            .fallbackToDestructiveMigrationOnDowngrade(true)
            .build()

    @Provides fun provideUserDao(db: AppDatabase): UserDao = db.userDao()
}
```

---

## 六、Migration

线上版本**禁止** `fallbackToDestructiveMigration()`，必须编写 `Migration`。

```kotlin
// AppDatabase.companion object
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE user ADD COLUMN bio TEXT NOT NULL DEFAULT ''")
    }
}
```

---

## 七、Repository 层

ViewModel **禁止**直接依赖 DAO，统一通过 Repository 接口访问。

```kotlin
interface UserLocalRepository {
    fun observeAll(): Flow<List<UserEntity>>
    suspend fun save(entity: UserEntity)
    suspend fun delete(entity: UserEntity)
}

class UserLocalRepositoryImpl @Inject constructor(private val dao: UserDao) : UserLocalRepository {
    override fun observeAll() = dao.observeAll()
    override suspend fun save(entity: UserEntity) = dao.upsert(entity)
    override suspend fun delete(entity: UserEntity) = dao.delete(entity)
}

@Module @InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton
    abstract fun bindUserLocal(impl: UserLocalRepositoryImpl): UserLocalRepository
}
```

---

## 八、ViewModel

```kotlin
val uiState = localRepo.observeAll()
    .map { UserUiState.Success(it) }
    .catch { emit(UserUiState.Error(it.message ?: "")) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserUiState.Loading)
```

---

## 九、测试

```kotlin
@RunWith(AndroidJUnit4::class)
class UserDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: UserDao

    @Before fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.userDao()
    }
    @After fun teardown() = db.close()

    @Test fun `upsert and observe`() = runTest {
        dao.upsert(UserEntity(uid = "u1", name = "Alice"))
        assertEquals("Alice", dao.observeByUid("u1").first()?.name)
    }
}
```

---

## 十、禁止事项

| 禁止行为                                    | 原因      |
|-----------------------------------------|---------|
| UI 层直接操作 DAO                            | 破坏分层    |
| DAO 方法返回非 `Flow` / 非 `suspend`          | 阻塞主线程   |
| Entity 中放业务逻辑                           | 违反单一职责  |
| `fallbackToDestructiveMigration()` 用于线上 | 用户数据丢失  |
| 多处调用 `databaseBuilder`                  | 破坏单例    |
| Entity 字段存储超 4 KB JSON                  | 性能差，应拆表 |
