package com.hienpc.bmiapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hienpc.bmiapp.data.model.*
import com.hienpc.bmiapp.data.repository.MeasurementRepository
import com.hienpc.bmiapp.data.repository.UserRepository
import com.hienpc.bmiapp.utils.UiState
import com.hienpc.bmiapp.utils.retryIO
import com.hienpc.bmiapp.utils.toErrorMessage
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val userRepository: UserRepository,
    private val measurementRepository: MeasurementRepository
) : ViewModel() {

    private val _dashboardState =
        MutableLiveData<UiState<DashboardSummary>>(UiState.Idle)
    val dashboardState: LiveData<UiState<DashboardSummary>> = _dashboardState

    private val _measurementState = MutableLiveData<UiState<Unit>>(UiState.Idle)
    val measurementState: LiveData<UiState<Unit>> = _measurementState
    
    private val _weeklySummaryState = MutableLiveData<UiState<WeeklySummaryResponse>>(UiState.Idle)
    val weeklySummaryState: LiveData<UiState<WeeklySummaryResponse>> = _weeklySummaryState
    
    private val _trendAnalysisState = MutableLiveData<UiState<TrendAnalysisResponse>>(UiState.Idle)
    val trendAnalysisState: LiveData<UiState<TrendAnalysisResponse>> = _trendAnalysisState
    
    private val _weightPredictionState = MutableLiveData<UiState<WeightPredictionResponse>>(UiState.Idle)
    val weightPredictionState: LiveData<UiState<WeightPredictionResponse>> = _weightPredictionState

    fun loadDashboard() {
        viewModelScope.launch {
            _dashboardState.value = UiState.Loading
            try {
                // Retry with exponential backoff
                val response = retryIO(times = 3) {
                    userRepository.getDashboardSummary()
                }
                
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) {
                        // Check if data is empty (no weight, no calories)
                        if (data.currentWeight == null && data.totalCaloriesToday == 0) {
                            _dashboardState.value = UiState.Empty(
                                "Chưa có dữ liệu. Hãy bắt đầu log bữa ăn!"
                            )
                        } else {
                            _dashboardState.value = UiState.Success(data)
                        }
                    } else {
                        _dashboardState.value = UiState.Error(
                            message = "Không có dữ liệu dashboard",
                            errorCode = "NO_DATA"
                        )
                    }
                } else {
                    _dashboardState.value = UiState.Error(
                        message = response.message().ifBlank { "Không tải được dashboard" },
                        errorCode = "HTTP_${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _dashboardState.value = UiState.Error(
                    message = e.toErrorMessage(),
                    errorCode = "EXCEPTION",
                    throwable = e
                )
            }
        }
    }

    fun addMeasurement(weightKg: Double, heightCm: Double?) {
        if (weightKg <= 0) {
            _measurementState.value = UiState.Error(
                message = "Cân nặng phải lớn hơn 0",
                errorCode = "VALIDATION_ERROR"
            )
            return
        }

        viewModelScope.launch {
            _measurementState.value = UiState.Loading
            try {
                val request = MeasurementRequest(weightKg = weightKg, heightCm = heightCm)
                val response = retryIO(times = 2) {
                    measurementRepository.addMeasurement(request)
                }
                
                if (response.isSuccessful) {
                    _measurementState.value = UiState.Success(Unit)
                    loadDashboard() // Reload dashboard after adding measurement
                } else {
                    _measurementState.value = UiState.Error(
                        message = response.message().ifBlank { "Lưu chỉ số thất bại" },
                        errorCode = "HTTP_${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _measurementState.value = UiState.Error(
                    message = e.toErrorMessage(),
                    errorCode = "EXCEPTION",
                    throwable = e
                )
            }
        }
    }

    fun resetMeasurementState() {
        _measurementState.value = UiState.Idle
    }
    
    fun loadWeeklySummary() {
        viewModelScope.launch {
            _weeklySummaryState.value = UiState.Loading
            try {
                val response = retryIO(times = 2) {
                    userRepository.getWeeklySummary()
                }
                
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) {
                        if (data.dailySummaries.isEmpty()) {
                            _weeklySummaryState.value = UiState.Empty("Chưa có dữ liệu 7 ngày qua")
                        } else {
                            _weeklySummaryState.value = UiState.Success(data)
                        }
                    } else {
                        _weeklySummaryState.value = UiState.Error(
                            message = "Không có dữ liệu weekly summary",
                            errorCode = "NO_DATA"
                        )
                    }
                } else {
                    _weeklySummaryState.value = UiState.Error(
                        message = response.message().ifBlank { "Không tải được weekly summary" },
                        errorCode = "HTTP_${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _weeklySummaryState.value = UiState.Error(
                    message = e.toErrorMessage(),
                    errorCode = "EXCEPTION",
                    throwable = e
                )
            }
        }
    }
    
    fun loadTrendAnalysis() {
        viewModelScope.launch {
            _trendAnalysisState.value = UiState.Loading
            try {
                val response = retryIO(times = 2) {
                    userRepository.getTrendAnalysis()
                }
                
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) {
                        _trendAnalysisState.value = UiState.Success(data)
                    } else {
                        _trendAnalysisState.value = UiState.Error(
                            message = "Không có dữ liệu trend analysis",
                            errorCode = "NO_DATA"
                        )
                    }
                } else {
                    _trendAnalysisState.value = UiState.Error(
                        message = response.message().ifBlank { "Không tải được trend analysis" },
                        errorCode = "HTTP_${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _trendAnalysisState.value = UiState.Error(
                    message = e.toErrorMessage(),
                    errorCode = "EXCEPTION",
                    throwable = e
                )
            }
        }
    }

    fun loadWeightPrediction(days: Int = 7) {
        viewModelScope.launch {
            _weightPredictionState.value = UiState.Loading
            try {
                val response = retryIO(times = 2) {
                    userRepository.predictWeight(days)
                }
                
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) {
                        _weightPredictionState.value = UiState.Success(data)
                    } else {
                        _weightPredictionState.value = UiState.Error(
                            message = "Không có dữ liệu dự đoán",
                            errorCode = "NO_DATA"
                        )
                    }
                } else {
                    _weightPredictionState.value = UiState.Error(
                        message = "Không thể tải dự đoán: ${response.message()}",
                        errorCode = "API_ERROR_${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _weightPredictionState.value = UiState.Error(
                    message = e.toErrorMessage(),
                    errorCode = "EXCEPTION"
                )
            }
        }
    }

    class Factory(
        private val userRepository: UserRepository,
        private val measurementRepository: MeasurementRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DashboardViewModel(userRepository, measurementRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}


