package com.example.english_app.data.local.dao

import androidx.room.*
import com.example.english_app.data.local.entity.VocabularySetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VocabularySetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(set: VocabularySetEntity): Long

    @Update
    suspend fun update(set: VocabularySetEntity)

    @Delete
    suspend fun delete(set: VocabularySetEntity)

    @Query("SELECT * FROM vocabulary_sets WHERE userId = :userId ORDER BY updatedAt DESC")
    fun observeByUserId(userId: Int): Flow<List<VocabularySetEntity>>

    @Query("SELECT * FROM vocabulary_sets WHERE id = :id LIMIT 1")
    suspend fun findById(id: Int): VocabularySetEntity?

    @Query("SELECT * FROM vocabulary_sets WHERE userId = :userId ORDER BY updatedAt DESC")
    suspend fun getByUserId(userId: Int): List<VocabularySetEntity>
}

