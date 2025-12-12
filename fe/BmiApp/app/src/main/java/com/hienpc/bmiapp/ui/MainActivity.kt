package com.hienpc.bmiapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.hienpc.bmiapp.R
import com.hienpc.bmiapp.data.local.AuthPreferences
import com.hienpc.bmiapp.data.local.TokenManager
import com.hienpc.bmiapp.ui.auth.LoginActivity
import com.hienpc.bmiapp.ui.chat.ChatFragment
import com.hienpc.bmiapp.ui.main.DashboardFragment
import com.hienpc.bmiapp.ui.main.LogContainerFragment
import com.hienpc.bmiapp.ui.profile.ProfileFragment
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var toolbar: MaterialToolbar
    private lateinit var authPreferences: AuthPreferences
    private var currentTabIndex = 0
    
    // Double tap to exit
    private var backPressedTime: Long = 0
    private var backToast: Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Khởi tạo AuthPreferences
        authPreferences = AuthPreferences(applicationContext)
        
        // Kiểm tra auto-login
        lifecycleScope.launch {
            val token = authPreferences.getToken()
            if (token.isNullOrBlank()) {
                // Chưa đăng nhập -> chuyển sang LoginActivity
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                return@launch
            }
            
            // Đã có token -> cập nhật TokenManager và tiếp tục
            TokenManager.token = token
            initializeUI(savedInstanceState)
        }
    }
    
    private fun initializeUI(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        bottomNavigation = findViewById(R.id.bottom_navigation)

        setSupportActionBar(toolbar)
        
        // Setup back press handler
        setupBackPressHandler()

        // Mặc định hiển thị Dashboard
        if (savedInstanceState == null) {
            openFragment(DashboardFragment(), "Dashboard", 0)
        }

        bottomNavigation.setOnItemSelectedListener { item ->
            val newTabIndex = when (item.itemId) {
                R.id.nav_dashboard -> {
                    openFragment(DashboardFragment(), "Dashboard", 0)
                    0
                }
                R.id.nav_log -> {
                    openFragment(LogContainerFragment(), "Log", 1)
                    1
                }
                R.id.nav_chat -> {
                    openFragment(ChatFragment(), "Chat", 2)
                    2
                }
                R.id.nav_profile -> {
                    openFragment(ProfileFragment(), "Profile", 3)
                    3
                }
                else -> currentTabIndex
            }
            currentTabIndex = newTabIndex
            true
        }
    }

    private fun openFragment(fragment: Fragment, title: String, newTabIndex: Int) {
        val transaction = supportFragmentManager.beginTransaction()
        
        // Xác định hướng animation dựa trên tab index
        if (newTabIndex > currentTabIndex) {
            // Chuyển sang tab bên phải -> slide in từ phải
            transaction.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )
        } else if (newTabIndex < currentTabIndex) {
            // Chuyển sang tab bên trái -> slide in từ trái
            transaction.setCustomAnimations(
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
        }
        
        transaction.replace(R.id.nav_host_fragment, fragment)
            .commit()
        toolbar.title = title
    }
    
    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Check if there's a fragment in back stack
                if (supportFragmentManager.backStackEntryCount > 0) {
                    // Pop back stack (navigate back in nested fragments)
                    supportFragmentManager.popBackStack()
                } else {
                    // At root level - show exit confirmation
                    showExitConfirmationDialog()
                }
            }
        })
    }
    
    private fun showExitConfirmationDialog() {
        val messages = listOf(
            getString(R.string.exit_message_1),
            getString(R.string.exit_message_2),
            getString(R.string.exit_message_3),
            getString(R.string.exit_message_4),
            getString(R.string.exit_message_5)
        )
        val randomMessage = messages.random()
        
        AlertDialog.Builder(this, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
            .setTitle(getString(R.string.exit_dialog_title))
            .setMessage(randomMessage)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(getString(R.string.exit_button_stay)) { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.exit_button_exit)) { _, _ ->
                finish()
            }
            .setCancelable(true)
            .show()
    }
    
    // Alternative: Double tap to exit pattern (commented out)
    /*
    private fun handleBackPressWithDoubleTap() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            // Double tap detected - exit app
            backToast?.cancel()
            finish()
        } else {
            // First tap - show toast
            backToast = Toast.makeText(
                this,
                "Nhấn Back một lần nữa để thoát",
                Toast.LENGTH_SHORT
            )
            backToast?.show()
        }
        backPressedTime = System.currentTimeMillis()
    }
    */
}