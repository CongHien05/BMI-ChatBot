package com.hienpc.bmiapp.utils

/**
 * UiState - sealed class chuẩn cho loading/success/error
 * Sprint 4: Enhanced với Empty state và error code
 */
sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Empty(val message: String = "Không có dữ liệu") : UiState<Nothing>()
    data class Error(
        val message: String,
        val errorCode: String? = null,
        val throwable: Throwable? = null
    ) : UiState<Nothing>()
}

/**
 * Extension functions để handle API errors
 */
fun Throwable.toErrorMessage(): String {
    return when (this) {
        is java.net.UnknownHostException -> "Không có kết nối mạng"
        is java.net.SocketTimeoutException -> "Kết nối timeout"
        is java.io.IOException -> "Lỗi kết nối"
        else -> this.message ?: "Đã xảy ra lỗi"
    }
}

/**
 * Retry wrapper cho API calls
 */
suspend fun <T> retryIO(
    times: Int = 3,
    initialDelay: Long = 100,
    maxDelay: Long = 1000,
    factor: Double = 2.0,
    block: suspend () -> T
): T {
    var currentDelay = initialDelay
    repeat(times - 1) {
        try {
            return block()
        } catch (e: Exception) {
            // Wait before retry
            kotlinx.coroutines.delay(currentDelay)
            currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
        }
    }
    return block() // Last attempt
}


