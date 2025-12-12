package com.hienpc.bmiapp.data.repository

import com.hienpc.bmiapp.data.model.*
import com.hienpc.bmiapp.data.network.ApiClient
import com.hienpc.bmiapp.data.network.ApiService

/**
 * UserRepository - gọi API dashboard & profile
 */
class UserRepository(
    private val apiService: ApiService = ApiClient.createService(ApiService::class.java)
) {
    suspend fun getDashboardSummary() = apiService.getDashboardSummary()
    
    suspend fun getWeeklySummary() = apiService.getWeeklySummary()
    
    suspend fun getMonthlySummary() = apiService.getMonthlySummary()
    
    suspend fun getTrendAnalysis() = apiService.getTrendAnalysis()

    suspend fun updateProfile(request: ProfileUpdateRequest) = apiService.updateProfile(request)
    
    suspend fun getAchievements() = apiService.getAchievements()
    
    suspend fun getStreak() = apiService.getStreak()
    
    suspend fun predictWeight(days: Int = 7) = apiService.predictWeight(days)
}