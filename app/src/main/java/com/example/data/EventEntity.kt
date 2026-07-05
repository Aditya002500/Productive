package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "events", indices = [Index(value = ["syncId"], unique = true)])
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val timeRange: String,
    val location: String = "",
    val day: Int,
    val monthName: String, // e.g. "October", "November"
    val year: Int = 2023,
    val color: Int = 0, // 0: primary teal, 1: secondary purple, 2: green, etc
    /** Non-null when this event was imported from Google Calendar; used to update it in place on re-import instead of duplicating. */
    val googleEventId: String? = null,
    val syncId: String = UUID.randomUUID().toString(),
    val updatedAt: Long = System.currentTimeMillis(),
    /** True when the user has manually marked this event as done. Permanent — does not reset at midnight. */
    val isDone: Boolean = false
)

