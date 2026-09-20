package com.haruma.habit.tracker.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Transaction

@Database(
    entities = [HabitEntity::class, HabitCompletionEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun completionDao(): CompletionDao

    @Transaction
    open suspend fun restoreBackup(
        habits: List<HabitEntity>,
        completions: List<HabitCompletionEntity>,
    ) {
        completionDao().deleteAll()
        habitDao().deleteAll()
        habitDao().insertAll(habits)
        completionDao().insertAll(completions)
    }
}
