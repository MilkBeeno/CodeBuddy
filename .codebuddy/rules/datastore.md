---
description: Android DataStore 封装规范，定义 Preferences DataStore 的封装模板、Key 管理、Hilt 注入及禁止事项，所有业务模块必须遵守
alwaysApply: true
enabled: true
---

# DataStore 封装规范（base 模块）

DataStore 统一封装在 `base/datastore/`，**禁止** UI 层或 ViewModel 直接操作，**禁止**使用已废弃的 `SharedPreferences`。

```
datastore/
├── AppPreferences.kt       # Preferences DataStore 封装
├── AppPreferencesKeys.kt   # Key 常量集中管理
└── room/                   # Room 封装，详见 room.md
```

---

## 一、选型原则

| 场景                  | 方案                      |
|---------------------|-------------------------|
| 简单键值对（Token、主题、语言等） | `Preferences DataStore` |
| 复杂结构体、强类型           | `Proto DataStore`       |
| 关系型数据、多表            | Room                    |

---

## 二、Key 管理

所有 Key 集中定义在 `AppPreferencesKeys.kt`，**禁止**在业务模块中散落定义。

```kotlin
object AppPreferencesKeys {
    val ACCESS_TOKEN    = stringPreferencesKey("access_token")
    val REFRESH_TOKEN   = stringPreferencesKey("refresh_token")
    val IS_LOGGED_IN    = booleanPreferencesKey("is_logged_in")
    val APP_THEME       = stringPreferencesKey("app_theme")      // "light"|"dark"|"system"
    val APP_LANGUAGE    = stringPreferencesKey("app_language")
}
```

---

## 三、AppPreferences 封装

```kotlin
private val Context.dataStore by preferencesDataStore(name = "app_preferences")

@Singleton
class AppPreferences @Inject constructor(@ApplicationContext context: Context) {

    private val dataStore = context.dataStore

    fun <T> observe(key: Preferences.Key<T>, default: T): Flow<T> =
        dataStore.data
            .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
            .map { it[key] ?: default }

    suspend fun <T> get(key: Preferences.Key<T>, default: T): T =
        observe(key, default).first()

    suspend fun <T> put(key: Preferences.Key<T>, value: T) =
        dataStore.edit { it[key] = value }

    suspend fun <T> remove(key: Preferences.Key<T>) =
        dataStore.edit { it.remove(key) }

    // 语义化属性
    val isLoggedIn: Flow<Boolean> = observe(AppPreferencesKeys.IS_LOGGED_IN, false)
    val appTheme: Flow<String>    = observe(AppPreferencesKeys.APP_THEME, "system")

    suspend fun saveTokens(access: String, refresh: String) = dataStore.edit {
        it[AppPreferencesKeys.ACCESS_TOKEN]  = access
        it[AppPreferencesKeys.REFRESH_TOKEN] = refresh
        it[AppPreferencesKeys.IS_LOGGED_IN]  = true
    }

    suspend fun clearSession() = dataStore.edit {
        it.remove(AppPreferencesKeys.ACCESS_TOKEN)
        it.remove(AppPreferencesKeys.REFRESH_TOKEN)
        it[AppPreferencesKeys.IS_LOGGED_IN] = false
    }
}
```

---

## 四、Repository 层集成

涉及认证/会话**必须**通过 Repository 封装，ViewModel 不直接依赖 `AppPreferences`。

```kotlin
interface SessionRepository {
    val isLoggedIn: Flow<Boolean>
    suspend fun saveTokens(access: String, refresh: String)
    suspend fun clearSession()
}

class SessionRepositoryImpl @Inject constructor(
    private val prefs: AppPreferences
) : SessionRepository {
    override val isLoggedIn = prefs.isLoggedIn
    override suspend fun saveTokens(access: String, refresh: String) = prefs.saveTokens(access, refresh)
    override suspend fun clearSession() = prefs.clearSession()
}

@Module @InstallIn(SingletonComponent::class)
abstract class SessionModule {
    @Binds @Singleton
    abstract fun bindSession(impl: SessionRepositoryImpl): SessionRepository
}
```

---

## 五、ViewModel 使用

```kotlin
@HiltViewModel
class SettingsViewModel @Inject constructor(private val session: SessionRepository) : ViewModel() {
    val isLoggedIn = session.isLoggedIn
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
}
```

---

## 六、禁止事项

| 禁止行为                                    | 原因           |
|-----------------------------------------|--------------|
| UI / ViewModel 直接访问 `context.dataStore` | 破坏分层         |
| 使用 `SharedPreferences`                  | 已废弃，线程不安全    |
| 业务模块分散定义 Key                            | Key 冲突，难以管理  |
| 读取不加 `.catch { IOException }`           | 文件损坏时崩溃      |
| 退出登录不调用 `clearSession()`                | 敏感数据残留       |
| DataStore 存储单条超 1 KB 对象                 | 性能差，应改用 Room |
