package com.haruma.habit.tracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val emoji: String = "check",
    val colorArgb: Int = 0xFF6750A4.toInt(),
    val frequencyType: String = "DAILY",
    val targetDays: String = "",
    val targetCount: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false,
    val reminderTimeMinutes: Int? = null,
)
