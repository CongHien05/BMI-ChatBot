package com.hienpc.bmiapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hienpc.bmiapp.data.model.ProfileUpdateRequest
import com.hienpc.bmiapp.data.repository.UserRepository
import com.hienpc.bmiapp.utils.UiState
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _updateProfileState = MutableLiveData<UiState<Unit>>(UiState.Idle)
    val updateProfileState: LiveData<UiState<Unit>> = _updateProfileState

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


