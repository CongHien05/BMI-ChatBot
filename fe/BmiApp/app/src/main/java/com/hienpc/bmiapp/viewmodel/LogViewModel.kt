package com.hienpc.bmiapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hienpc.bmiapp.data.model.*
import com.hienpc.bmiapp.data.repository.FoodRepository
import com.hienpc.bmiapp.utils.UiState
import kotlinx.coroutines.launch

/**
 * LogViewModel - quản lý:
 *  - Danh sách Food/Exercise
 *  - Gửi log food/exercise
 */
class LogViewModel(
    private val repository: FoodRepository = FoodRepository()
) : ViewModel() {

    private val _foodsState = MutableLiveData<UiState<List<FoodResponse>>>(UiState.Idle)
    val foodsState: LiveData<UiState<List<FoodResponse>>> = _foodsState

    private val _exercisesState = MutableLiveData<UiState<List<ExerciseResponse>>>(UiState.Idle)
    val exercisesState: LiveData<UiState<List<ExerciseResponse>>> = _exercisesState

    private val _logFoodState = MutableLiveData<UiState<Unit>>(UiState.Idle)
    val logFoodState: LiveData<UiState<Unit>> = _logFoodState

    private val _logExerciseState = MutableLiveData<UiState<Unit>>(UiState.Idle)
    val logExerciseState: LiveData<UiState<Unit>> = _logExerciseState
    
    private val _foodRecommendationsState = MutableLiveData<UiState<RecommendationResponse>>(UiState.Idle)
    val foodRecommendationsState: LiveData<UiState<RecommendationResponse>> = _foodRecommendationsState
    
    private val _exerciseRecommendationsState = MutableLiveData<UiState<RecommendationResponse>>(UiState.Idle)
    val exerciseRecommendationsState: LiveData<UiState<RecommendationResponse>> = _exerciseRecommendationsState

    fun loadFoods(query: String? = null) {
        viewModelScope.launch {
            _foodsState.value = UiState.Loading
            try {
                val response = repository.getFoods(query)
                if (response.isSuccessful) {
                    val data = response.body().orEmpty()
                    _foodsState.value = UiState.Success(data)
                } else {
                    _foodsState.value = UiState.Error(
                        response.message().ifBlank { "Không tải được danh sách món ăn" }
                    )
                }
            } catch (e: Exception) {
                _foodsState.value = UiState.Error("Lỗi kết nối: ${e.message}")
            }
        }
    }

    fun loadExercises(query: String? = null) {
        viewModelScope.launch {
            _exercisesState.value = UiState.Loading
            try {
                val response = repository.getExercises(query)
                if (response.isSuccessful) {
                    val data = response.body().orEmpty()
                    _exercisesState.value = UiState.Success(data)
                } else {
                    _exercisesState.value = UiState.Error(
                        response.message().ifBlank { "Không tải được danh sách bài tập" }
                    )
                }
            } catch (e: Exception) {
                _exercisesState.value = UiState.Error("Lỗi kết nối: ${e.message}")
            }
        }
    }

    fun logFood(foodId: Int, quantity: Double, mealType: String?) {
        viewModelScope.launch {
            _logFoodState.value = UiState.Loading
            try {
                val request = FoodLogRequest(foodId = foodId, quantity = quantity, mealType = mealType)
                val response = repository.logFood(request)
                if (response.isSuccessful) {
                    _logFoodState.value = UiState.Success(Unit)
                } else {
                    _logFoodState.value = UiState.Error(
                        response.message().ifBlank { "Log món ăn thất bại" }
                    )
                }
            } catch (e: Exception) {
                _logFoodState.value = UiState.Error("Lỗi kết nối: ${e.message}")
            }
        }
    }

    fun logExercise(exerciseId: Int, durationMinutes: Int) {
        viewModelScope.launch {
            _logExerciseState.value = UiState.Loading
            try {
                val request = ExerciseLogRequest(exerciseId = exerciseId, durationMinutes = durationMinutes)
                val response = repository.logExercise(request)
                if (response.isSuccessful) {
                    _logExerciseState.value = UiState.Success(Unit)
                } else {
                    _logExerciseState.value = UiState.Error(
                        response.message().ifBlank { "Log bài tập thất bại" }
                    )
                }
            } catch (e: Exception) {
                _logExerciseState.value = UiState.Error("Lỗi kết nối: ${e.message}")
            }
        }
    }

    fun resetLogStates() {
        _logFoodState.value = UiState.Idle
        _logExerciseState.value = UiState.Idle
    }
    
    fun loadFoodRecommendations(limit: Int = 10) {
        viewModelScope.launch {
            _foodRecommendationsState.value = UiState.Loading
            try {
                val response = repository.getFoodRecommendations(limit)
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) {
                        _foodRecommendationsState.value = UiState.Success(data)
                    } else {
                        _foodRecommendationsState.value = UiState.Error("Không có dữ liệu gợi ý")
                    }
                } else {
                    _foodRecommendationsState.value = UiState.Error(
                        response.message().ifBlank { "Không tải được gợi ý món ăn" }
                    )
                }
            } catch (e: Exception) {
                _foodRecommendationsState.value = UiState.Error("Lỗi kết nối: ${e.message}")
            }
        }
    }
    
    fun loadExerciseRecommendations(limit: Int = 10) {
        viewModelScope.launch {
            _exerciseRecommendationsState.value = UiState.Loading
            try {
                val response = repository.getExerciseRecommendations(limit)
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) {
                        _exerciseRecommendationsState.value = UiState.Success(data)
                    } else {
                        _exerciseRecommendationsState.value = UiState.Error("Không có dữ liệu gợi ý")
                    }
                } else {
                    _exerciseRecommendationsState.value = UiState.Error(
                        response.message().ifBlank { "Không tải được gợi ý bài tập" }
                    )
                }
            } catch (e: Exception) {
                _exerciseRecommendationsState.value = UiState.Error("Lỗi kết nối: ${e.message}")
            }
        }
    }
}
