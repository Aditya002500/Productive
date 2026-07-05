package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Records a single completion of a [HabitEntity] on a specific calendar day.
 * Multiple logs per day are allowed (e.g. two runs in one day).
 */
@Entity(
    tableName = "habit_logs",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["habitId"]), Index(value = ["epochDay"])]
)
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    /** References [HabitEntity.id]. Cascades on delete so logs are removed when the habit is deleted. */
    val habitId: Int,
    /** java.time.LocalDate.now().toEpochDay() — the calendar day this completion belongs to. */
    val epochDay: Long,
    /**
     * The measured metric for this session, in [HabitEntity.metricUnit]:
     * - Running/Cycling: kilometres or miles
     * - Hydration: millilitres
     * - Meditation/Yoga/Reading/Sleep: minutes
     * - Strength: total reps
     * - Generic: always 0
     */
    val metricValue: Float = 0f,
    /** Optional free-text note ("felt great!", "rainy run"). */
    val note: String = "",
    val loggedAt: Long = System.currentTimeMillis()
)
