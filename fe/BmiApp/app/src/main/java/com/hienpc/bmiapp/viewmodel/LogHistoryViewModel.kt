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
 * LogHistoryViewModel - quản lý lịch sử logs (Food + Exercise)
 */
class LogHistoryViewModel(
    private val repository: FoodRepository = FoodRepository()
) : ViewModel() {

    private val _foodHistoryState = MutableLiveData<UiState<List<FoodLogHistoryResponse>>>(UiState.Idle)
    val foodHistoryState: LiveData<UiState<List<FoodLogHistoryResponse>>> = _foodHistoryState

    private val _exerciseHistoryState = MutableLiveData<UiState<List<ExerciseLogHistoryResponse>>>(UiState.Idle)
    val exerciseHistoryState: LiveData<UiState<List<ExerciseLogHistoryResponse>>> = _exerciseHistoryState

    private val _updateFoodLogState = MutableLiveData<UiState<Unit>>(UiState.Idle)
    val updateFoodLogState: LiveData<UiState<Unit>> = _updateFoodLogState

    private val _deleteFoodLogState = MutableLiveData<UiState<Unit>>(UiState.Idle)
    val deleteFoodLogState: LiveData<UiState<Unit>> = _deleteFoodLogState

    private val _updateExerciseLogState = MutableLiveData<UiState<Unit>>(UiState.Idle)
    val updateExerciseLogState: LiveData<UiState<Unit>> = _updateExerciseLogState

    private val _deleteExerciseLogState = MutableLiveData<UiState<Unit>>(UiState.Idle)
    val deleteExerciseLogState: LiveData<UiState<Unit>> = _deleteExerciseLogState

    fun loadFoodHistory(fromStr: String? = null, toStr: String? = null) {
        viewModelScope.launch {
            _foodHistoryState.value = UiState.Loading
            try {
                val response = repository.getFoodLogHistory(fromStr, toStr)
                if (response.isSuccessful && response.body() != null) {
                    _foodHistoryState.value = UiState.Success(response.body()!!)
                } else {
                    _foodHistoryState.value = UiState.Error("Không thể tải lịch sử bữa ăn")
                }
            } catch (e: Exception) {
                _foodHistoryState.value = UiState.Error("Lỗi: ${e.message}")
            }
        }
    }

    fun loadExerciseHistory(fromStr: String? = null, toStr: String? = null) {
        viewModelScope.launch {
            _exerciseHistoryState.value = UiState.Loading
            try {
                val response = repository.getExerciseLogHistory(fromStr, toStr)
                if (response.isSuccessful && response.body() != null) {
                    _exerciseHistoryState.value = UiState.Success(response.body()!!)
                } else {
                    _exerciseHistoryState.value = UiState.Error("Không thể tải lịch sử bài tập")
                }
            } catch (e: Exception) {
                _exerciseHistoryState.value = UiState.Error("Lỗi: ${e.message}")
            }
        }
    }

    fun updateFoodLog(logId: Int, foodId: Int, quantity: Double, mealType: String) {
        viewModelScope.launch {
            _updateFoodLogState.value = UiState.Loading
            try {
                val request = FoodLogRequest(foodId, quantity, mealType)
                val response = repository.updateFoodLog(logId, request)
                if (response.isSuccessful) {
                    _updateFoodLogState.value = UiState.Success(Unit)
                    // Reload history after update
                    loadFoodHistory()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Không thể cập nhật log"
                    _updateFoodLogState.value = UiState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _updateFoodLogState.value = UiState.Error("Lỗi: ${e.message}")
            }
        }
    }

    fun deleteFoodLog(logId: Int) {
        viewModelScope.launch {
            _deleteFoodLogState.value = UiState.Loading
            try {
                val response = repository.deleteFoodLog(logId)
                if (response.isSuccessful) {
                    _deleteFoodLogState.value = UiState.Success(Unit)
                    // Reload history after delete
                    loadFoodHistory()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Không thể xóa log"
                    _deleteFoodLogState.value = UiState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _deleteFoodLogState.value = UiState.Error("Lỗi: ${e.message}")
            }
        }
    }

    fun updateExerciseLog(logId: Int, exerciseId: Int, durationMinutes: Int) {
        viewModelScope.launch {
            _updateExerciseLogState.value = UiState.Loading
            try {
                val request = ExerciseLogRequest(exerciseId, durationMinutes)
                val response = repository.updateExerciseLog(logId, request)
                if (response.isSuccessful) {
                    _updateExerciseLogState.value = UiState.Success(Unit)
                    // Reload history after update
                    loadExerciseHistory()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Không thể cập nhật log"
                    _updateExerciseLogState.value = UiState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _updateExerciseLogState.value = UiState.Error("Lỗi: ${e.message}")
            }
        }
    }

    fun deleteExerciseLog(logId: Int) {
        viewModelScope.launch {
            _deleteExerciseLogState.value = UiState.Loading
            try {
                val response = repository.deleteExerciseLog(logId)
                if (response.isSuccessful) {
                    _deleteExerciseLogState.value = UiState.Success(Unit)
                    // Reload history after delete
                    loadExerciseHistory()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Không thể xóa log"
                    _deleteExerciseLogState.value = UiState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _deleteExerciseLogState.value = UiState.Error("Lỗi: ${e.message}")
            }
        }
    }

    fun resetUpdateStates() {
        _updateFoodLogState.value = UiState.Idle
        _updateExerciseLogState.value = UiState.Idle
    }

    fun resetDeleteStates() {
        _deleteFoodLogState.value = UiState.Idle
        _deleteExerciseLogState.value = UiState.Idle
    }
}

