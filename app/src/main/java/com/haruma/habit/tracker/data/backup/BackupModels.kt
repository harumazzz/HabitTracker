package com.haruma.habit.tracker.data.backup

import com.haruma.habit.tracker.data.HabitCompletionEntity
import com.haruma.habit.tracker.data.HabitEntity
import kotlinx.serialization.Serializable

@Serializable
data class BackupPayload(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val appVersion: String = "1.0.0",
    val habits: List<HabitBackupItem> = emptyList(),
    val completions: List<CompletionBackupItem> = emptyList(),
)

@Serializable
data class HabitBackupItem(
    val id: Int,
    val name: String,
    val emoji: String = "check",
    val colorArgb: Int = 0xFF6750A4.toInt(),
    val frequencyType: String = "DAILY",
    val targetDays: String = "",
    val targetCount: Int = 1,
    val createdAt: Long = 0L,
    val isArchived: Boolean = false,
    val reminderTimeMinutes: Int? = null,
)

@Serializable
data class CompletionBackupItem(
    val id: Int,
    val habitId: Int,
    val completedDateEpochDay: Long,
    val count: Int = 1,
)

fun HabitEntity.toBackupItem() = HabitBackupItem(
    id = id,
    name = name,
    emoji = emoji,
    colorArgb = colorArgb,
    frequencyType = frequencyType,
    targetDays = targetDays,
    targetCount = targetCount,
    createdAt = createdAt,
    isArchived = isArchived,
    reminderTimeMinutes = reminderTimeMinutes,
)

fun HabitBackupItem.toEntity() = HabitEntity(
    id = id,
    name = name,
    emoji = emoji,
    colorArgb = colorArgb,
    frequencyType = frequencyType,
    targetDays = targetDays,
    targetCount = targetCount,
    createdAt = createdAt,
    isArchived = isArchived,
    reminderTimeMinutes = reminderTimeMinutes,
)

fun HabitCompletionEntity.toBackupItem() = CompletionBackupItem(
    id = id,
    habitId = habitId,
    completedDateEpochDay = completedDateEpochDay,
    count = count,
)

fun CompletionBackupItem.toEntity() = HabitCompletionEntity(
    id = id,
    habitId = habitId,
    completedDateEpochDay = completedDateEpochDay,
    count = count,
)

data class DriveBackupMetadata(
    val fileId: String,
    val fileName: String,
    val modifiedTimeMillis: Long,
    val sizeBytes: Long,
    val habitsCount: Int,
    val completionsCount: Int,
)
