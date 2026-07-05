package com.example.data

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/** Schedules the once-a-day briefing notification for a fixed 8:00 AM local time. */
object DailyBriefingScheduler {
    private const val WORK_NAME = "daily_briefing"
    private const val BRIEFING_HOUR = 8

    fun schedule(context: Context) {
        val now = LocalDateTime.now()
        var next = now.withHour(BRIEFING_HOUR).withMinute(0).withSecond(0).withNano(0)
        if (!next.isAfter(now)) next = next.plusDays(1)
        val initialDelayMs = Duration.between(now, next).toMillis()

        val request = PeriodicWorkRequestBuilder<DailyBriefingWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
