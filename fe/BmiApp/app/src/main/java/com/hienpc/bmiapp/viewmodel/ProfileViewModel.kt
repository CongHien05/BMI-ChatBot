package com.hienpc.bmiapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hienpc.bmiapp.data.model.Achievement
import com.hienpc.bmiapp.data.model.ProfileUpdateRequest
import com.hienpc.bmiapp.data.model.Streak
import com.hienpc.bmiapp.data.repository.UserRepository
import com.hienpc.bmiapp.utils.UiState
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _updateProfileState = MutableLiveData<UiState<Unit>>(UiState.Idle)
    val updateProfileState: LiveData<UiState<Unit>> = _updateProfileState
    
    private val _streakState = MutableLiveData<UiState<Streak>>(UiState.Idle)
    val streakState: LiveData<UiState<Streak>> = _streakState
    
    private val _achievementsState = MutableLiveData<UiState<List<Achievement>>>(UiState.Idle)
    val achievementsState: LiveData<UiState<List<Achievement>>> = _achievementsState

    fun updateProfile(request: ProfileUpdateRequest) {
        viewModelScope.launch {
            _updateProfileState.value = UiState.Loading
            try {
                val response = userRepository.updateProfile(request)
                if (response.isSuccessful) {
                    _updateProfileState.value = UiState.Success(Unit)
                } else {
                    _updateProfileState.value = UiState.Error(
                        response.message().ifBlank { "Cập nhật profile thất bại" }
                    )
                }
            } catch (e: Exception) {
                _updateProfileState.value = UiState.Error("Lỗi kết nối: ${e.message}")
            }
        }
    }

    fun resetState() {
        _updateProfileState.value = UiState.Idle
    }
    
    fun loadStreak() {
        viewModelScope.launch {
            _streakState.value = UiState.Loading
            try {
                val response = userRepository.getStreak()
                if (response.isSuccessful && response.body() != null) {
                    _streakState.value = UiState.Success(response.body()!!)
                } else {
                    _streakState.value = UiState.Error("Không thể tải streak")
                }
            } catch (e: Exception) {
                _streakState.value = UiState.Error("Lỗi: ${e.message}")
            }
        }
    }
    
    fun loadAchievements() {
        viewModelScope.launch {
            _achievementsState.value = UiState.Loading
            try {
                val response = userRepository.getAchievements()
                if (response.isSuccessful && response.body() != null) {
                    _achievementsState.value = UiState.Success(response.body()!!)
                } else {
                    _achievementsState.value = UiState.Error("Không thể tải thành tựu")
                }
            } catch (e: Exception) {
                _achievementsState.value = UiState.Error("Lỗi: ${e.message}")
            }
        }
    }

    class Factory(
        private val userRepository: UserRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ProfileViewModel(userRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}


