package com.milk.codebuddy.login.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.milk.codebuddy.login.data.model.UserSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")

/**
 * 会话管理器
 * 使用 DataStore 存储用户会话信息
 * 
 * 技术栈规范：
 * - 解耦封装：DataStore 封装在 Manager 内部，通过 Flow 暴露配置项
 * - 异常处理：读取操作包含 IOException 处理逻辑
 * - 线程隔离：所有读写操作强制在 Dispatchers.IO 中执行
 */
class SessionManager(private val context: Context) {

    companion object {
        private val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_PHONE = stringPreferencesKey("phone")
        private val KEY_NICKNAME = stringPreferencesKey("nickname")
        private val KEY_AVATAR = stringPreferencesKey("avatar")
    }

    /**
     * 获取用户会话
     * 使用 catch 操作符捕获 IOException 异常，防止底层存储损坏导致应用崩溃
     */
    val userSession: Flow<UserSession> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                // 存储损坏时返回空会话
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val accessToken = preferences[KEY_ACCESS_TOKEN].orEmpty()
            val refreshToken = preferences[KEY_REFRESH_TOKEN].orEmpty()
            
            if (accessToken.isNotEmpty()) {
                UserSession(
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    userId = preferences[KEY_USER_ID].orEmpty(),
                    phone = preferences[KEY_PHONE].orEmpty(),
                    nickname = preferences[KEY_NICKNAME].orEmpty(),
                    avatar = preferences[KEY_AVATAR].orEmpty(),
                    isLoggedIn = true
                )
            } else {
                UserSession.EMPTY
            }
        }
        .flowOn(Dispatchers.IO)

    /**
     * 保存用户会话
     * 在 IO 线程执行写操作
     */
    suspend fun saveSession(session: UserSession) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ACCESS_TOKEN] = session.accessToken
            preferences[KEY_REFRESH_TOKEN] = session.refreshToken
            preferences[KEY_USER_ID] = session.userId
            preferences[KEY_PHONE] = session.phone
            preferences[KEY_NICKNAME] = session.nickname
            preferences[KEY_AVATAR] = session.avatar
        }
    }

    /**
     * 更新 Token
     * 在 IO 线程执行写操作
     */
    suspend fun updateTokens(accessToken: String, refreshToken: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ACCESS_TOKEN] = accessToken
            preferences[KEY_REFRESH_TOKEN] = refreshToken
        }
    }

    /**
     * 清除会话（登出）
     * 全量清理：用户退出登录时的全局清理逻辑，确保敏感信息被安全重置
     */
    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    /**
     * 检查是否已登录
     */
    fun isLoggedIn(): Flow<Boolean> = userSession.map { it.isLoggedIn }
}
