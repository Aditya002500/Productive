package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "sub_tasks", indices = [Index(value = ["syncId"], unique = true)])
data class SubTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val taskId: Int,
    val title: String,
    val isCompleted: Boolean = false,
    /** Stable per-subtask key used when embedding subtasks inside their parent task's Firestore doc. */
    val syncId: String = UUID.randomUUID().toString(),
    val updatedAt: Long = System.currentTimeMillis()
)
