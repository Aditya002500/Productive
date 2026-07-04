package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "captures")
data class CaptureEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val timestamp: String,
    val imageUrl: String,
    val category: String, // "UI Ref", "Expense", "Snippet"
    val extractedText: String = "",
    val status: String = "New", // "New", "Needs Review", "Processed", "Failed OCR"
    val summary: String = ""
)
