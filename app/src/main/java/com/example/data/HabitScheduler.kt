package com.example.data

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/** Schedules/cancels the recurring WorkManager reminder for a habit — shared by locally-created habits (AppViewModel) and ones pulled in via cloud sync (CloudSyncRepository). */
object HabitScheduler {
    private fun workName(habitId: Int) = "habit_reminder_$habitId"

    fun schedule(context: Context, habit: HabitEntity) {
        val request = PeriodicWorkRequestBuilder<HabitReminderWorker>(
            habit.intervalHours.toLong(), TimeUnit.HOURS
        ).setInputData(
            Data.Builder()
                .putInt(HabitReminderWorker.KEY_HABIT_ID, habit.id)
                .putString(HabitReminderWorker.KEY_HABIT_TITLE, habit.title)
                .build()
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            workName(habit.id),
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancel(context: Context, habitId: Int) {
        WorkManager.getInstance(context).cancelUniqueWork(workName(habitId))
    }
}
