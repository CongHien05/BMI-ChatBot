package com.hienpc.bmiapp.ui.main

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.hienpc.bmiapp.ui.main.log.ExerciseLogFragment
import com.hienpc.bmiapp.ui.main.log.FoodLogFragment

class LogPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FoodLogFragment()
            else -> ExerciseLogFragment()
        }
    }
}


