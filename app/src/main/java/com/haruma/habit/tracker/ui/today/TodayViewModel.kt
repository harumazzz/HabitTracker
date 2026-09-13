package com.haruma.habit.tracker.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.haruma.habit.tracker.data.HabitCompletionEntity
import com.haruma.habit.tracker.data.HabitEntity
import com.haruma.habit.tracker.data.HabitRepository
import com.haruma.habit.tracker.data.HabitTemplate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

sealed interface TodayEvent {
    data class TargetAlreadyReached(val habitName: String) : TodayEvent
    data class HabitAdded(val habitName: String) : TodayEvent
}

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val repo: HabitRepository,
) : ViewModel() {

    private val _eventChannel = Channel<TodayEvent>(Channel.BUFFERED)
    val eventFlow: Flow<TodayEvent> = _eventChannel.receiveAsFlow()

    val habits: Flow<PagingData<HabitEntity>> = repo.habitsPager().cachedIn(viewModelScope)

    val todayCompletionsMap: StateFlow<Map<Int, Int>> = repo.observeCompletionsToday()
        .map { list -> list.associate { it.habitId to it.count } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val completedHabitIds: StateFlow<Set<Int>> = combine(
        repo.observeAllHabits(),
        repo.observeCompletionsToday(),
    ) { allHabits, completions ->
        val completionMap = completions.associate { it.habitId to it.count }
        allHabits.filter { habit ->
            val count = completionMap[habit.id] ?: 0
            count >= habit.targetCount
        }.map { it.id }.toSet()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    val todayProgress: StateFlow<Pair<Int, Int>> = combine(
        repo.observeAllHabits(),
        repo.observeCompletionsToday(),
    ) { allHabits, completions ->
        val activeHabits = allHabits.filter { !it.isArchived }
        val completionMap = completions.associate { it.habitId to it.count }
        val completedCount = activeHabits.count { habit ->
            val count = completionMap[habit.id] ?: 0
            count >= habit.targetCount
        }
        Pair(completedCount, activeHabits.size)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Pair(0, 0))

    val habitStreaks: StateFlow<Map<Int, Int>> = repo.observeCompletionsInRange(
        LocalDate.now().minusDays(365),
        LocalDate.now(),
    ).map { completions ->
        calculateStreaks(completions)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    fun incrementHabit(habit: HabitEntity) {
        viewModelScope.launch {
            val incremented = repo.incrementCompletion(habit.id, habit.targetCount)
            if (!incremented) {
                _eventChannel.send(TodayEvent.TargetAlreadyReached(habit.name))
            }
        }
    }

    fun addHabitFromTemplate(template: HabitTemplate, habitName: String) {
        viewModelScope.launch {
            val newHabit = HabitEntity(
                name = habitName,
                emoji = template.emoji,
                colorArgb = template.colorArgb,
                frequencyType = template.frequencyType,
                targetCount = template.targetCount,
            )
            repo.save(newHabit)
            _eventChannel.send(TodayEvent.HabitAdded(habitName))
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
