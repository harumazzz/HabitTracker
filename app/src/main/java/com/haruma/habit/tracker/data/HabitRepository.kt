package com.haruma.habit.tracker.data

import android.content.Context
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.haruma.habit.tracker.ui.widget.HabitAppWidgetProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val habitDao: HabitDao,
    private val completionDao: CompletionDao,
) {

    fun habitsPager(): Flow<PagingData<HabitEntity>> = Pager(
        config = PagingConfig(pageSize = 20, enablePlaceholders = false),
        pagingSourceFactory = { habitDao.pagingSource() },
    ).flow

    fun observeAllHabits(): Flow<List<HabitEntity>> = habitDao.observeAll()

    fun observeCompletionsToday(): Flow<List<HabitCompletionEntity>> {
        val today = LocalDate.now().toEpochDay()
        return completionDao.observeInRange(today, today)
    }

    fun observeCompletionsInRange(from: LocalDate, to: LocalDate): Flow<List<HabitCompletionEntity>> =
        completionDao.observeInRange(from.toEpochDay(), to.toEpochDay())

    suspend fun toggleCompletion(habitId: Int) {
        val today = LocalDate.now().toEpochDay()
        val existing = completionDao.observeForHabit(habitId).first()
            .firstOrNull { it.completedDateEpochDay == today }
        if (existing != null) {
            completionDao.delete(existing)
        } else {
            completionDao.upsert(HabitCompletionEntity(habitId = habitId, completedDateEpochDay = today))
        }
        HabitAppWidgetProvider.updateAllWidgets(context)
    }

    suspend fun save(entity: HabitEntity): Long {
        val id = habitDao.upsert(entity)
        HabitAppWidgetProvider.updateAllWidgets(context)
        return id
    }

    suspend fun delete(entity: HabitEntity) {
        habitDao.delete(entity)
        HabitAppWidgetProvider.updateAllWidgets(context)
    }

    suspend fun archive(id: Int) {
        val entity = habitDao.getById(id) ?: return
        habitDao.upsert(entity.copy(isArchived = true))
        HabitAppWidgetProvider.updateAllWidgets(context)
    }

    suspend fun getById(id: Int): HabitEntity? = habitDao.getById(id)

    suspend fun clearAllData() {
        completionDao.deleteAll()
        habitDao.deleteAll()
        HabitAppWidgetProvider.updateAllWidgets(context)
    }
}

