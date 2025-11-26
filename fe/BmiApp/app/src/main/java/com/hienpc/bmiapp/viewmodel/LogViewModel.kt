package com.hienpc.bmiapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hienpc.bmiapp.data.model.ExerciseLogRequest
import com.hienpc.bmiapp.data.model.ExerciseResponse
import com.hienpc.bmiapp.data.model.FoodLogRequest
import com.hienpc.bmiapp.data.model.FoodResponse
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

    fun loadFoods() {
        viewModelScope.launch {
            _foodsState.value = UiState.Loading
            try {
                val response = repository.getFoods()
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

    fun loadExercises() {
        viewModelScope.launch {
            _exercisesState.value = UiState.Loading
            try {
                val response = repository.getExercises()
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

    fun logFood(foodId: Long, quantity: Double, mealType: String?) {
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

    fun logExercise(exerciseId: Long, durationMinutes: Int) {
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
}
