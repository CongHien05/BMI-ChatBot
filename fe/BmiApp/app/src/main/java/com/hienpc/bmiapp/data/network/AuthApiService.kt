package com.hienpc.bmiapp.data.network

import com.hienpc.bmiapp.data.model.AuthResponse
import com.hienpc.bmiapp.data.model.LoginRequest
import com.hienpc.bmiapp.data.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * AuthApiService - Interface định nghĩa các API endpoints cho authentication
 * 
 * Retrofit sẽ tự động tạo implementation cho interface này
 * Sử dụng annotations để định nghĩa HTTP methods và paths
 */
interface AuthApiService {
    
    /**
     * Đăng ký tài khoản mới
     * @param request: RegisterRequest chứa email và password
     * @return Response<AuthResponse> chứa token nếu thành công
     */
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
    
    /**
     * Đăng nhập
     * @param request: LoginRequest chứa email và password
     * @return Response<AuthResponse> chứa token nếu thành công
     */
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
}

