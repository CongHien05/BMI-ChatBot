package com.hienpc.bmiapp.data.network

import com.hienpc.bmiapp.data.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

/**
 * ApiService - các API được sử dụng ở Sprint 2:
 *  - Food/Exercise list
 *  - Log food/exercise
 *  - Dashboard summary
 *  - Profile update
 */
interface ApiService {

    @GET("api/foods")
    suspend fun getFoods(): Response<List<FoodResponse>>

    @GET("api/exercises")
    suspend fun getExercises(): Response<List<ExerciseResponse>>

    @POST("api/logs/food")
    suspend fun logFood(@Body request: FoodLogRequest): Response<Unit>

    @POST("api/logs/exercise")
    suspend fun logExercise(@Body request: ExerciseLogRequest): Response<Unit>

    @GET("api/dashboard/summary")
    suspend fun getDashboardSummary(): Response<DashboardSummary>
    
    @GET("api/dashboard/weekly-summary")
    suspend fun getWeeklySummary(): Response<WeeklySummaryResponse>
    
    @GET("api/dashboard/monthly-summary")
    suspend fun getMonthlySummary(): Response<MonthlySummaryResponse>
    
    @GET("api/dashboard/trends")
    suspend fun getTrendAnalysis(): Response<TrendAnalysisResponse>

    @GET("api/profile")
    suspend fun getProfile(): Response<ProfileResponse>
    
    @PUT("api/profile")
    suspend fun updateProfile(@Body request: ProfileUpdateRequest): Response<Unit>

    @POST("api/measurements")
    suspend fun addMeasurement(@Body request: MeasurementRequest): Response<MeasurementResponse>

    @GET("api/measurements/latest")
    suspend fun getLatestMeasurement(): Response<MeasurementResponse>
    
    @GET("api/profile/achievements")
    suspend fun getAchievements(): Response<List<Achievement>>
    
    @GET("api/profile/streak")
    suspend fun getStreak(): Response<Streak>
    
    @GET("api/predictions/weight")
    suspend fun predictWeight(@Query("days") days: Int = 7): Response<WeightPredictionResponse>
    
    @GET("api/recommendations/foods")
    suspend fun getFoodRecommendations(@Query("limit") limit: Int = 10): Response<RecommendationResponse>
    
    @GET("api/recommendations/exercises")
    suspend fun getExerciseRecommendations(@Query("limit") limit: Int = 10): Response<RecommendationResponse>
}