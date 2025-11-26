package com.hienpc.bmiapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.hienpc.bmiapp.R
import com.hienpc.bmiapp.data.local.AuthPreferences
import com.hienpc.bmiapp.data.local.TokenManager
import com.hienpc.bmiapp.databinding.ActivityRegisterBinding
import com.hienpc.bmiapp.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

/**
 * RegisterActivity - Activity cho màn hình đăng ký
 * 
 * Sử dụng ViewBinding để truy cập views
 * Sử dụng ViewModel để quản lý business logic
 * Observe LiveData từ ViewModel để cập nhật UI
 */
class RegisterActivity : AppCompatActivity() {
    
    // ViewBinding - tự động generate từ activity_register.xml
    private lateinit var binding: ActivityRegisterBinding

    // AuthPreferences - quản lý lưu token bằng DataStore
    private lateinit var authPreferences: AuthPreferences
    
    // ViewModel instance - sử dụng viewModels() delegate để tự động tạo
    private val viewModel: AuthViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Khởi tạo ViewBinding
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Khởi tạo AuthPreferences
        authPreferences = AuthPreferences(applicationContext)
        
        // Setup click listeners
        setupClickListeners()
        
        // Observe LiveData từ ViewModel
        observeViewModel()
    }
    
    /**
     * Setup các click listeners cho buttons và text views
     */
    private fun setupClickListeners() {
        // Register button click
        binding.buttonRegister.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()
            
            // Gọi ViewModel để thực hiện register
            viewModel.register(email, password)
        }
        
        // Login link click - chuyển sang LoginActivity
        binding.textViewLogin.setOnClickListener {
            // Quay lại LoginActivity
            finish()
        }
    }
    
    /**
     * Observe các LiveData từ ViewModel để cập nhật UI
     */
    private fun observeViewModel() {
        // Observe loading state - hiển thị/ẩn progress bar
        viewModel.isLoading.observe(this, Observer { isLoading ->
            binding.progressBar.visibility = if (isLoading) {
                android.view.View.VISIBLE
            } else {
                android.view.View.GONE
            }
            
            // Disable/enable button khi đang loading
            binding.buttonRegister.isEnabled = !isLoading
        })
        
        // Observe success state - khi đăng ký thành công
        viewModel.authSuccess.observe(this, Observer { token ->
            token?.let {
                // Đăng ký thành công
                Toast.makeText(this, "Đăng ký thành công! Token: $it", Toast.LENGTH_SHORT).show()

                // Lưu token vào DataStore (chuẩn bị cho Sprint 2 dùng thật)
                lifecycleScope.launch {
                    authPreferences.saveToken(it)
                }

                // Cập nhật TokenManager để interceptor có thể dùng ngay
                TokenManager.token = it

                // Navigate to MainActivity
                val intent = Intent(this, com.hienpc.bmiapp.ui.MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()

                // Reset state
                viewModel.resetStates()
            }
        })
        
        // Observe error state - khi có lỗi
        viewModel.authError.observe(this, Observer { errorMessage ->
            errorMessage?.let {
                // Hiển thị error message
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                
                // Reset state
                viewModel.resetStates()
            }
        })
    }
}

