package com.hienpc.bmiapp.ui.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hienpc.bmiapp.R
import com.hienpc.bmiapp.data.model.Achievement
import com.hienpc.bmiapp.databinding.ItemAchievementBinding
import java.text.SimpleDateFormat
import java.util.*

class AchievementAdapter : ListAdapter<Achievement, AchievementAdapter.ViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Achievement>() {
            override fun areItemsTheSame(oldItem: Achievement, newItem: Achievement): Boolean {
                return oldItem.achievementType == newItem.achievementType
            }

            override fun areContentsTheSame(oldItem: Achievement, newItem: Achievement): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAchievementBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemAchievementBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(achievement: Achievement) {
            binding.textAchievementTitle.text = achievement.title
            binding.textAchievementDescription.text = achievement.description

            // Set icon (using emoji from title for now, can be replaced with actual icons)
            val iconResId = getIconResource(achievement.iconName)
            binding.imageAchievementIcon.setImageResource(iconResId)

            if (achievement.isUnlocked) {
                // Unlocked state
                binding.viewLockedOverlay.visibility = View.GONE
                binding.imageLockIcon.visibility = View.GONE
                binding.textAchievedDate.visibility = View.VISIBLE
                
                // Format achieved date
                achievement.achievedAt?.let { dateStr ->
                    try {
                        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val date = inputFormat.parse(dateStr)
                        binding.textAchievedDate.text = "Đạt được: ${date?.let { outputFormat.format(it) }}"
                    } catch (e: Exception) {
                        binding.textAchievedDate.text = "Đạt được: $dateStr"
                    }
                }
            } else {
                // Locked state
                binding.viewLockedOverlay.visibility = View.VISIBLE
                binding.imageLockIcon.visibility = View.VISIBLE
                binding.textAchievedDate.visibility = View.GONE
            }
        }

        private fun getIconResource(iconName: String): Int {
            // Map icon names to drawable resources
            // For now, using default Android icons
            return when (iconName) {
                "ic_achievement_first" -> android.R.drawable.star_big_on
                "ic_achievement_exercise" -> android.R.drawable.ic_menu_compass
                "ic_achievement_7day" -> android.R.drawable.star_on
                "ic_achievement_30day" -> android.R.drawable.btn_star_big_on
                "ic_achievement_100day" -> android.R.drawable.btn_star
                "ic_achievement_goal" -> android.R.drawable.ic_menu_mylocation
                "ic_achievement_50logs" -> android.R.drawable.ic_menu_edit
                "ic_achievement_100logs" -> android.R.drawable.ic_menu_save
                "ic_achievement_early" -> android.R.drawable.ic_menu_day
                "ic_achievement_night" -> android.R.drawable.ic_lock_idle_alarm
                else -> android.R.drawable.star_big_on
            }
        }
    }
}

