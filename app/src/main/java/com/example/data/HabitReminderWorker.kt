package com.example.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first

/** Posts a "time to: {habit}" reminder. Fires on WorkManager's periodic schedule set up in AppViewModel. */
class HabitReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_HABIT_ID = "habit_id"
        const val KEY_HABIT_TITLE = "habit_title"
        private const val CHANNEL_ID = "habit_reminders"
    }

    override suspend fun doWork(): Result {
        val settingsRepository = SettingsRepository(applicationContext)
        if (!settingsRepository.notificationsEnabled.first()) return Result.success()

        val habitId = inputData.getInt(KEY_HABIT_ID, 0)
        val title = inputData.getString(KEY_HABIT_TITLE) ?: return Result.success()

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(CHANNEL_ID, "Habit Reminders", NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Time to: $title")
            .setContentText("Tap to open CaptureFlow")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        try {
            notificationManager.notify(habitId, notification)
        } catch (_: SecurityException) {
            // Notification permission not granted — silently skip.
        }

        return Result.success()
    }
}
