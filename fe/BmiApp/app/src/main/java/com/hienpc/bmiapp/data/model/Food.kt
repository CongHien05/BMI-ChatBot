package com.hienpc.bmiapp.data.model

/**
 * FoodResponse - dữ liệu món ăn trả về từ API
 */
data class FoodResponse(
    val id: Long,
    val name: String,
    val caloriesPerUnit: Double,
    val unit: String // ví dụ: "g", "portion"
)

/**
 * ExerciseResponse - dữ liệu bài tập trả về từ API
 */
data class ExerciseResponse(
    val id: Long,
    val name: String,
    val caloriesBurnedPerHour: Double
)

/**
 * Request log ăn uống
 */
data class FoodLogRequest(
    val foodId: Long,
    val quantity: Double,
    val mealType: String? = null // breakfast/lunch/dinner/snack
)

/**
 * Request log luyện tập
 */
data class ExerciseLogRequest(
    val exerciseId: Long,
    val durationMinutes: Int
)

/**
 * DashboardSummary - dữ liệu tổng hợp cho dashboard
 */
data class DashboardSummary(
    val currentWeight: Double,
    val bmi: Double,
    val totalCaloriesToday: Double
)

/**
 * ProfileUpdateRequest - cập nhật mục tiêu và thông tin hồ sơ
 */
data class ProfileUpdateRequest(
    val targetWeight: Double? = null,
    val gender: String? = null,
    val dailyCalorieGoal: Int? = null
)