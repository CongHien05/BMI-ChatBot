package com.hienpc.bmiapp.data.network

import com.hienpc.bmiapp.data.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
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
    suspend fun getFoods(@Query("q") query: String? = null): Response<List<FoodResponse>>

    @GET("api/exercises")
    suspend fun getExercises(@Query("q") query: String? = null): Response<List<ExerciseResponse>>

    @POST("api/logs/food")
    suspend fun logFood(@Body request: FoodLogRequest): Response<Unit>

    @GET("api/logs/food/history")
    suspend fun getFoodLogHistory(
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): Response<List<com.hienpc.bmiapp.data.model.FoodLogHistoryResponse>>

    @PUT("api/logs/food/{logId}")
    suspend fun updateFoodLog(
        @Path("logId") logId: Int,
        @Body request: FoodLogRequest
    ): Response<Unit>

    @DELETE("api/logs/food/{logId}")
    suspend fun deleteFoodLog(@Path("logId") logId: Int): Response<Unit>

    @POST("api/logs/exercise")
    suspend fun logExercise(@Body request: ExerciseLogRequest): Response<Unit>

    @GET("api/logs/exercise/history")
    suspend fun getExerciseLogHistory(
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): Response<List<com.hienpc.bmiapp.data.model.ExerciseLogHistoryResponse>>

    @PUT("api/logs/exercise/{logId}")
    suspend fun updateExerciseLog(
        @Path("logId") logId: Int,
        @Body request: ExerciseLogRequest
    ): Response<Unit>

    @DELETE("api/logs/exercise/{logId}")
    suspend fun deleteExerciseLog(@Path("logId") logId: Int): Response<Unit>

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
    
    // ========== FAVORITES API ==========
    
    @GET("api/foods/favorites")
    suspend fun getFavoriteFoods(): Response<List<FoodResponse>>
    
    @POST("api/foods/favorites/{foodId}")
    suspend fun addFavoriteFood(@Path("foodId") foodId: Int): Response<Unit>
    
    @DELETE("api/foods/favorites/{foodId}")
    suspend fun removeFavoriteFood(@Path("foodId") foodId: Int): Response<Unit>
    
    @GET("api/foods/{foodId}/is-favorite")
    suspend fun isFoodFavorite(@Path("foodId") foodId: Int): Response<Boolean>
    
    @GET("api/exercises/favorites")
    suspend fun getFavoriteExercises(): Response<List<ExerciseResponse>>
    
    @POST("api/exercises/favorites/{exerciseId}")
    suspend fun addFavoriteExercise(@Path("exerciseId") exerciseId: Int): Response<Unit>
    
    @DELETE("api/exercises/favorites/{exerciseId}")
    suspend fun removeFavoriteExercise(@Path("exerciseId") exerciseId: Int): Response<Unit>
    
    @GET("api/exercises/{exerciseId}/is-favorite")
    suspend fun isExerciseFavorite(@Path("exerciseId") exerciseId: Int): Response<Boolean>
}