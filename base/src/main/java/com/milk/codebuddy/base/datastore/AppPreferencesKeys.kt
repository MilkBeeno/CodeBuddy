package com.milk.codebuddy.base.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * DataStore Key 常量集中管理
 *
 * 规范：
 * - 所有 Key 必须在此文件定义，禁止在业务模块散落定义
 * - Key 名称使用 snake_case 字符串，避免冲突
 *
 * 示例：
 * ```kotlin
 * appPreferences.get(AppPreferencesKeys.ACCESS_TOKEN, "")
 * ```
 */
object AppPreferencesKeys {
    val ACCESS_TOKEN = stringPreferencesKey("access_token")
    val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    val USER_ID = stringPreferencesKey("user_id")
    val USER_PHONE = stringPreferencesKey("user_phone")
    val USER_NICKNAME = stringPreferencesKey("user_nickname")
    val USER_AVATAR = stringPreferencesKey("user_avatar")
    val APP_THEME = stringPreferencesKey("app_theme")       // "light"|"dark"|"system"
    val APP_LANGUAGE = stringPreferencesKey("app_language")
}
