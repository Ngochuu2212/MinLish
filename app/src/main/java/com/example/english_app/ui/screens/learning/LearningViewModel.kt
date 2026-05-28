package com.example.english_app.ui.screens.learning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.english_app.data.local.entity.LearningRecordEntity
import com.example.english_app.data.local.entity.VocabularySetEntity
import com.example.english_app.data.repository.AuthRepository
import com.example.english_app.data.repository.WordWithRecord
import com.example.english_app.data.repository.LearningRepository
import com.example.english_app.data.repository.VocabularyRepository
import com.example.english_app.domain.srs.SM2Algorithm
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LearningUiState(
    val sets: List<VocabularySetEntity> = emptyList(),
    val sessionWords: List<WordWithRecord> = emptyList(),
    val currentIndex: Int = 0,
    val isFlipped: Boolean = false,
    val isSessionComplete: Boolean = false,
    val sessionStats: SessionStats = SessionStats(),
    val isLoading: Boolean = true
)

data class SessionStats(
    val total: Int = 0,
    val correct: Int = 0,
    val again: Int = 0
)

class LearningViewModel(
    private val authRepository: AuthRepository,
    private val vocabularyRepository: VocabularyRepository,
    private val learningRepository: LearningRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LearningUiState())
    val uiState: StateFlow<LearningUiState> = _uiState.asStateFlow()

    private var currentUserId = -1

    init {
        viewModelScope.launch {
            authRepository.currentUserId.collect { uid ->
                currentUserId = uid
                if (uid > 0) loadSets()
            }
        }
    }

    fun loadSets() {
        viewModelScope.launch {
            if (currentUserId <= 0) return@launch
            val sets = vocabularyRepository.getSets(currentUserId)
            _uiState.update { it.copy(sets = sets, isLoading = false) }
        }
    }

    /**
     * Trả về userId hợp lệ (>0).
     * Nếu currentUserId chưa được set (vẫn là -1 do race condition với DataStore),
     * sẽ chờ Flow emit giá trị hợp lệ trước khi tiếp tục.
     */
    private suspend fun awaitValidUserId(): Int {
        if (currentUserId > 0) return currentUserId
        return authRepository.currentUserId.first { it > 0 }
    }

    fun startFlashcardSession(setId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val uid = awaitValidUserId()
            learningRepository.initializeWordsForUser(uid, setId)
            val words = vocabularyRepository.getWords(setId)
            val session = words.map { word ->
                val existing = learningRepository.getDueSession(uid, 200).find { it.word.id == word.id }
                existing ?: WordWithRecord(word, LearningRecordEntity(wordId = word.id, userId = uid))
            }
            _uiState.update {
                it.copy(
                    sessionWords = session.shuffled(),
                    currentIndex = 0, isFlipped = false,
                    isSessionComplete = session.isEmpty(),
                    sessionStats = SessionStats(total = session.size),
                    isLoading = false
                )
            }
        }
    }

    fun startDailyReview() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val uid = awaitValidUserId()
            // Dùng dailyWordCount của user nếu có, mặc định 20
            val dailyLimit = try {
                vocabularyRepository.getUserDailyWordCount(uid)
            } catch (e: Exception) { 20 }
            val due = learningRepository.getDueSession(uid, dailyLimit)
            _uiState.update {
                it.copy(
                    sessionWords = due,
                    currentIndex = 0,
                    isFlipped = false,
                    isSessionComplete = due.isEmpty(),
                    sessionStats = SessionStats(total = due.size),
                    isLoading = false
                )
            }
        }
    }

    fun flipCard() {
        _uiState.update { it.copy(isFlipped = !it.isFlipped) }
    }

    fun rateWord(quality: Int) {
        viewModelScope.launch {
            val state = _uiState.value
            val current = state.sessionWords.getOrNull(state.currentIndex) ?: return@launch

            learningRepository.reviewWord(current.record, quality)

            val isCorrect = quality >= SM2Algorithm.QUALITY_GOOD
            val newStats = state.sessionStats.copy(
                correct = if (isCorrect) state.sessionStats.correct + 1 else state.sessionStats.correct,
                again = if (quality == SM2Algorithm.QUALITY_AGAIN) state.sessionStats.again + 1 else state.sessionStats.again
            )
            val nextIndex = state.currentIndex + 1
            val isComplete = nextIndex >= state.sessionWords.size

            _uiState.update {
                it.copy(
                    currentIndex = nextIndex,
                    isFlipped = false,
                    isSessionComplete = isComplete,
                    sessionStats = newStats
                )
            }
        }
    }

    fun restartSession() {
        val state = _uiState.value
        _uiState.update {
            it.copy(currentIndex = 0, isFlipped = false, isSessionComplete = false,
                sessionStats = SessionStats(total = state.sessionWords.size))
        }
    }
}

class LearningViewModelFactory(
    private val authRepository: AuthRepository,
    private val vocabularyRepository: VocabularyRepository,
    private val learningRepository: LearningRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return LearningViewModel(authRepository, vocabularyRepository, learningRepository) as T
    }
}
