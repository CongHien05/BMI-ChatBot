package com.hienpc.bmiapp.data.model

data class Achievement(
    val achievementId: Int?,
    val achievementType: String,
    val title: String,
    val description: String,
    val iconName: String,
    val achievedAt: String?,
    val isUnlocked: Boolean
)

data class Streak(
    val currentStreak: Int,
    val longestStreak: Int,
    val lastLogDate: String?,
    val message: String,
    val isActive: Boolean
)

