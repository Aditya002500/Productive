package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "tasks", indices = [Index(value = ["syncId"], unique = true)])
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val isCompleted: Boolean = false,
    val priority: String = "Medium", // High, Medium, Low
    val dueDate: String = "",
    val dueTime: String = "",
    val notes: String = "",
    /** Stable cross-device identity used as the Firestore document id (Room's [id] is per-device). */
    val syncId: String = UUID.randomUUID().toString(),
    /** Epoch millis of the last local write; drives last-write-wins conflict resolution. */
    val updatedAt: Long = System.currentTimeMillis()
)
