package com.example.english_app.data.local.dao

import androidx.room.*
import com.example.english_app.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserEntity): Long

    @Update
    suspend fun update(user: UserEntity)

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun observeById(id: Int): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun findById(id: Int): UserEntity?

    @Query("UPDATE users SET streak = :streak, lastStudyDate = :lastStudyDate WHERE id = :userId")
    suspend fun updateStreak(userId: Int, streak: Int, lastStudyDate: Long)

    @Query("SELECT dailyWordCount FROM users WHERE id = :userId LIMIT 1")
    suspend fun getDailyWordCount(userId: Int): Int?
}

