package com.example.english_app.data.repository

import com.example.english_app.data.local.UserPreferences
import com.example.english_app.data.local.dao.LearningRecordDao
import com.example.english_app.data.local.dao.UserDao
import com.example.english_app.data.local.dao.WordDao
import com.example.english_app.data.local.entity.LearningRecordEntity
import com.example.english_app.data.local.entity.WordEntity
import com.example.english_app.domain.srs.SM2Algorithm
import com.example.english_app.utils.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Calendar

data class WordWithRecord(
    val word: WordEntity,
    val record: LearningRecordEntity
)

data class ProgressStats(
    val totalWords: Int,
    val learnedWords: Int,
    val dueWords: Int,
    val totalReviews: Int,
    val accuracy: Float,
    val streak: Int
)

class LearningRepository(
    private val recordDao: LearningRecordDao,
    private val wordDao: WordDao,
    private val userPreferences: UserPreferences,
    private val userDao: UserDao
) {
    suspend fun initializeWordsForUser(userId: Int, setId: Int) {
        val words = wordDao.getBySetId(setId)
        words.forEach { word ->
            val existing = recordDao.findByWordAndUser(word.id, userId)
            if (existing == null) {
                recordDao.insert(LearningRecordEntity(wordId = word.id, userId = userId))
            }
        }
    }

    suspend fun getDueSession(userId: Int, limit: Int = 20): List<WordWithRecord> {
        val now = System.currentTimeMillis()
        val due = recordDao.getDueWords(userId, now).take(limit)
        return due.mapNotNull { record ->
            val word = wordDao.findById(record.wordId) ?: return@mapNotNull null
            WordWithRecord(word, record)
        }
    }

    suspend fun getNewWordsSession(userId: Int, limit: Int = 10): List<WordWithRecord> {
        val newRecords = recordDao.getNewWords(userId).take(limit)
        return newRecords.mapNotNull { record ->
            val word = wordDao.findById(record.wordId) ?: return@mapNotNull null
            WordWithRecord(word, record)
        }
    }

    suspend fun reviewWord(record: LearningRecordEntity, quality: Int): LearningRecordEntity {
        val result = SM2Algorithm.calculate(record, quality)
        val isCorrect = quality >= SM2Algorithm.QUALITY_GOOD
        val updated = record.copy(
            easeFactor = result.easeFactor,
            interval = result.interval,
            repetitions = result.repetitions,
            nextReviewDate = result.nextReviewDate,
            lastReviewDate = System.currentTimeMillis(),
            totalReviews = record.totalReviews + 1,
            correctReviews = if (isCorrect) record.correctReviews + 1 else record.correctReviews,
            isNew = false
        )
        recordDao.update(updated)
        updateStreak(userId = record.userId)
        return updated
    }

    private suspend fun updateStreak(userId: Int) {
        val user = userDao.findById(userId) ?: return
        val now = System.currentTimeMillis()
        val newStreak = when {
            user.lastStudyDate == 0L -> 1
            DateUtils.isSameDay(user.lastStudyDate, now) -> user.streak
            DateUtils.isSameDay(DateUtils.addDays(user.lastStudyDate, 1), now) -> user.streak + 1
            else -> 1
        }
        userDao.updateStreak(userId, newStreak, now)
    }

    suspend fun getStats(userId: Int): ProgressStats {
        val totalWords    = wordDao.countByUserId(userId)
        val learnedWords  = recordDao.countLearnedWords(userId)
        val dueWords      = recordDao.countDueWords(userId)
        val totalReviews  = recordDao.getTotalReviews(userId) ?: 0
        val totalCorrect  = recordDao.getTotalCorrect(userId) ?: 0
        val accuracy      = if (totalReviews > 0) totalCorrect.toFloat() / totalReviews else 0f
        val streak        = userDao.findById(userId)?.streak ?: 0
        return ProgressStats(totalWords, learnedWords, dueWords, totalReviews, accuracy, streak)
    }

    fun observeRecords(userId: Int): Flow<List<LearningRecordEntity>> =
        recordDao.observeByUserId(userId)

    suspend fun getRecentActivity(userId: Int, days: Int = 7): Map<String, Int> {
        val since = DateUtils.daysAgo(days)
        val records = recordDao.getReviewedSince(userId, since)
        return records.groupBy { DateUtils.formatDisplay(it.lastReviewDate) }
            .mapValues { it.value.size }
    }

    /**
     * Returns a list of 7 Float? values (Mon=0 … Sun=6).
     * Each value is the accuracy (0f-1f) of words last reviewed on that day.
     * Null means no reviews happened that day.
     */
    suspend fun getRetentionByDay(userId: Int): List<Float?> {
        val since = DateUtils.daysAgo(7)
        val records = recordDao.getReviewedSince(userId, since)
        val byDay = Array<MutableList<LearningRecordEntity>>(7) { mutableListOf() }
        records.forEach { record ->
            if (record.lastReviewDate > 0) {
                val cal = Calendar.getInstance().apply { timeInMillis = record.lastReviewDate }
                // Calendar.DAY_OF_WEEK: Sun=1..Sat=7 → Mon=0..Sun=6
                val dayIdx = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
                byDay[dayIdx].add(record)
            }
        }
        return byDay.map { recs ->
            if (recs.isEmpty()) null
            else {
                val total = recs.sumOf { it.totalReviews }
                val correct = recs.sumOf { it.correctReviews }
                if (total > 0) correct.toFloat() / total else null
            }
        }
    }
}




