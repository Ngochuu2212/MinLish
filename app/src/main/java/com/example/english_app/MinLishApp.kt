package com.example.english_app

import android.app.Application
import com.example.english_app.data.local.AppDatabase
import com.example.english_app.data.local.UserPreferences
import com.example.english_app.data.repository.AuthRepository
import com.example.english_app.data.repository.LearningRepository
import com.example.english_app.data.repository.VocabularyRepository
import com.example.english_app.notification.NotificationHelper

class MinLishApp : Application() {

    val database by lazy { AppDatabase.getInstance(this) }
    val userPreferences by lazy { UserPreferences(this) }

    val authRepository by lazy {
        AuthRepository(database.userDao(), userPreferences)
    }

    val vocabularyRepository by lazy {
        VocabularyRepository(database.vocabularySetDao(), database.wordDao())
    }

    val learningRepository by lazy {
        LearningRepository(database.learningRecordDao(), database.wordDao(), userPreferences, database.userDao())
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
    }
}

