package com.example.english_app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.example.english_app.data.local.entity.UserEntity
import com.example.english_app.data.repository.AuthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

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

    fun saveAvatar(context: Context, uri: Uri) {
        viewModelScope.launch {
            val user = _uiState.value.user ?: return@launch
            _uiState.update { it.copy(isSaving = true) }
            try {
                // Sao chép ảnh vào internal storage để tránh mất quyền truy cập sau này
                val fileName = "avatar_${user.id}.jpg"
                val destFile = File(context.filesDir, fileName)
                context.contentResolver.openInputStream(uri)?.use { input ->
                    destFile.outputStream().use { output -> input.copyTo(output) }
                }
                val localUri = destFile.toUri().toString()
                authRepository.updateProfile(user.copy(avatarUri = localUri))
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = "Không thể lưu ảnh: ${e.message}") }
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

