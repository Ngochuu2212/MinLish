package com.example.english_app.data.repository

import com.example.english_app.data.local.dao.UserDao
import com.example.english_app.data.local.dao.VocabularySetDao
import com.example.english_app.data.local.dao.WordDao
import com.example.english_app.data.local.entity.VocabularySetEntity
import com.example.english_app.data.local.entity.WordEntity
import kotlinx.coroutines.flow.Flow

class VocabularyRepository(
    private val setDao: VocabularySetDao,
    private val wordDao: WordDao,
    private val userDao: UserDao
) {
    fun observeSets(userId: Int): Flow<List<VocabularySetEntity>> =
        setDao.observeByUserId(userId)

    suspend fun getSets(userId: Int): List<VocabularySetEntity> =
        setDao.getByUserId(userId)

    suspend fun createSet(userId: Int, name: String, description: String, tags: String): Int {
        val entity = VocabularySetEntity(
            userId = userId,
            name = name,
            description = description,
            tags = tags
        )
        return setDao.insert(entity).toInt()
    }

    suspend fun updateSet(set: VocabularySetEntity) {
        setDao.update(set.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteSet(set: VocabularySetEntity) = setDao.delete(set)

    suspend fun getSetById(id: Int): VocabularySetEntity? = setDao.findById(id)

    fun observeWords(setId: Int): Flow<List<WordEntity>> = wordDao.observeBySetId(setId)

    suspend fun getWords(setId: Int): List<WordEntity> = wordDao.getBySetId(setId)

    suspend fun addWord(
        setId: Int, word: String, pronunciation: String, meaning: String,
        description: String, example: String, collocation: String,
        relatedWords: String, note: String
    ): Int {
        val entity = WordEntity(
            setId = setId, word = word, pronunciation = pronunciation,
            meaning = meaning, description = description, example = example,
            collocation = collocation, relatedWords = relatedWords, note = note
        )
        return wordDao.insert(entity).toInt()
    }

    suspend fun getWordById(id: Int): WordEntity? = wordDao.findById(id)

    suspend fun updateWord(word: WordEntity) = wordDao.update(word)

    suspend fun deleteWord(word: WordEntity) = wordDao.delete(word)

    suspend fun countWords(setId: Int): Int = wordDao.countBySetId(setId)

    suspend fun countWordsByUser(userId: Int): Int = wordDao.countByUserId(userId)

    suspend fun importWords(words: List<WordEntity>) = wordDao.insertAll(words)

    /** Lấy dailyWordCount của user để dùng cho daily review limit */
    suspend fun getUserDailyWordCount(userId: Int): Int =
        userDao.getDailyWordCount(userId) ?: 20
}

