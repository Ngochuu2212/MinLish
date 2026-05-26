package com.example.english_app.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.english_app.MinLishApp
import com.example.english_app.data.local.UserPreferences
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class DailyReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val app = applicationContext as MinLishApp
        val userId = app.userPreferences.userId.first()
        if (userId <= 0) return Result.success()

        val dueCount = app.database.learningRecordDao()
            .countDueWords(userId, System.currentTimeMillis())

        NotificationHelper.sendDailyReminder(applicationContext, dueCount)
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "daily_reminder"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<DailyReminderWorker>(1, TimeUnit.DAYS)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}

