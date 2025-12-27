package com.hienpc.bmiapp.ui.main.history

import com.hienpc.bmiapp.data.model.ExerciseLogHistoryResponse
import com.hienpc.bmiapp.data.model.FoodLogHistoryResponse

/**
 * Sealed class to represent either a food log or exercise log
 */
sealed class LogItem {
    data class FoodLog(val log: FoodLogHistoryResponse) : LogItem()
    data class ExerciseLog(val log: ExerciseLogHistoryResponse) : LogItem()
    
    val isToday: Boolean
        get() = when (this) {
            is FoodLog -> {
                try {
                    val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
                    val date = inputFormat.parse(log.dateEaten)
                    if (date != null) {
                        val today = java.util.Calendar.getInstance()
                        val logDate = java.util.Calendar.getInstance()
                        logDate.time = date
                        today.get(java.util.Calendar.YEAR) == logDate.get(java.util.Calendar.YEAR) &&
                        today.get(java.util.Calendar.DAY_OF_YEAR) == logDate.get(java.util.Calendar.DAY_OF_YEAR)
                    } else false
                } catch (e: Exception) {
                    false
                }
            }
            is ExerciseLog -> {
                try {
                    val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
                    val date = inputFormat.parse(log.dateExercised)
                    if (date != null) {
                        val today = java.util.Calendar.getInstance()
                        val logDate = java.util.Calendar.getInstance()
                        logDate.time = date
                        today.get(java.util.Calendar.YEAR) == logDate.get(java.util.Calendar.YEAR) &&
                        today.get(java.util.Calendar.DAY_OF_YEAR) == logDate.get(java.util.Calendar.DAY_OF_YEAR)
                    } else false
                } catch (e: Exception) {
                    false
                }
            }
        }
}

