package com.example.english_app.ui.screens.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.english_app.data.local.entity.VocabularySetEntity
import com.example.english_app.data.repository.AuthRepository
import com.example.english_app.data.repository.LearningRepository
import com.example.english_app.data.repository.VocabularyRepository
import com.example.english_app.notification.DailyReminderWorker
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

data class NotificationSettings(
    val enabled: Boolean = true,
    val hour: Int = 8,
    val minute: Int = 0
)

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val vocabularyRepository: VocabularyRepository,
    private val learningRepository: LearningRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _notifSettings = MutableStateFlow(NotificationSettings())
    val notifSettings: StateFlow<NotificationSettings> = _notifSettings.asStateFlow()

    init {
        loadData()
        loadNotificationSettings()
    }

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

    private fun loadNotificationSettings() {
        viewModelScope.launch {
            combine(
                authRepository.userPreferences.notifEnabled,
                authRepository.userPreferences.notifHour,
                authRepository.userPreferences.notifMinute
            ) { enabled, hour, minute ->
                NotificationSettings(enabled, hour, minute)
            }.collect { settings ->
                _notifSettings.value = settings
            }
        }
    }

    fun saveNotificationSettings(context: Context, enabled: Boolean, hour: Int, minute: Int) {
        viewModelScope.launch {
            authRepository.userPreferences.saveNotificationSettings(enabled, hour, minute)
            if (enabled) {
                DailyReminderWorker.schedule(context, hour, minute)
            } else {
                DailyReminderWorker.cancel(context)
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
