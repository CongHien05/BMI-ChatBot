package com.hienpc.bmiapp.data.model

/**
 * LoginRequest - Data class cho request đăng nhập
 * 
 * DTO (Data Transfer Object) để gửi dữ liệu đăng nhập lên server
 * Gson sẽ tự động serialize object này thành JSON
 */
data class LoginRequest(
    val email: String,
    val password: String
)

