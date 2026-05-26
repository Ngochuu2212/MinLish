package com.example.english_app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.english_app.data.local.entity.UserEntity
import com.example.english_app.data.repository.AuthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: UserEntity? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
)

class ProfileViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUserId.collect { userId ->
                if (userId <= 0) { _uiState.update { it.copy(isLoading = false) }; return@collect }
                authRepository.observeUser(userId).collect { user ->
                    _uiState.update { it.copy(user = user, isLoading = false) }
                }
            }
        }
    }

    fun updateProfile(name: String, goal: String, level: String, dailyCount: Int) {
        viewModelScope.launch {
            val user = _uiState.value.user ?: return@launch
            _uiState.update { it.copy(isSaving = true) }
            authRepository.updateProfile(
                user.copy(name = name, learningGoal = goal, level = level, dailyWordCount = dailyCount)
            )
            _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
        }
    }

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }

    fun clearSuccess() = _uiState.update { it.copy(saveSuccess = false) }
}

class ProfileViewModelFactory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ProfileViewModel(authRepository) as T
    }
}

