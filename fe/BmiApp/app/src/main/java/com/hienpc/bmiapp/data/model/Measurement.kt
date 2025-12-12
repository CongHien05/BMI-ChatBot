package com.hienpc.bmiapp.data.model

/**
 * MeasurementRequest - payload gửi cân nặng/chiều cao hiện tại lên API.
 */
data class MeasurementRequest(
    val weightKg: Double,
    val heightCm: Double? = null
)

/**
 * MeasurementResponse - dữ liệu cân nặng/chiều cao mới nhất trả về từ API.
 */
data class MeasurementResponse(
    val weightKg: Double?,
    val heightCm: Double?,
    val dateRecorded: String?
)

