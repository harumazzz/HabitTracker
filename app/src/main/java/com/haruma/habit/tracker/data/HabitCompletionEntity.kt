package com.haruma.habit.tracker.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "habit_completions",
    foreignKeys = [ForeignKey(
        entity = HabitEntity::class,
        parentColumns = ["id"],
        childColumns = ["habitId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("habitId")],
)
data class HabitCompletionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val habitId: Int,
    val completedDateEpochDay: Long,
    val count: Int = 1,
)
