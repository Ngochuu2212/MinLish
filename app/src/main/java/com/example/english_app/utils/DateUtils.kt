package com.example.english_app.utils

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object DateUtils {
    private val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    fun todayStart(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun todayKey(): String = dayFormat.format(Date())

    fun formatDisplay(timestamp: Long): String = displayFormat.format(Date(timestamp))

    fun formatTime(timestamp: Long): String = timeFormat.format(Date(timestamp))

    fun daysAgo(days: Int): Long = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(days.toLong())

    fun daysBetween(from: Long, to: Long): Int {
        val diff = to - from
        return TimeUnit.MILLISECONDS.toDays(diff).toInt()
    }

    fun addDays(timestamp: Long, days: Int): Long =
        timestamp + TimeUnit.DAYS.toMillis(days.toLong())

    fun isSameDay(t1: Long, t2: Long): Boolean {
        return dayFormat.format(Date(t1)) == dayFormat.format(Date(t2))
    }
}

