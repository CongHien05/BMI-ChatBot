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
 * Food log history response
 */
data class FoodLogHistoryResponse(
    @SerializedName("logId")
    val logId: Int,
    @SerializedName("foodId")
    val foodId: Int?,
    @SerializedName("foodName")
    val foodName: String,
    @SerializedName("quantity")
    val quantity: Double,
    @SerializedName("unit")
    val unit: String,
    @SerializedName("caloriesPerUnit")
    val caloriesPerUnit: Int,
    @SerializedName("totalCalories")
    val totalCalories: Int,
    @SerializedName("mealType")
    val mealType: String? = null, // BREAKFAST, LUNCH, DINNER, SNACK
    @SerializedName("dateEaten")
    val dateEaten: String // ISO date time string
)

/**
 * Exercise log history response
 */
data class ExerciseLogHistoryResponse(
    @SerializedName("logId")
    val logId: Int,
    @SerializedName("exerciseId")
    val exerciseId: Int?,
    @SerializedName("exerciseName")
    val exerciseName: String,
    @SerializedName("duration")
    val duration: Double, // minutes
    @SerializedName("caloriesPerMinute")
    val caloriesPerMinute: Int,
    @SerializedName("totalCaloriesBurned")
    val totalCaloriesBurned: Int,
    @SerializedName("dateExercised")
    val dateExercised: String // ISO date time string
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