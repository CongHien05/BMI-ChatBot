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
    suspend fun getFoods() = apiService.getFoods()

    suspend fun getExercises() = apiService.getExercises()

    suspend fun logFood(request: FoodLogRequest) = apiService.logFood(request)

    suspend fun logExercise(request: ExerciseLogRequest) = apiService.logExercise(request)
}