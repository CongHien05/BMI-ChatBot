package com.hienpc.bmiapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hienpc.bmiapp.data.model.DashboardSummary
import com.hienpc.bmiapp.data.repository.UserRepository
import com.hienpc.bmiapp.utils.UiState
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _dashboardState =
        MutableLiveData<UiState<DashboardSummary>>(UiState.Idle)
    val dashboardState: LiveData<UiState<DashboardSummary>> = _dashboardState

    fun loadDashboard() {
        viewModelScope.launch {
            _dashboardState.value = UiState.Loading
            try {
                val response = userRepository.getDashboardSummary()
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) {
                        _dashboardState.value = UiState.Success(data)
                    } else {
                        _dashboardState.value = UiState.Error("Không có dữ liệu dashboard")
                    }
                } else {
                    _dashboardState.value = UiState.Error(
                        response.message().ifBlank { "Không tải được dashboard" }
                    )
                }
            } catch (e: Exception) {
                _dashboardState.value = UiState.Error("Lỗi kết nối: ${e.message}")
            }
        }
    }

    class Factory(
        private val userRepository: UserRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DashboardViewModel(userRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}


