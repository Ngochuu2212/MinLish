package com.example.english_app.data.local.dao

import androidx.room.*
import com.example.english_app.data.local.entity.WordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(word: WordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(words: List<WordEntity>)

    @Update
    suspend fun update(word: WordEntity)

    @Delete
    suspend fun delete(word: WordEntity)

    @Query("SELECT * FROM words WHERE setId = :setId ORDER BY createdAt ASC")
    fun observeBySetId(setId: Int): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE setId = :setId ORDER BY createdAt ASC")
    suspend fun getBySetId(setId: Int): List<WordEntity>

    @Query("SELECT * FROM words WHERE id = :id LIMIT 1")
    suspend fun findById(id: Int): WordEntity?

    @Query("SELECT COUNT(*) FROM words WHERE setId = :setId")
    suspend fun countBySetId(setId: Int): Int

    @Query("SELECT COUNT(*) FROM words WHERE setId IN (SELECT id FROM vocabulary_sets WHERE userId = :userId)")
    suspend fun countByUserId(userId: Int): Int

    @Query("SELECT * FROM words WHERE setId IN (SELECT id FROM vocabulary_sets WHERE userId = :userId)")
    suspend fun getAllByUserId(userId: Int): List<WordEntity>

    @Query("UPDATE words SET isBookmarked = :isBookmarked WHERE id = :wordId")
    suspend fun setBookmarked(wordId: Int, isBookmarked: Boolean)
}

