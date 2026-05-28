package com.example.english_app.ui.screens.vocabulary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.english_app.data.local.entity.VocabularySetEntity
import com.example.english_app.data.local.entity.WordEntity
import com.example.english_app.data.repository.AuthRepository
import com.example.english_app.data.repository.LearningRepository
import com.example.english_app.data.repository.VocabularyRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class VocabSetWithCount(val set: VocabularySetEntity, val wordCount: Int)

data class VocabularyUiState(
    val sets: List<VocabSetWithCount> = emptyList(),
    val selectedSet: VocabularySetEntity? = null,
    val words: List<WordEntity> = emptyList(),
    val selectedWord: WordEntity? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val actionSuccess: Boolean = false
)

class VocabularyViewModel(
    private val authRepository: AuthRepository,
    private val vocabularyRepository: VocabularyRepository,
    private val learningRepository: LearningRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VocabularyUiState())
    val uiState: StateFlow<VocabularyUiState> = _uiState.asStateFlow()

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
            vocabularyRepository.observeSets(currentUserId).collect { sets ->
                val withCount = sets.map { set ->
                    VocabSetWithCount(set, vocabularyRepository.countWords(set.id))
                }
                _uiState.update { it.copy(sets = withCount, isLoading = false) }
            }
        }
    }

    fun loadSet(setId: Int) {
        viewModelScope.launch {
            val set = vocabularyRepository.getSetById(setId)
            _uiState.update { it.copy(selectedSet = set) }
            vocabularyRepository.observeWords(setId).collect { words ->
                _uiState.update { it.copy(words = words, isLoading = false) }
            }
        }
    }

    fun loadWord(wordId: Int) {
        viewModelScope.launch {
            // Query DB trực tiếp theo wordId để không bị phụ thuộc vào danh sách words trong bộ nhớ
            val word = vocabularyRepository.getWordById(wordId)
            _uiState.update { it.copy(selectedWord = word) }
        }
    }

    fun createSet(name: String, description: String, tags: String, onSuccess: (Int) -> Unit) {
        viewModelScope.launch {
            if (name.isBlank()) {
                _uiState.update { it.copy(error = "Set name cannot be empty") }
                return@launch
            }
            val setId = vocabularyRepository.createSet(currentUserId, name, description, tags)
            _uiState.update { it.copy(actionSuccess = true) }
            onSuccess(setId)
        }
    }

    fun addWord(
        setId: Int, word: String, pronunciation: String, meaning: String,
        description: String, example: String, collocation: String,
        relatedWords: String, note: String, onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            if (word.isBlank() || meaning.isBlank()) {
                _uiState.update { it.copy(error = "Word and meaning are required") }
                return@launch
            }
            val wordId = vocabularyRepository.addWord(
                setId, word, pronunciation, meaning, description,
                example, collocation, relatedWords, note
            )
            // Initialize learning record for user
            learningRepository.initializeWordsForUser(currentUserId, setId)
            _uiState.update { it.copy(actionSuccess = true) }
            onSuccess()
        }
    }

    fun updateWord(word: WordEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            vocabularyRepository.updateWord(word)
            _uiState.update { it.copy(actionSuccess = true) }
            onSuccess()
        }
    }

    fun deleteWord(word: WordEntity) {
        viewModelScope.launch {
            vocabularyRepository.deleteWord(word)
        }
    }

    fun deleteSet(set: VocabularySetEntity) {
        viewModelScope.launch {
            vocabularyRepository.deleteSet(set)
        }
    }

    fun updateSet(set: VocabularySetEntity, name: String, description: String, tags: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (name.isBlank()) {
                _uiState.update { it.copy(error = "Set name cannot be empty") }
                return@launch
            }
            val updated = set.copy(
                name = name.trim(),
                description = description.trim(),
                tags = tags.trim()
            )
            vocabularyRepository.updateSet(updated)
            _uiState.update { it.copy(selectedSet = updated, actionSuccess = true) }
            onSuccess()
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
    fun clearSuccess() = _uiState.update { it.copy(actionSuccess = false) }
}

class VocabularyViewModelFactory(
    private val authRepository: AuthRepository,
    private val vocabularyRepository: VocabularyRepository,
    private val learningRepository: LearningRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return VocabularyViewModel(authRepository, vocabularyRepository, learningRepository) as T
    }
}

