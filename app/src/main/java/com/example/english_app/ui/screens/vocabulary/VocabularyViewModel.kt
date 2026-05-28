package com.example.english_app.ui.screens.vocabulary

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.english_app.data.local.entity.VocabularySetEntity
import com.example.english_app.data.local.entity.WordEntity
import com.example.english_app.data.repository.AuthRepository
import com.example.english_app.data.repository.LearningRepository
import com.example.english_app.data.repository.VocabularyRepository
import com.example.english_app.utils.CsvHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

data class VocabSetWithCount(val set: VocabularySetEntity, val wordCount: Int)

data class VocabularyUiState(
    val sets: List<VocabSetWithCount> = emptyList(),
    val selectedSet: VocabularySetEntity? = null,
    val words: List<WordEntity> = emptyList(),
    val selectedWord: WordEntity? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val actionSuccess: Boolean = false,
    // Import/Export
    val importedCount: Int? = null,   // null = not imported yet, >0 = success count
    val importError: String? = null,
    val isImporting: Boolean = false
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
    fun clearImportResult() = _uiState.update { it.copy(importedCount = null, importError = null) }

    // ── Import from CSV ───────────────────────────────────────────────────────
    fun importFromCsv(context: Context, uri: Uri, setId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, importError = null, importedCount = null) }
            try {
                val csvContent = context.contentResolver.openInputStream(uri)
                    ?.bufferedReader()?.readText()
                    ?: run {
                        _uiState.update { it.copy(isImporting = false, importError = "Cannot read file") }
                        return@launch
                    }
                val words = CsvHelper.parseWords(csvContent, setId)
                if (words.isEmpty()) {
                    _uiState.update { it.copy(isImporting = false, importError = "No valid words found.\nMake sure columns: word, meaning are filled.") }
                    return@launch
                }
                vocabularyRepository.importWords(words)
                learningRepository.initializeWordsForUser(currentUserId, setId)
                _uiState.update { it.copy(isImporting = false, importedCount = words.size) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isImporting = false, importError = "Import failed: ${e.message}") }
            }
        }
    }

    // ── Export to CSV ─────────────────────────────────────────────────────────
    fun exportToCsv(context: Context, setId: Int) {
        viewModelScope.launch {
            try {
                val set = vocabularyRepository.getSetById(setId) ?: return@launch
                val words = vocabularyRepository.getWords(setId)
                if (words.isEmpty()) {
                    _uiState.update { it.copy(error = "No words to export") }
                    return@launch
                }
                val csvContent = CsvHelper.exportToCsv(words)
                val safeName = set.name.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
                val file = File(context.cacheDir, "${safeName}.csv")
                file.writeText(csvContent, Charsets.UTF_8)
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Vocabulary: ${set.name}")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Export \"${set.name}\""))
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Export failed: ${e.message}") }
            }
        }
    }

    // ── Download CSV template ─────────────────────────────────────────────────
    fun downloadTemplate(context: Context) {
        viewModelScope.launch {
            try {
                val file = File(context.cacheDir, "vocabulary_template.csv")
                file.writeText(CsvHelper.buildTemplate(), Charsets.UTF_8)
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "MinLish - Vocabulary Import Template")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Save Template"))
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed: ${e.message}") }
            }
        }
    }
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

