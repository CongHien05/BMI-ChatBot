package com.hienpc.bmiapp.data.repository

import com.hienpc.bmiapp.data.model.*
import com.hienpc.bmiapp.data.network.ApiClient
import com.hienpc.bmiapp.data.network.ApiService

/**
 * FoodRepository - chịu trách nhiệm gọi API liên quan đến
 *  - Danh sách món ăn & bài tập
 *  - Log ăn uống & luyện tập
 */
class FoodRepository(
    private val apiService: ApiService = ApiClient.createService(ApiService::class.java)
) {
    suspend fun getFoods(query: String? = null) = apiService.getFoods(query)

    suspend fun getExercises(query: String? = null) = apiService.getExercises(query)

    suspend fun logFood(request: FoodLogRequest) = apiService.logFood(request)

    suspend fun getFoodLogHistory(from: String? = null, to: String? = null) = 
        apiService.getFoodLogHistory(from, to)

    suspend fun updateFoodLog(logId: Int, request: FoodLogRequest) = 
        apiService.updateFoodLog(logId, request)

    suspend fun deleteFoodLog(logId: Int) = apiService.deleteFoodLog(logId)

    suspend fun logExercise(request: ExerciseLogRequest) = apiService.logExercise(request)

    suspend fun getExerciseLogHistory(from: String? = null, to: String? = null) = 
        apiService.getExerciseLogHistory(from, to)

    suspend fun updateExerciseLog(logId: Int, request: ExerciseLogRequest) = 
        apiService.updateExerciseLog(logId, request)

    suspend fun deleteExerciseLog(logId: Int) = apiService.deleteExerciseLog(logId)
    
    suspend fun getFoodRecommendations(limit: Int = 10) = apiService.getFoodRecommendations(limit)
    
    suspend fun getExerciseRecommendations(limit: Int = 10) = apiService.getExerciseRecommendations(limit)
    
    // ========== FAVORITES METHODS ==========
    
    suspend fun getFavoriteFoods() = apiService.getFavoriteFoods()
    
    suspend fun addFavoriteFood(foodId: Int) = apiService.addFavoriteFood(foodId)
    
    suspend fun removeFavoriteFood(foodId: Int) = apiService.removeFavoriteFood(foodId)
    
    suspend fun isFoodFavorite(foodId: Int) = apiService.isFoodFavorite(foodId)
    
    suspend fun getFavoriteExercises() = apiService.getFavoriteExercises()
    
    suspend fun addFavoriteExercise(exerciseId: Int) = apiService.addFavoriteExercise(exerciseId)
    
    suspend fun removeFavoriteExercise(exerciseId: Int) = apiService.removeFavoriteExercise(exerciseId)
    
    suspend fun isExerciseFavorite(exerciseId: Int) = apiService.isExerciseFavorite(exerciseId)
}