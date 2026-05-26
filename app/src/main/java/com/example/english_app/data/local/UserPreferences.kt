package com.example.english_app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "minlish_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        val KEY_USER_ID = intPreferencesKey("user_id")
        val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_LAST_STUDY_DATE = longPreferencesKey("last_study_date")
        val KEY_STREAK = intPreferencesKey("streak")
    }

    val userId: Flow<Int> = context.dataStore.data.map { it[KEY_USER_ID] ?: -1 }
    val userEmail: Flow<String> = context.dataStore.data.map { it[KEY_USER_EMAIL] ?: "" }
    val userName: Flow<String> = context.dataStore.data.map { it[KEY_USER_NAME] ?: "" }
    val streak: Flow<Int> = context.dataStore.data.map { it[KEY_STREAK] ?: 0 }
    val lastStudyDate: Flow<Long> = context.dataStore.data.map { it[KEY_LAST_STUDY_DATE] ?: 0L }

    suspend fun saveUser(id: Int, email: String, name: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_ID] = id
            prefs[KEY_USER_EMAIL] = email
            prefs[KEY_USER_NAME] = name
        }
    }

    suspend fun updateStreak(streak: Int, lastStudyDate: Long) {
        context.dataStore.edit { prefs ->
            prefs[KEY_STREAK] = streak
            prefs[KEY_LAST_STUDY_DATE] = lastStudyDate
        }
    }

    suspend fun clearUser() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_USER_ID)
            prefs.remove(KEY_USER_EMAIL)
            prefs.remove(KEY_USER_NAME)
            prefs.remove(KEY_STREAK)
            prefs.remove(KEY_LAST_STUDY_DATE)
        }
    }
}

