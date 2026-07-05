package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * A recurring habit that can be simple (Generic) or activity-typed (Running, Cycling, etc.).
 * Activity-typed habits track a metric value (distance, duration, volume) per completion
 * stored in [HabitLogEntity]. Scheduled via WorkManager periodic work keyed off [id].
 */
@Entity(tableName = "habits", indices = [Index(value = ["syncId"], unique = true)])
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val intervalHours: Int = 1,
    val isActive: Boolean = true,
    val createdAt: Long = 0L,
    val syncId: String = UUID.randomUUID().toString(),
    val updatedAt: Long = System.currentTimeMillis(),
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastCompletedEpochDay: Long = -1,
    /**
     * One of: "Generic", "Running", "Cycling", "Meditation", "Strength", "Yoga",
     * "Hydration", "Reading", "Sleep"
     */
    val habitType: String = "Generic",
    /** Unit for the tracked metric: "km", "mi", "ml", "min", "reps", "" for Generic */
    val metricUnit: String = "",
    /** Target value per session (e.g. 5.0 km, 8.0 glasses, 30.0 min). 0 = no specific goal. */
    val dailyGoalValue: Float = 0f,
    /** How many days per week the user aims to complete this habit (1–7). */
    val weeklyTargetDays: Int = 7
)

