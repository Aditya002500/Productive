package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A captured screenshot / imported image and everything the pipeline derives
 * from it. Per the PRD, every AI-produced field stays user-editable and the
 * original image plus raw OCR text are always preserved.
 */
@Entity(tableName = "captures")
data class CaptureEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val timestamp: String,
    val imageUrl: String,
    val category: String, // e.g. "Documents", "Receipts / bills", "Code / technical"
    val extractedText: String = "",   // raw OCR text, preserved verbatim
    val status: String = "New",        // "New", "Processing", "Needs Review", "Processed", "Failed OCR"
    val summary: String = "",
    // --- AI / OCR metadata (Phase 2) ---
    val aiTitle: String = "",          // AI-suggested heading before user edits
    val detectedLanguage: String = "", // BCP-47 code from OCR/AI, e.g. "en"
    val tags: String = "",             // comma-separated user + AI tags
    val entities: String = "",         // JSON: dates, urls, phones, prices, etc.
    val confidence: Float = 0f,        // 0..1 extraction confidence
    val sourceType: String = "screenshot", // "screenshot", "imported image", "shared image"
    val isImportant: Boolean = false
)
