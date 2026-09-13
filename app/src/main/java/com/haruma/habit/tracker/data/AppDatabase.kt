package com.haruma.habit.tracker.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [HabitEntity::class, HabitCompletionEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun completionDao(): CompletionDao
}
