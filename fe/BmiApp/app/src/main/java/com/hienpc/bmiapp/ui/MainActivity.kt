package com.hienpc.bmiapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.hienpc.bmiapp.R
import com.hienpc.bmiapp.ui.main.DashboardFragment
import com.hienpc.bmiapp.ui.main.LogContainerFragment
import com.hienpc.bmiapp.ui.chat.ChatFragment
import com.hienpc.bmiapp.ui.profile.ProfileFragment

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var toolbar: MaterialToolbar
    private var currentTabIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        bottomNavigation = findViewById(R.id.bottom_navigation)

        setSupportActionBar(toolbar)

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
}