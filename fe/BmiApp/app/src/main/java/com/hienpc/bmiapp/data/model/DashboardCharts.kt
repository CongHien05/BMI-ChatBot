package com.hienpc.bmiapp.data.model

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*

/**
 * Models cho dashboard charts data
 */

data class DailySummaryItem(
    @SerializedName("date")
    val date: String, // Format: "2025-12-12"
    
    @SerializedName("weight")
    val weight: Double?,
    
    @SerializedName("totalCalories")
    val totalCalories: Int,
    
    @SerializedName("caloriesBurned")
    val caloriesBurned: Int,
    
    @SerializedName("netCalories")
    val netCalories: Int,
    
    @SerializedName("foodLogsCount")
    val foodLogsCount: Int,
    
    @SerializedName("exerciseLogsCount")
    val exerciseLogsCount: Int
) {
    // Helper để parse date (API 24+ compatible)
    fun getDate(): Date? {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            format.parse(date)
        } catch (e: Exception) {
            null
        }
    }
    
    // Format date to readable string
    fun getFormattedDate(pattern: String = "dd/MM/yyyy"): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat(pattern, Locale.getDefault())
            val date = inputFormat.parse(date)
            if (date != null) outputFormat.format(date) else this.date
        } catch (e: Exception) {
            this.date
        }
    }
}

data class WeeklySummaryResponse(
    @SerializedName("dailySummaries")
    val dailySummaries: List<DailySummaryItem>,
    
    @SerializedName("averageCalories")
    val averageCalories: Double,
    
    @SerializedName("averageWeight")
    val averageWeight: Double?,
    
    @SerializedName("weightChange")
    val weightChange: Double?,
    
    @SerializedName("totalFoodLogs")
    val totalFoodLogs: Int,
    
    @SerializedName("totalExerciseLogs")
    val totalExerciseLogs: Int
)

data class MonthlySummaryResponse(
    @SerializedName("dailySummaries")
    val dailySummaries: List<DailySummaryItem>,
    
    @SerializedName("averageCalories")
    val averageCalories: Double,
    
    @SerializedName("averageWeight")
    val averageWeight: Double?,
    
    @SerializedName("weightChange")
    val weightChange: Double?,
    
    @SerializedName("totalFoodLogs")
    val totalFoodLogs: Int,
    
    @SerializedName("totalExerciseLogs")
    val totalExerciseLogs: Int,
    
    @SerializedName("bmiChange")
    val bmiChange: Double?
)

data class TrendAnalysisResponse(
    @SerializedName("weightTrend")
    val weightTrend: String, // "LOSING" | "GAINING" | "STABLE"
    
    @SerializedName("weightChangeRate")
    val weightChangeRate: Double, // kg/week
    
    @SerializedName("caloriesTrend")
    val caloriesTrend: String,
    
    @SerializedName("avgDailyCalories")
    val avgDailyCalories: Double,
    
    @SerializedName("activityTrend")
    val activityTrend: String,
    
    @SerializedName("avgWeeklyExercises")
    val avgWeeklyExercises: Double,
    
    @SerializedName("insight")
    val insight: String,
    
    @SerializedName("onTrack")
    val onTrack: Boolean,
    
    @SerializedName("daysToGoal")
    val daysToGoal: Int?
)

