package com.example.data.sync

import com.example.data.CaptureEntity
import com.example.data.EventEntity
import com.example.data.HabitEntity
import com.example.data.NoteEntity
import com.example.data.SubTaskEntity
import com.example.data.TaskEntity

/** Field-for-field Firestore document shapes, shared between [CloudSyncRepository] and [ImageUploadWorker] so the two stay in sync. */

internal fun taskToFirestoreMap(task: TaskEntity, subTasks: List<SubTaskEntity>): Map<String, Any?> = mapOf(
    "syncId" to task.syncId,
    "title" to task.title,
    "isCompleted" to task.isCompleted,
    "priority" to task.priority,
    "dueDate" to task.dueDate,
    "dueTime" to task.dueTime,
    "notes" to task.notes,
    "updatedAt" to task.updatedAt,
    "subtasks" to subTasks.map { st ->
        mapOf(
            "syncId" to st.syncId,
            "title" to st.title,
            "isCompleted" to st.isCompleted,
            "updatedAt" to st.updatedAt
        )
    }
)

internal fun noteToFirestoreMap(note: NoteEntity): Map<String, Any?> = mapOf(
    "syncId" to note.syncId,
    "title" to note.title,
    "content" to note.content,
    "date" to note.date,
    "colorTag" to note.colorTag,
    "referenceImageUrls" to note.referenceImageUrls,
    "tags" to note.tags,
    "isBold" to note.isBold,
    "isItalic" to note.isItalic,
    "fontScale" to note.fontScale.toDouble(),
    "textColorArgb" to note.textColorArgb,
    "aiSummary" to note.aiSummary,
    "updatedAt" to note.updatedAt
)

internal fun captureToFirestoreMap(capture: CaptureEntity): Map<String, Any?> = mapOf(
    "syncId" to capture.syncId,
    "title" to capture.title,
    "timestamp" to capture.timestamp,
    "imageUrl" to capture.imageUrl,
    "category" to capture.category,
    "extractedText" to capture.extractedText,
    "status" to capture.status,
    "summary" to capture.summary,
    "aiTitle" to capture.aiTitle,
    "detectedLanguage" to capture.detectedLanguage,
    "tags" to capture.tags,
    "entities" to capture.entities,
    "confidence" to capture.confidence.toDouble(),
    "sourceType" to capture.sourceType,
    "isImportant" to capture.isImportant,
    "updatedAt" to capture.updatedAt
)

internal fun eventToFirestoreMap(event: EventEntity): Map<String, Any?> = mapOf(
    "syncId" to event.syncId,
    "title" to event.title,
    "timeRange" to event.timeRange,
    "location" to event.location,
    "day" to event.day,
    "monthName" to event.monthName,
    "year" to event.year,
    "color" to event.color,
    "googleEventId" to event.googleEventId,
    "updatedAt" to event.updatedAt
)

internal fun habitToFirestoreMap(habit: HabitEntity): Map<String, Any?> = mapOf(
    "syncId" to habit.syncId,
    "title" to habit.title,
    "intervalHours" to habit.intervalHours,
    "isActive" to habit.isActive,
    "createdAt" to habit.createdAt,
    "updatedAt" to habit.updatedAt,
    "currentStreak" to habit.currentStreak,
    "longestStreak" to habit.longestStreak,
    "lastCompletedEpochDay" to habit.lastCompletedEpochDay
)
