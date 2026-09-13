package com.haruma.habit.tracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CompletionDao {

    @Query("SELECT * FROM habit_completions WHERE completedDateEpochDay BETWEEN :from AND :to")
    fun observeInRange(from: Long, to: Long): Flow<List<HabitCompletionEntity>>

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId ORDER BY completedDateEpochDay DESC")
    fun observeForHabit(habitId: Int): Flow<List<HabitCompletionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(completion: HabitCompletionEntity)

    @Delete
    suspend fun delete(completion: HabitCompletionEntity)

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId AND completedDateEpochDay = :epochDay")
    suspend fun deleteForDay(habitId: Int, epochDay: Long)

    @Query("DELETE FROM habit_completions")
    suspend fun deleteAll()
}

