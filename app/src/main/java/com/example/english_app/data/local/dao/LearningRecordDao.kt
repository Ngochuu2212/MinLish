package com.example.english_app.data.local.dao

import androidx.room.*
import com.example.english_app.data.local.entity.LearningRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LearningRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: LearningRecordEntity): Long

    @Update
    suspend fun update(record: LearningRecordEntity)

    @Query("SELECT * FROM learning_records WHERE wordId = :wordId AND userId = :userId LIMIT 1")
    suspend fun findByWordAndUser(wordId: Int, userId: Int): LearningRecordEntity?

    @Query("SELECT * FROM learning_records WHERE userId = :userId AND nextReviewDate <= :now ORDER BY nextReviewDate ASC")
    suspend fun getDueWords(userId: Int, now: Long = System.currentTimeMillis()): List<LearningRecordEntity>

    @Query("SELECT * FROM learning_records WHERE userId = :userId AND isNew = 1")
    suspend fun getNewWords(userId: Int): List<LearningRecordEntity>

    @Query("SELECT COUNT(*) FROM learning_records WHERE userId = :userId AND isNew = 0")
    suspend fun countLearnedWords(userId: Int): Int

    @Query("SELECT COUNT(*) FROM learning_records WHERE userId = :userId AND nextReviewDate <= :now")
    suspend fun countDueWords(userId: Int, now: Long = System.currentTimeMillis()): Int

    @Query("SELECT * FROM learning_records WHERE userId = :userId ORDER BY lastReviewDate DESC")
    fun observeByUserId(userId: Int): Flow<List<LearningRecordEntity>>

    @Query("SELECT SUM(totalReviews) FROM learning_records WHERE userId = :userId")
    suspend fun getTotalReviews(userId: Int): Int?

    @Query("SELECT SUM(correctReviews) FROM learning_records WHERE userId = :userId")
    suspend fun getTotalCorrect(userId: Int): Int?

    @Query("SELECT * FROM learning_records WHERE userId = :userId AND lastReviewDate >= :since")
    suspend fun getReviewedSince(userId: Int, since: Long): List<LearningRecordEntity>
}

