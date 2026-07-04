package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val timeRange: String,
    val location: String = "",
    val day: Int,
    val monthName: String, // e.g. "October", "November"
    val year: Int = 2023,
    val color: Int = 0 // 0: primary teal, 1: secondary purple, 2: green, etc
)
