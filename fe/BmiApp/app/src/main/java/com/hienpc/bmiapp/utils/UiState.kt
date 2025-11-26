package com.hienpc.bmiapp.utils

/**
 * UiState - sealed class chuẩn cho loading/success/error
 * dùng chung cho các ViewModel ở Sprint 2.
 */
sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}


