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
 * @return 依次 emit [totalSeconds-1, totalSeconds-2, …, 0] 的 Flow，异常静默处理
 *
 * 使用示例（ViewModel 层）：
 * ```kotlin
 * viewModelScope.launch {
 *     _uiState.update { it.copy(isCountingDown = true, countdownSeconds = 60) }
 *     countdownFlow().collect { remaining ->
 *         _uiState.update { it.copy(countdownSeconds = remaining) }
 *     }
 *     _uiState.update { it.copy(isCountingDown = false) }
 * }
 * ```
 */
fun countdownFlow(totalSeconds: Int = 60): Flow<Int> = flow {
    repeat(totalSeconds) { elapsed ->
        emit(totalSeconds - elapsed - 1)
        delay(1000L)
    }
}
    .flowOn(Dispatchers.Default)
    .catch { /* 倒计时异常静默处理 */ }
