package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "notes", indices = [Index(value = ["syncId"], unique = true)])
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val date: String,
    val colorTag: Int = 0,
    /** Comma-separated URIs of reference photos (e.g. the capture this note was generated from). */
    val referenceImageUrls: String = "",
    /** Comma-separated user-created tags. */
    val tags: String = "",
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    /** Multiplier applied to the base content font size (e.g. 0.85 = Small, 1.5 = XL). */
    val fontScale: Float = 1.0f,
    /** ARGB text color; 0 means "use the current theme's default text color". */
    val textColorArgb: Int = 0,
    /** User-triggered AI-generated summary of this note's content; empty until generated. */
    val aiSummary: String = "",
    val syncId: String = UUID.randomUUID().toString(),
    val updatedAt: Long = System.currentTimeMillis()
)
