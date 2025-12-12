package com.hienpc.bmiapp.data.model

data class WeightPredictionResponse(
    val historicalData: List<WeightDataPoint>,
    val predictions: List<WeightDataPoint>,
    val metrics: ModelMetrics,
    val insights: String
)

data class WeightDataPoint(
    val date: String,  // YYYY-MM-DD
    val weightKg: Double
)

data class ModelMetrics(
    val rSquared: Double,       // R² coefficient
    val slope: Double,           // kg per day
    val intercept: Double,       // initial weight
    val dataPoints: Int,         // number of measurements
    val trend: String            // "DECREASING", "INCREASING", "STABLE"
)

