package com.example.english_app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true)])
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val email: String,
    val passwordHash: String,
    val name: String,
    val learningGoal: String = "General",
    val level: String = "A1",
    val dailyWordCount: Int = 10,
    val createdAt: Long = System.currentTimeMillis(),
    val streak: Int = 0,
    val lastStudyDate: Long = 0L,
    @ColumnInfo(defaultValue = "") val avatarUri: String = ""
)

