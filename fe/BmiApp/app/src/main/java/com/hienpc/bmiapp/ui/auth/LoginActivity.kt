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
import com.hienpc.bmiapp.databinding.ActivityLoginBinding
import com.hienpc.bmiapp.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

/**
 * LoginActivity - Activity cho màn hình đăng nhập
 * 
 * Sử dụng ViewBinding để truy cập views
 * Sử dụng ViewModel để quản lý business logic
 * Observe LiveData từ ViewModel để cập nhật UI
 */
class LoginActivity : AppCompatActivity() {
    
    // ViewBinding - tự động generate từ activity_login.xml
    private lateinit var binding: ActivityLoginBinding

    // AuthPreferences - quản lý lưu token bằng DataStore
    private lateinit var authPreferences: AuthPreferences
    
    // ViewModel instance - sử dụng viewModels() delegate để tự động tạo
    private val viewModel: AuthViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Khởi tạo ViewBinding
        binding = ActivityLoginBinding.inflate(layoutInflater)
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
        // Login button click
        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()
            
            // Gọi ViewModel để thực hiện login
            viewModel.login(email, password)
        }
        
        // Register link click - chuyển sang RegisterActivity
        binding.textViewRegister.setOnClickListener {
            Intent(this, RegisterActivity::class.java).also { 
                startActivity(it) 
            }
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
            binding.buttonLogin.isEnabled = !isLoading
        })
        
        // Observe success state - khi đăng nhập thành công
        viewModel.authSuccess.observe(this, Observer { token ->
            token?.let {
                // Đăng nhập thành công
                Toast.makeText(this, "Đăng nhập thành công! Token: $it", Toast.LENGTH_SHORT).show()

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

