package com.hienpc.bmiapp.ui.main.history

import com.hienpc.bmiapp.data.model.ExerciseLogHistoryResponse
import com.hienpc.bmiapp.data.model.FoodLogHistoryResponse

/**
 * Summary data for a day's logs
 */
data class DaySummary(
    val foodLogs: List<FoodLogHistoryResponse>,
    val exerciseLogs: List<ExerciseLogHistoryResponse>
) {
    val totalFoodCalories: Int
        get() = foodLogs.sumOf { it.totalCalories }

    val totalExerciseCalories: Int
        get() = exerciseLogs.sumOf { it.totalCaloriesBurned }

    val foodCount: Int
        get() = foodLogs.size

    val exerciseCount: Int
        get() = exerciseLogs.size

    val netCalories: Int
        get() = totalFoodCalories - totalExerciseCalories
}

