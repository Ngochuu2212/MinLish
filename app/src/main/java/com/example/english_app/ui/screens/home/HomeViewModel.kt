package com.example.english_app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.english_app.data.local.entity.VocabularySetEntity
import com.example.english_app.data.repository.AuthRepository
import com.example.english_app.data.repository.LearningRepository
import com.example.english_app.data.repository.VocabularyRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeUiState(
    val userName: String = "",
    val totalWords: Int = 0,
    val learnedWords: Int = 0,
    val dueWords: Int = 0,
    val newWords: Int = 0,
    val streak: Int = 0,
    val accuracy: Float = 0f,
    val masteryProgress: Float = 0f,
    val recentSets: List<VocabularySetEntity> = emptyList(),
    val isLoading: Boolean = true
)

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val vocabularyRepository: VocabularyRepository,
    private val learningRepository: LearningRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init { loadData() }

    fun loadData() {
        viewModelScope.launch {
            authRepository.currentUserId.collect { userId ->
                if (userId <= 0) return@collect
                val user = authRepository.getUserById(userId)
                val stats = learningRepository.getStats(userId)
                val recentSets = vocabularyRepository.getSets(userId).take(3)
                val mastery = if (stats.totalWords > 0)
                    (stats.learnedWords.toFloat() / stats.totalWords.coerceAtLeast(1)).coerceIn(0f, 1f)
                else 0f
                _uiState.value = HomeUiState(
                    userName = user?.name?.substringBefore(" ") ?: "there",
                    totalWords = stats.totalWords,
                    learnedWords = stats.learnedWords,
                    dueWords = stats.dueWords,
                    newWords = (user?.dailyWordCount ?: 10),
                    streak = stats.streak,
                    accuracy = stats.accuracy,
                    masteryProgress = mastery,
                    recentSets = recentSets,
                    isLoading = false
                )
            }
        }
    }
}

class HomeViewModelFactory(
    private val authRepository: AuthRepository,
    private val vocabularyRepository: VocabularyRepository,
    private val learningRepository: LearningRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return HomeViewModel(authRepository, vocabularyRepository, learningRepository) as T
    }
}
