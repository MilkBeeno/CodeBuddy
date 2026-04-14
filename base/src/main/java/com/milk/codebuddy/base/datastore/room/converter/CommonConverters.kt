package com.milk.codebuddy.base.datastore.room.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

/**
 * Room 全局 TypeConverter
 *
 * 规范：
 * - 新增 Converter 统一追加到此文件，禁止分散到业务模块
 * - 每个 Converter 方法必须成对定义（序列化 + 反序列化）
 *
 * 已支持类型：
 * - `List<String>` ↔ JSON 字符串
 * - `Date` ↔ Long（Unix 时间戳毫秒）
 */
class CommonConverters {

    private val gson = Gson()

    // ── List<String> ────────────────────────────────────────────────────────

    /**
     * 将 `List<String>` 序列化为 JSON 字符串存储到数据库。
     * null 值视为空列表。
     */
    @TypeConverter
    fun fromStringList(value: List<String>?): String =
        gson.toJson(value ?: emptyList<String>())

    /**
     * 将 JSON 字符串反序列化为 `List<String>`。
     * 解析失败时返回空列表。
     */
    @TypeConverter
    fun toStringList(value: String): List<String> =
        gson.fromJson(value, object : TypeToken<List<String>>() {}.type)
            ?: emptyList()

    // ── Date ────────────────────────────────────────────────────────────────

    /**
     * 将 `Date` 转为 Long 时间戳（毫秒）存储。
     * null 值存储为 null。
     */
    @TypeConverter
    fun fromDate(date: Date?): Long? = date?.time

    /**
     * 将 Long 时间戳（毫秒）还原为 `Date`。
     * null 值还原为 null。
     */
    @TypeConverter
    fun toDate(timestamp: Long?): Date? = timestamp?.let { Date(it) }
}
