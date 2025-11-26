package com.hienpc.bmiapp.data.network

import com.hienpc.bmiapp.data.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

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

    @PUT("api/profile")
    suspend fun updateProfile(@Body request: ProfileUpdateRequest): Response<Unit>
}