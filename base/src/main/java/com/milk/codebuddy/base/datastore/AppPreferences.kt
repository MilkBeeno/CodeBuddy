package com.milk.codebuddy.base.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

/**
 * 全局 Preferences DataStore 封装
 *
 * 规范：
 * - UI / ViewModel 禁止直接访问 context.dataStore，统一通过此类操作
 * - 读取时必须捕获 IOException，防止文件损坏导致崩溃
 * - 所有 Key 集中在 [AppPreferencesKeys] 定义
 *
 * 使用示例（Repository）：
 * ```kotlin
 * class SessionRepositoryImpl(private val prefs: AppPreferences) {
 *     val isLoggedIn: Flow<Boolean> = prefs.isLoggedIn
 *
 *     suspend fun saveTokens(access: String, refresh: String) =
 *         prefs.saveTokens(access, refresh)
 * }
 * ```
 */
class AppPreferences(context: Context) {

    private val dataStore = context.applicationContext.dataStore

    // ── 语义化属性 ──────────────────────────────────────────────────────────

    /** 登录状态流 */
    val isLoggedIn: Flow<Boolean> = observe(AppPreferencesKeys.IS_LOGGED_IN, false)

    /** 主题设置流（"light" | "dark" | "system"） */
    val appTheme: Flow<String> = observe(AppPreferencesKeys.APP_THEME, "system")

    // ── 通用读写 ────────────────────────────────────────────────────────────

    /**
     * 持续观察某个 Key 的值
     *
     * @param key     DataStore Key，来自 [AppPreferencesKeys]
     * @param default 文件损坏或 Key 不存在时的默认值
     */
    fun <T> observe(key: Preferences.Key<T>, default: T): Flow<T> =
        dataStore.data
            .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
            .map { it[key] ?: default }

    /**
     * 一次性读取某个 Key 的值
     *
     * @param key     DataStore Key
     * @param default 默认值
     */
    suspend fun <T> get(key: Preferences.Key<T>, default: T): T =
        observe(key, default).first()

    /**
     * 写入单个 Key
     *
     * @param key   DataStore Key
     * @param value 要写入的值
     */
    suspend fun <T> put(key: Preferences.Key<T>, value: T) =
        dataStore.edit { it[key] = value }

    /**
     * 删除单个 Key
     *
     * @param key DataStore Key
     */
    suspend fun <T> remove(key: Preferences.Key<T>) =
        dataStore.edit { it.remove(key) }

    // ── 业务语义方法 ────────────────────────────────────────────────────────

    /**
     * 保存登录 Token 并标记已登录
     *
     * @param access  AccessToken
     * @param refresh RefreshToken
     */
    suspend fun saveTokens(access: String, refresh: String) = dataStore.edit {
        it[AppPreferencesKeys.ACCESS_TOKEN] = access
        it[AppPreferencesKeys.REFRESH_TOKEN] = refresh
        it[AppPreferencesKeys.IS_LOGGED_IN] = true
    }

    /**
     * 保存完整会话信息
     *
     * @param access    AccessToken
     * @param refresh   RefreshToken
     * @param userId    用户 ID
     * @param phone     手机号
     * @param nickname  昵称
     * @param avatar    头像 URL
     */
    suspend fun saveSession(
        access: String,
        refresh: String,
        userId: String,
        phone: String,
        nickname: String,
        avatar: String
    ) = dataStore.edit {
        it[AppPreferencesKeys.ACCESS_TOKEN] = access
        it[AppPreferencesKeys.REFRESH_TOKEN] = refresh
        it[AppPreferencesKeys.IS_LOGGED_IN] = true
        it[AppPreferencesKeys.USER_ID] = userId
        it[AppPreferencesKeys.USER_PHONE] = phone
        it[AppPreferencesKeys.USER_NICKNAME] = nickname
        it[AppPreferencesKeys.USER_AVATAR] = avatar
    }

    /**
     * 仅更新 Token（Token 刷新时使用）
     *
     * @param access  新 AccessToken
     * @param refresh 新 RefreshToken
     */
    suspend fun updateTokens(access: String, refresh: String) = dataStore.edit {
        it[AppPreferencesKeys.ACCESS_TOKEN] = access
        it[AppPreferencesKeys.REFRESH_TOKEN] = refresh
    }

    /**
     * 清除会话（退出登录）
     * 删除 Token 并将 isLoggedIn 置为 false，敏感数据完全清理
     */
    suspend fun clearSession() = dataStore.edit {
        it.remove(AppPreferencesKeys.ACCESS_TOKEN)
        it.remove(AppPreferencesKeys.REFRESH_TOKEN)
        it.remove(AppPreferencesKeys.USER_ID)
        it.remove(AppPreferencesKeys.USER_PHONE)
        it.remove(AppPreferencesKeys.USER_NICKNAME)
        it.remove(AppPreferencesKeys.USER_AVATAR)
        it[AppPreferencesKeys.IS_LOGGED_IN] = false
    }
}
