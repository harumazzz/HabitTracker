package com.haruma.habit.tracker.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.haruma.habit.tracker.data.HabitCompletionEntity
import com.haruma.habit.tracker.data.HabitEntity
import com.haruma.habit.tracker.data.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val repo: HabitRepository,
) : ViewModel() {

    val habits: Flow<PagingData<HabitEntity>> = repo.habitsPager().cachedIn(viewModelScope)

    val completedHabitIds: StateFlow<Set<Int>> = repo.observeCompletionsToday()
        .map { list -> list.map { it.habitId }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    val todayProgress: StateFlow<Pair<Int, Int>> = combine(
        repo.observeAllHabits(),
        repo.observeCompletionsToday(),
    ) { allHabits, completions ->
        val completedSet = completions.map { it.habitId }.toSet()
        val completedCount = allHabits.count { it.id in completedSet }
        Pair(completedCount, allHabits.size)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Pair(0, 0))

    val habitStreaks: StateFlow<Map<Int, Int>> = repo.observeCompletionsInRange(
        LocalDate.now().minusDays(365),
        LocalDate.now(),
    ).map { completions ->
        calculateStreaks(completions)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    fun toggleHabit(habitId: Int) {
        viewModelScope.launch {
            repo.toggleCompletion(habitId)
        }
    }

    private fun calculateStreaks(completions: List<HabitCompletionEntity>): Map<Int, Int> {
        val grouped = completions.groupBy { it.habitId }
        val todayEpochDay = LocalDate.now().toEpochDay()
        val result = mutableMapOf<Int, Int>()

        for ((habitId, list) in grouped) {
            val daysSet = list.map { it.completedDateEpochDay }.toSet()
            var streak = 0
            var currentCheckDay = if (daysSet.contains(todayEpochDay)) {
                todayEpochDay
            } else if (daysSet.contains(todayEpochDay - 1)) {
                todayEpochDay - 1
            } else {
                null
            }

            while (currentCheckDay != null && daysSet.contains(currentCheckDay)) {
                streak++
                currentCheckDay--
            }

            result[habitId] = streak
        }

        return result
    }
}
