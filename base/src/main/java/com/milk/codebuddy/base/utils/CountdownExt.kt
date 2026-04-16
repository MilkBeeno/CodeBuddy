package com.milk.codebuddy.base.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * 创建一个从 [totalSeconds] 倒计时到 0 的冷流，每秒 emit 一次剩余秒数。
 *
 * @param totalSeconds 倒计时总秒数，默认 60
 * @return 依次 emit [totalSeconds, totalSeconds-1, …, 1] 的 Flow，异常静默处理
 *
 * 使用示例（ViewModel 层）：
 * ```kotlin
 * viewModelScope.launch {
 *     _uiState.update { it.copy(isCountingDown = true, countdownSeconds = DEFAULT_COUNTDOWN_SECONDS) }
 *     countdownFlow().collect { remaining ->
 *         _uiState.update { it.copy(countdownSeconds = remaining) }
 *     }
 *     _uiState.update { it.copy(isCountingDown = false) }
 * }
 * ```
 */
fun countdownFlow(totalSeconds: Int = DEFAULT_COUNTDOWN_SECONDS): Flow<Int> = flow {
    for (remaining in totalSeconds downTo 1) {
        emit(remaining)
        delay(1000L)
    }
    emit(0)
}
    .flowOn(Dispatchers.Default)
    .catch { /* 倒计时异常静默处理 */ }

// ── 校验工具 ────────────────────────────────────────────────────────────────

/** 中国大陆手机号正则（1开头，第二位3-9，共11位），缓存为顶层常量避免重复编译 */
private val PHONE_REGEX = Regex("^1[3-9]\\d{9}$")

/**
 * 校验是否为合法中国大陆手机号
 *
 * 使用示例：
 * ```kotlin
 * if (!phone.isValidPhone()) { showPhoneError() }
 * ```
 *
 * @return true 表示格式合法
 */
fun String.isValidPhone(): Boolean = isNotEmpty() && PHONE_REGEX.matches(this)

// ── 业务常量 ─────────────────────────────────────────────────────────────────

/** 发送验证码后的默认倒计时秒数 */
const val DEFAULT_COUNTDOWN_SECONDS = 60
