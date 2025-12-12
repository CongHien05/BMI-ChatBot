package com.hienpc.bmiapp.utils

import android.graphics.Color
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.hienpc.bmiapp.data.model.DailySummaryItem
import java.text.SimpleDateFormat
import java.util.*

/**
 * Helper class để setup MPAndroidChart
 */
object ChartHelper {

    /**
     * Setup weight line chart
     */
    fun setupWeightChart(
        chart: LineChart,
        dailySummaries: List<DailySummaryItem>
    ) {
        val entries = mutableListOf<Entry>()
        val labels = mutableListOf<String>()
        
        dailySummaries.forEachIndexed { index, summary ->
            summary.weight?.let { weight ->
                entries.add(Entry(index.toFloat(), weight.toFloat()))
            }
            // Format date to "dd/MM" (API 24 compatible)
            try {
                // Parse "2025-12-12" format
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
                val date = inputFormat.parse(summary.date)
                labels.add(if (date != null) outputFormat.format(date) else summary.date.substring(5))
            } catch (e: Exception) {
                // Fallback: extract "MM-dd" part
                labels.add(summary.date.substring(5))
            }
        }
        
        if (entries.isEmpty()) {
            chart.clear()
            chart.setNoDataText("Chưa có dữ liệu cân nặng")
            return
        }
        
        val dataSet = LineDataSet(entries, "Cân nặng (kg)").apply {
            color = Color.parseColor("#4CAF50")
            lineWidth = 3f
            setCircleColor(Color.parseColor("#4CAF50"))
            circleRadius = 5f
            circleHoleRadius = 3f
            setDrawValues(true)
            valueTextSize = 10f
            valueTextColor = Color.BLACK
            mode = LineDataSet.Mode.CUBIC_BEZIER // Smooth curve
            setDrawFilled(true)
            fillColor = Color.parseColor("#4CAF50")
            fillAlpha = 50
        }
        
        val lineData = LineData(dataSet)
        chart.data = lineData
        
        // Setup X axis
        chart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            granularity = 1f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return labels.getOrNull(value.toInt()) ?: ""
                }
            }
        }
        
        // Setup Y axis
        chart.axisLeft.apply {
            setDrawGridLines(true)
            gridColor = Color.LTGRAY
        }
        chart.axisRight.isEnabled = false
        
        // General chart settings
        chart.apply {
            description.isEnabled = false
            legend.isEnabled = true
            setTouchEnabled(true)
            setScaleEnabled(false)
            setPinchZoom(false)
            animateX(800)
            invalidate()
        }
    }

    /**
     * Setup calories bar chart
     */
    fun setupCaloriesChart(
        chart: BarChart,
        dailySummaries: List<DailySummaryItem>
    ) {
        val entries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()
        
        dailySummaries.forEachIndexed { index, summary ->
            entries.add(BarEntry(index.toFloat(), summary.totalCalories.toFloat()))
            
            // Format date to "dd/MM" (API 24 compatible)
            try {
                // Parse "2025-12-12" format
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
                val date = inputFormat.parse(summary.date)
                labels.add(if (date != null) outputFormat.format(date) else summary.date.substring(5))
            } catch (e: Exception) {
                // Fallback: extract "MM-dd" part
                labels.add(summary.date.substring(5))
            }
        }
        
        if (entries.isEmpty()) {
            chart.clear()
            chart.setNoDataText("Chưa có dữ liệu calories")
            return
        }
        
        val dataSet = BarDataSet(entries, "Calories (kcal)").apply {
            color = Color.parseColor("#FF9800")
            valueTextSize = 10f
            valueTextColor = Color.BLACK
            setDrawValues(true)
        }
        
        val barData = BarData(dataSet)
        barData.barWidth = 0.7f
        chart.data = barData
        
        // Setup X axis
        chart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            granularity = 1f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return labels.getOrNull(value.toInt()) ?: ""
                }
            }
        }
        
        // Setup Y axis
        chart.axisLeft.apply {
            setDrawGridLines(true)
            gridColor = Color.LTGRAY
            axisMinimum = 0f
        }
        chart.axisRight.isEnabled = false
        
        // General chart settings
        chart.apply {
            description.isEnabled = false
            legend.isEnabled = true
            setTouchEnabled(true)
            setScaleEnabled(false)
            setPinchZoom(false)
            animateY(800)
            invalidate()
        }
    }
}

