package com.example.english_app.notification

import android.content.Context
import androidx.work.*
import com.example.english_app.MinLishApp
import com.example.english_app.data.local.UserPreferences
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.concurrent.TimeUnit

class DailyReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val app = applicationContext as MinLishApp
        val userId = app.userPreferences.userId.first()
        if (userId <= 0) return Result.success()

        val notifEnabled = app.userPreferences.notifEnabled.first()
        if (!notifEnabled) return Result.success()

        val dueCount = app.database.learningRecordDao()
            .countDueWords(userId, System.currentTimeMillis())

        NotificationHelper.sendDailyReminder(applicationContext, dueCount)
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "daily_reminder"

        fun schedule(context: Context, hour: Int = 8, minute: Int = 0) {
            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                // If the time has already passed today, schedule for tomorrow
                if (before(now)) add(Calendar.DAY_OF_YEAR, 1)
            }
            val initialDelay = target.timeInMillis - now.timeInMillis

            val request = PeriodicWorkRequestBuilder<DailyReminderWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}

