package com.hienpc.bmiapp.data.model

import com.google.gson.annotations.SerializedName

/**
 * FoodResponse - dữ liệu món ăn trả về từ API
 */
data class FoodResponse(
    @SerializedName("foodId")
    val id: Int,
    @SerializedName("foodName")
    val name: String,
    @SerializedName("caloriesPerUnit")
    val caloriesPerUnit: Int,
    @SerializedName("servingUnit")
    val unit: String // ví dụ: "g", "portion"
)

/**
 * ExerciseResponse - dữ liệu bài tập trả về từ API
 */
data class ExerciseResponse(
    @SerializedName("exerciseId")
    val id: Int,
    @SerializedName("exerciseName")
    val name: String,
    val caloriesBurnedPerHour: Int
)

/**
 * Request log ăn uống
 */
data class FoodLogRequest(
    val foodId: Int,
    val quantity: Double,
    val mealType: String? = null // breakfast/lunch/dinner/snack
)

/**
 * Request log luyện tập
 */
data class ExerciseLogRequest(
    val exerciseId: Int,
    val durationMinutes: Int
)

/**
 * DashboardSummary - dữ liệu tổng hợp cho dashboard
 */
data class DashboardSummary(
    val currentWeight: Double?,
    val bmi: Double?,
    val totalCaloriesToday: Int
)

/**
 * ProfileUpdateRequest - cập nhật mục tiêu và thông tin hồ sơ
 */
data class ProfileUpdateRequest(
    @SerializedName("goalWeightKg")
    val goalWeightKg: Double? = null,
    val gender: String? = null,
    val goalType: String? = null,
    val dailyCalorieGoal: Int? = null
)