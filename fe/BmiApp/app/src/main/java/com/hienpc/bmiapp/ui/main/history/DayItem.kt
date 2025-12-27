package com.hienpc.bmiapp.ui.main.history

import java.util.Date

/**
 * Represents a day in the calendar
 */
data class DayItem(
    val date: Date,
    val dayOfMonth: Int,
    val dayOfWeek: String, // "T2", "T3", etc.
    val isToday: Boolean = false,
    val isSelected: Boolean = false
)

