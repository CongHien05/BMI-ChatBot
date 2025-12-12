package com.hienpc.bmiapp.data.model

/**
 * RegisterRequest - Data class cho request đăng ký
 * 
 * DTO (Data Transfer Object) để gửi dữ liệu đăng ký lên server
 * Gson sẽ tự động serialize object này thành JSON
 */
data class RegisterRequest(
    val email: String,
    val password: String,
    val fullName: String? = null
)

