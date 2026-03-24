package com.milk.codebuddy.login.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.milk.codebuddy.login.data.model.UserSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")

/**
 * 会话管理器
 * 使用 DataStore 存储用户会话信息
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
     */
    val userSession: Flow<UserSession> = context.dataStore.data.map { preferences ->
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

    /**
     * 保存用户会话
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
     */
    suspend fun updateTokens(accessToken: String, refreshToken: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ACCESS_TOKEN] = accessToken
            preferences[KEY_REFRESH_TOKEN] = refreshToken
        }
    }

    /**
     * 清除会话（登出）
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
