package com.example.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/** Posts a once-daily notification summarizing today's open tasks/events. Fires on the fixed schedule set up by [DailyBriefingScheduler]. */
class DailyBriefingWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    companion object {
        private const val CHANNEL_ID = "daily_briefing"
        private const val NOTIFICATION_ID = 9001
    }

    override suspend fun doWork(): Result {
        val settingsRepository = SettingsRepository(applicationContext)
        if (!settingsRepository.notificationsEnabled.first()) return Result.success()

        val dao = AppDatabase.getDatabase(applicationContext).appDao()
        val openTasks = dao.getAllTasksFlow().first().count { !it.isCompleted }

        val today = LocalDate.now()
        val todayMonthName = today.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
        val todaysEvents = dao.getAllEventsFlow().first()
            .count { it.monthName.equals(todayMonthName, ignoreCase = true) && it.day == today.dayOfMonth }

        if (openTasks == 0 && todaysEvents == 0) return Result.success()

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(CHANNEL_ID, "Daily Briefing", NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Today: $openTasks open task${if (openTasks == 1) "" else "s"}, $todaysEvents event${if (todaysEvents == 1) "" else "s"}")
            .setContentText("Tap to open CaptureFlow")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        try {
            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // Notification permission not granted — silently skip.
        }

        return Result.success()
    }
}
