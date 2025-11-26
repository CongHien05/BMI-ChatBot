package com.hienpc.bmiapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hienpc.bmiapp.data.model.AuthResponse
import com.hienpc.bmiapp.data.model.LoginRequest
import com.hienpc.bmiapp.data.model.RegisterRequest
import com.hienpc.bmiapp.data.network.ApiClient
import com.hienpc.bmiapp.data.network.AuthApiService
import kotlinx.coroutines.launch

/**
 * AuthViewModel - ViewModel cho authentication
 * 
 * Sử dụng MVVM pattern:
 * - ViewModel quản lý UI-related data
 * - Sử dụng LiveData để expose data cho UI
 * - Sử dụng Coroutines (viewModelScope) để thực hiện async operations
 * - ViewModel sống sót qua configuration changes (như screen rotation)
 */
class AuthViewModel : ViewModel() {
    
    // API Service instance
    private val authApiService: AuthApiService = ApiClient.createService(AuthApiService::class.java)
    
    // LiveData cho success state - chứa token khi đăng nhập/đăng ký thành công
    private val _authSuccess = MutableLiveData<String?>()
    val authSuccess: LiveData<String?> = _authSuccess
    
    // LiveData cho error state - chứa error message khi có lỗi
    private val _authError = MutableLiveData<String?>()
    val authError: LiveData<String?> = _authError
    
    // LiveData cho loading state - để hiển thị progress bar
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    /**
     * Validate cơ bản cho email & password.
     * Trả về message lỗi (String) nếu không hợp lệ, null nếu hợp lệ.
     */
    private fun validateCredentials(email: String, password: String): String? {
        if (email.isBlank() || password.isBlank()) {
            return "Email và mật khẩu không được để trống"
        }
        // Validate format email đơn giản
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
        if (!emailRegex.matches(email)) {
            return "Email không hợp lệ"
        }
        // Độ dài mật khẩu tối thiểu
        if (password.length < 6) {
            return "Mật khẩu phải có ít nhất 6 ký tự"
        }
        return null
    }
    
    /**
     * Đăng ký tài khoản mới
     * @param email: Email của user
     * @param password: Password của user
     */
    fun register(email: String, password: String) {
        // Validate input chi tiết
        validateCredentials(email, password)?.let { errorMsg ->
            _authError.value = errorMsg
            return
        }
        
        // Sử dụng viewModelScope để launch coroutine
        // viewModelScope sẽ tự động cancel khi ViewModel bị cleared
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _authError.value = null
                
                // Tạo request object
                val request = RegisterRequest(email, password)
                
                // Gọi API
                val response = authApiService.register(request)

                // Kiểm tra response
                if (response.isSuccessful) {
                    val body = response.body()
                    val token = body?.token

                    if (!token.isNullOrBlank()) {
                        // Thành công - lưu token
                        _authSuccess.value = token
                    } else {
                        // Response ok nhưng không có token → coi như lỗi
                        _authError.value = body?.message ?: "Đăng ký thất bại (không nhận được token)"
                    }
                } else {
                    // Lỗi từ server (4xx/5xx) - cố gắng đọc errorBody để hiển thị message rõ ràng hơn
                    val rawError = try {
                        response.errorBody()?.string()
                    } catch (e: Exception) {
                        null
                    }
                    _authError.value = rawError?.takeIf { it.isNotBlank() }
                        ?: response.message()
                        ?: "Đăng ký thất bại"
                }
            } catch (e: Exception) {
                // Lỗi network hoặc exception khác
                _authError.value = "Lỗi kết nối: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Đăng nhập
     * @param email: Email của user
     * @param password: Password của user
     */
    fun login(email: String, password: String) {
        // Validate input chi tiết
        validateCredentials(email, password)?.let { errorMsg ->
            _authError.value = errorMsg
            return
        }
        
        // Sử dụng viewModelScope để launch coroutine
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _authError.value = null
                
                // Tạo request object
                val request = LoginRequest(email, password)
                
                // Gọi API
                val response = authApiService.login(request)

                // Kiểm tra response
                if (response.isSuccessful) {
                    val body = response.body()
                    val token = body?.token

                    if (!token.isNullOrBlank()) {
                        // Thành công - lưu token
                        _authSuccess.value = token
                    } else {
                        // Response ok nhưng không có token → coi như lỗi
                        _authError.value = body?.message ?: "Đăng nhập thất bại (không nhận được token)"
                    }
                } else {
                    // Lỗi từ server (4xx/5xx) - cố gắng đọc errorBody để hiển thị message rõ ràng hơn
                    val rawError = try {
                        response.errorBody()?.string()
                    } catch (e: Exception) {
                        null
                    }
                    _authError.value = rawError?.takeIf { it.isNotBlank() }
                        ?: response.message()
                        ?: "Đăng nhập thất bại"
                }
            } catch (e: Exception) {
                // Lỗi network hoặc exception khác
                _authError.value = "Lỗi kết nối: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Reset các LiveData về trạng thái ban đầu
     */
    fun resetStates() {
        _authSuccess.value = null
        _authError.value = null
        _isLoading.value = false
    }
}
