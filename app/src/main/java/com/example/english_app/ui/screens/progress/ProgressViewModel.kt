package com.example.english_app.ui.screens.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.english_app.data.repository.AuthRepository
import com.example.english_app.data.repository.LearningRepository
import com.example.english_app.data.repository.ProgressStats
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProgressUiState(
    val stats: ProgressStats = ProgressStats(0, 0, 0, 0, 0f, 0),
    val recentActivity: Map<String, Int> = emptyMap(),
    val level: String = "Beginner",
    val isLoading: Boolean = true
)

class ProgressViewModel(
    private val authRepository: AuthRepository,
    private val learningRepository: LearningRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    init {
        loadProgress()
    }

    fun loadProgress() {
        viewModelScope.launch {
            authRepository.currentUserId.collect { userId ->
                if (userId <= 0) return@collect
                val stats = learningRepository.getStats(userId)
                val activity = learningRepository.getRecentActivity(userId, 7)
                val level = estimateLevel(stats.learnedWords)
                _uiState.value = ProgressUiState(
                    stats = stats,
                    recentActivity = activity,
                    level = level,
                    isLoading = false
                )
            }
        }
    }

    private fun estimateLevel(learnedWords: Int): String = when {
        learnedWords >= 5000 -> "C2 - Mastery"
        learnedWords >= 3000 -> "C1 - Advanced"
        learnedWords >= 1500 -> "B2 - Upper Intermediate"
        learnedWords >= 800 -> "B1 - Intermediate"
        learnedWords >= 300 -> "A2 - Elementary"
        else -> "A1 - Beginner"
    }
}

class ProgressViewModelFactory(
    private val authRepository: AuthRepository,
    private val learningRepository: LearningRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ProgressViewModel(authRepository, learningRepository) as T
    }
}

