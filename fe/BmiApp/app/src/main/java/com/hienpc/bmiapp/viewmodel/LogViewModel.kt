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
    
    private val _favoriteFoodsState = MutableLiveData<UiState<List<FoodResponse>>>(UiState.Idle)
    val favoriteFoodsState: LiveData<UiState<List<FoodResponse>>> = _favoriteFoodsState
    
    private val _favoriteExercisesState = MutableLiveData<UiState<List<ExerciseResponse>>>(UiState.Idle)
    val favoriteExercisesState: LiveData<UiState<List<ExerciseResponse>>> = _favoriteExercisesState
    
    private val _toggleFavoriteState = MutableLiveData<UiState<Boolean>>(UiState.Idle)
    val toggleFavoriteState: LiveData<UiState<Boolean>> = _toggleFavoriteState
    
    private val _recentFoodsState = MutableLiveData<UiState<List<FoodResponse>>>(UiState.Idle)
    val recentFoodsState: LiveData<UiState<List<FoodResponse>>> = _recentFoodsState
    
    private val _recentExercisesState = MutableLiveData<UiState<List<ExerciseResponse>>>(UiState.Idle)
    val recentExercisesState: LiveData<UiState<List<ExerciseResponse>>> = _recentExercisesState
    
    private val _customFoodsState = MutableLiveData<UiState<List<FoodResponse>>>(UiState.Idle)
    val customFoodsState: LiveData<UiState<List<FoodResponse>>> = _customFoodsState
    
    private val _customExercisesState = MutableLiveData<UiState<List<ExerciseResponse>>>(UiState.Idle)
    val customExercisesState: LiveData<UiState<List<ExerciseResponse>>> = _customExercisesState
    
    private val _createCustomFoodState = MutableLiveData<UiState<FoodResponse>>(UiState.Idle)
    val createCustomFoodState: LiveData<UiState<FoodResponse>> = _createCustomFoodState
    
    private val _createCustomExerciseState = MutableLiveData<UiState<ExerciseResponse>>(UiState.Idle)
    val createCustomExerciseState: LiveData<UiState<ExerciseResponse>> = _createCustomExerciseState
    
    private val _updateCustomFoodState = MutableLiveData<UiState<FoodResponse>>(UiState.Idle)
    val updateCustomFoodState: LiveData<UiState<FoodResponse>> = _updateCustomFoodState
    
    private val _updateCustomExerciseState = MutableLiveData<UiState<ExerciseResponse>>(UiState.Idle)
    val updateCustomExerciseState: LiveData<UiState<ExerciseResponse>> = _updateCustomExerciseState
    
    private val _deleteCustomFoodState = MutableLiveData<UiState<Unit>>(UiState.Idle)
    val deleteCustomFoodState: LiveData<UiState<Unit>> = _deleteCustomFoodState
    
    private val _deleteCustomExerciseState = MutableLiveData<UiState<Unit>>(UiState.Idle)
    val deleteCustomExerciseState: LiveData<UiState<Unit>> = _deleteCustomExerciseState

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
    
    // ========== FAVORITES METHODS ==========
    
    fun loadFavoriteFoods() {
        viewModelScope.launch {
            _favoriteFoodsState.value = UiState.Loading
            try {
                val response = repository.getFavoriteFoods()
                if (response.isSuccessful) {
                    val data = response.body().orEmpty()
                    _favoriteFoodsState.value = UiState.Success(data)
                } else {
                    _favoriteFoodsState.value = UiState.Error(
                        response.message().ifBlank { "Không tải được món ăn yêu thích" }
                    )
                }
            } catch (e: Exception) {
                _favoriteFoodsState.value = UiState.Error("Lỗi kết nối: ${e.message}")
            }
        }
    }
    
    fun loadFavoriteExercises() {
        viewModelScope.launch {
            _favoriteExercisesState.value = UiState.Loading
            try {
                val response = repository.getFavoriteExercises()
                if (response.isSuccessful) {
                    val data = response.body().orEmpty()
                    _favoriteExercisesState.value = UiState.Success(data)
                } else {
                    _favoriteExercisesState.value = UiState.Error(
                        response.message().ifBlank { "Không tải được bài tập yêu thích" }
                    )
                }
            } catch (e: Exception) {
                _favoriteExercisesState.value = UiState.Error("Lỗi kết nối: ${e.message}")
            }
        }
    }
    
    fun toggleFavoriteFood(foodId: Int, isCurrentlyFavorite: Boolean) {
        viewModelScope.launch {
            _toggleFavoriteState.value = UiState.Loading
            try {
                val response = if (isCurrentlyFavorite) {
                    repository.removeFavoriteFood(foodId)
                } else {
                    repository.addFavoriteFood(foodId)
                }
                if (response.isSuccessful) {
                    _toggleFavoriteState.value = UiState.Success(!isCurrentlyFavorite)
                    // Reload favorites list
                    loadFavoriteFoods()
                } else {
                    _toggleFavoriteState.value = UiState.Error(
                        response.message().ifBlank { "Thao tác thất bại" }
                    )
                }
            } catch (e: Exception) {
                _toggleFavoriteState.value = UiState.Error("Lỗi kết nối: ${e.message}")
            }
        }
    }
    
    fun toggleFavoriteExercise(exerciseId: Int, isCurrentlyFavorite: Boolean) {
        viewModelScope.launch {
            _toggleFavoriteState.value = UiState.Loading
            try {
                val response = if (isCurrentlyFavorite) {
                    repository.removeFavoriteExercise(exerciseId)
                } else {
                    repository.addFavoriteExercise(exerciseId)
                }
                if (response.isSuccessful) {
                    _toggleFavoriteState.value = UiState.Success(!isCurrentlyFavorite)
                    // Reload favorites list
                    loadFavoriteExercises()
                } else {
                    _toggleFavoriteState.value = UiState.Error(
                        response.message().ifBlank { "Thao tác thất bại" }
                    )
                }
            } catch (e: Exception) {
                _toggleFavoriteState.value = UiState.Error("Lỗi kết nối: ${e.message}")
            }
        }
    }
    
    fun checkFoodFavorite(foodId: Int, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.isFoodFavorite(foodId)
                if (response.isSuccessful) {
                    callback(response.body() ?: false)
                }
            } catch (e: Exception) {
                // Silent fail, assume not favorite
                callback(false)
            }
        }
    }
    
    fun checkExerciseFavorite(exerciseId: Int, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.isExerciseFavorite(exerciseId)
                if (response.isSuccessful) {
                    callback(response.body() ?: false)
                }
            } catch (e: Exception) {
                // Silent fail, assume not favorite
                callback(false)
            }
        }
    }
    
    // ========== RECENTLY USED METHODS ==========
    
    fun loadRecentFoods(limit: Int = 10) {
        viewModelScope.launch {
            _recentFoodsState.value = UiState.Loading
            try {
                val response = repository.getRecentFoods(limit)
                if (response.isSuccessful) {
                    val data = response.body().orEmpty()
                    _recentFoodsState.value = UiState.Success(data)
                } else {
                    _recentFoodsState.value = UiState.Error(
                        response.message().ifBlank { "Không tải được món ăn gần đây" }
                    )
                }
            } catch (e: Exception) {
                _recentFoodsState.value = UiState.Error("Lỗi kết nối: ${e.message}")
            }
        }
    }
    
    fun loadRecentExercises(limit: Int = 10) {
        viewModelScope.launch {
            _recentExercisesState.value = UiState.Loading
            try {
                val response = repository.getRecentExercises(limit)
                if (response.isSuccessful) {
                    val data = response.body().orEmpty()
                    _recentExercisesState.value = UiState.Success(data)
                } else {
                    _recentExercisesState.value = UiState.Error(
                        response.message().ifBlank { "Không tải được bài tập gần đây" }
                    )
                }
            } catch (e: Exception) {
                _recentExercisesState.value = UiState.Error("Lỗi kết nối: ${e.message}")
            }
        }
    }
    
    // ========== CUSTOM FOOD/EXERCISE METHODS ==========
    
    fun loadCustomFoods() {
        viewModelScope.launch {
            _customFoodsState.value = UiState.Loading
            try {
                val response = repository.getCustomFoods()
                if (response.isSuccessful) {
                    val data = response.body().orEmpty()
                    _customFoodsState.value = UiState.Success(data)
                } else {
                    _customFoodsState.value = UiState.Error(
                        response.message().ifBlank { "Không tải được món ăn của tôi" }
                    )
                }
            } catch (e: Exception) {
                _customFoodsState.value = UiState.Error("Lỗi kết nối: ${e.message}")
            }
        }
    }
    
    fun createCustomFood(foodName: String, servingUnit: String, caloriesPerUnit: Int) {
        viewModelScope.launch {
            _createCustomFoodState.value = UiState.Loading
            try {
                val request = CustomFoodRequest(foodName, servingUnit, caloriesPerUnit)
                val response = repository.createCustomFood(request)
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) {
                        _createCustomFoodState.value = UiState.Success(data)
                        // Reload custom foods list
                        loadCustomFoods()
                        // Reload all foods to include the new custom food
                        loadFoods()
                    } else {
                        _createCustomFoodState.value = UiState.Error("Không tạo được món ăn")
                    }
                } else {
                    _createCustomFoodState.value = UiState.Error(
                        response.message().ifBlank { "Tạo món ăn thất bại" }
                    )
                }
            } catch (e: Exception) {
                _createCustomFoodState.value = UiState.Error("Lỗi kết nối: ${e.message}")
            }
        }
    }
    
    fun updateCustomFood(foodId: Int, foodName: String, servingUnit: String, caloriesPerUnit: Int) {
        viewModelScope.launch {
            _updateCustomFoodState.value = UiState.Loading
            try {
                val request = CustomFoodRequest(foodName, servingUnit, caloriesPerUnit)
                val response = repository.updateCustomFood(foodId, request)
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) {
                        _updateCustomFoodState.value = UiState.Success(data)
                        // Reload custom foods list
                        loadCustomFoods()
                        // Reload all foods
                        loadFoods()
                    } else {
                        _updateCustomFoodState.value = UiState.Error("Không cập nhật được món ăn")
                    }
                } else {
                    _updateCustomFoodState.value = UiState.Error(
                        response.message().ifBlank { "Cập nhật món ăn thất bại" }
                    )
                }
            } catch (e: Exception) {
                _updateCustomFoodState.value = UiState.Error("Lỗi kết nối: ${e.message}")
            }
        }
    }
    
    fun deleteCustomFood(foodId: Int) {
        viewModelScope.launch {
            _deleteCustomFoodState.value = UiState.Loading
            try {
                val response = repository.deleteCustomFood(foodId)
                if (response.isSuccessful) {
                    _deleteCustomFoodState.value = UiState.Success(Unit)
                    // Reload custom foods list
                    loadCustomFoods()
                    // Reload all foods
                    loadFoods()
                } else {
                    _deleteCustomFoodState.value = UiState.Error(
                        response.message().ifBlank { "Xóa món ăn thất bại" }
                    )
                }
            } catch (e: Exception) {
                _deleteCustomFoodState.value = UiState.Error("Lỗi kết nối: ${e.message}")
            }
        }
    }
    
    fun loadCustomExercises() {
        viewModelScope.launch {
            _customExercisesState.value = UiState.Loading
            try {
                val response = repository.getCustomExercises()
                if (response.isSuccessful) {
                    val data = response.body().orEmpty()
                    _customExercisesState.value = UiState.Success(data)
                } else {
                    _customExercisesState.value = UiState.Error(
                        response.message().ifBlank { "Không tải được bài tập của tôi" }
                    )
                }
            } catch (e: Exception) {
                _customExercisesState.value = UiState.Error("Lỗi kết nối: ${e.message}")
            }
        }
    }
    
    fun createCustomExercise(exerciseName: String, caloriesBurnedPerHour: Int) {
        viewModelScope.launch {
            _createCustomExerciseState.value = UiState.Loading
            try {
                val request = CustomExerciseRequest(exerciseName, caloriesBurnedPerHour)
                val response = repository.createCustomExercise(request)
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) {
                        _createCustomExerciseState.value = UiState.Success(data)
                        // Reload custom exercises list
                        loadCustomExercises()
                        // Reload all exercises to include the new custom exercise
                        loadExercises()
                    } else {
                        _createCustomExerciseState.value = UiState.Error("Không tạo được bài tập")
                    }
                } else {
                    _createCustomExerciseState.value = UiState.Error(
                        response.message().ifBlank { "Tạo bài tập thất bại" }
                    )
                }
            } catch (e: Exception) {
                _createCustomExerciseState.value = UiState.Error("Lỗi kết nối: ${e.message}")
            }
        }
    }
    
    fun updateCustomExercise(exerciseId: Int, exerciseName: String, caloriesBurnedPerHour: Int) {
        viewModelScope.launch {
            _updateCustomExerciseState.value = UiState.Loading
            try {
                val request = CustomExerciseRequest(exerciseName, caloriesBurnedPerHour)
                val response = repository.updateCustomExercise(exerciseId, request)
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) {
                        _updateCustomExerciseState.value = UiState.Success(data)
                        // Reload custom exercises list
                        loadCustomExercises()
                        // Reload all exercises
                        loadExercises()
                    } else {
                        _updateCustomExerciseState.value = UiState.Error("Không cập nhật được bài tập")
                    }
                } else {
                    _updateCustomExerciseState.value = UiState.Error(
                        response.message().ifBlank { "Cập nhật bài tập thất bại" }
                    )
                }
            } catch (e: Exception) {
                _updateCustomExerciseState.value = UiState.Error("Lỗi kết nối: ${e.message}")
            }
        }
    }
    
    fun deleteCustomExercise(exerciseId: Int) {
        viewModelScope.launch {
            _deleteCustomExerciseState.value = UiState.Loading
            try {
                val response = repository.deleteCustomExercise(exerciseId)
                if (response.isSuccessful) {
                    _deleteCustomExerciseState.value = UiState.Success(Unit)
                    // Reload custom exercises list
                    loadCustomExercises()
                    // Reload all exercises
                    loadExercises()
                } else {
                    _deleteCustomExerciseState.value = UiState.Error(
                        response.message().ifBlank { "Xóa bài tập thất bại" }
                    )
                }
            } catch (e: Exception) {
                _deleteCustomExerciseState.value = UiState.Error("Lỗi kết nối: ${e.message}")
            }
        }
    }
    
    // ========== VALIDATION METHODS ==========
    
    private val _foodNameValidationState = MutableLiveData<UiState<Boolean>>(UiState.Idle)
    val foodNameValidationState: LiveData<UiState<Boolean>> = _foodNameValidationState
    
    private val _exerciseNameValidationState = MutableLiveData<UiState<Boolean>>(UiState.Idle)
    val exerciseNameValidationState: LiveData<UiState<Boolean>> = _exerciseNameValidationState
    
    fun checkFoodNameExists(name: String) {
        if (name.isBlank()) {
            _foodNameValidationState.value = UiState.Idle
            return
        }
        
        viewModelScope.launch {
            _foodNameValidationState.value = UiState.Loading
            try {
                val response = repository.checkFoodNameExists(name.trim())
                if (response.isSuccessful) {
                    val exists = response.body() ?: false
                    _foodNameValidationState.value = UiState.Success(exists)
                } else {
                    _foodNameValidationState.value = UiState.Error("Không thể kiểm tra tên món ăn")
                }
            } catch (e: Exception) {
                _foodNameValidationState.value = UiState.Error("Lỗi kết nối: ${e.message}")
            }
        }
    }
    
    fun checkExerciseNameExists(name: String) {
        if (name.isBlank()) {
            _exerciseNameValidationState.value = UiState.Idle
            return
        }
        
        viewModelScope.launch {
            _exerciseNameValidationState.value = UiState.Loading
            try {
                val response = repository.checkExerciseNameExists(name.trim())
                if (response.isSuccessful) {
                    val exists = response.body() ?: false
                    _exerciseNameValidationState.value = UiState.Success(exists)
                } else {
                    _exerciseNameValidationState.value = UiState.Error("Không thể kiểm tra tên bài tập")
                }
            } catch (e: Exception) {
                _exerciseNameValidationState.value = UiState.Error("Lỗi kết nối: ${e.message}")
            }
        }
    }
}
