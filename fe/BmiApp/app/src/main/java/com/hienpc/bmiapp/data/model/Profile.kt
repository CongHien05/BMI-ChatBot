package com.hienpc.bmiapp.data.model

import com.google.gson.annotations.SerializedName

/**
 * ProfileResponse - dữ liệu profile trả về từ API
 */
data class ProfileResponse(
    @SerializedName("dateOfBirth")
    val dateOfBirth: String?, // Format: "2000-01-01"
    
    @SerializedName("gender")
    val gender: String?,
    
    @SerializedName("goalType")
    val goalType: String?,
    
    @SerializedName("goalWeightKg")
    val goalWeightKg: Double?,
    
    @SerializedName("dailyCalorieGoal")
    val dailyCalorieGoal: Int?
)

