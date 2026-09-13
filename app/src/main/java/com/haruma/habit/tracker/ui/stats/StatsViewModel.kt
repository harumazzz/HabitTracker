package com.haruma.habit.tracker.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haruma.habit.tracker.data.HabitCompletionEntity
import com.haruma.habit.tracker.data.HabitEntity
import com.haruma.habit.tracker.data.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repo: HabitRepository,
) : ViewModel() {

    private val selectedRangeFlow = MutableStateFlow(StatsTimeRange.WEEKLY)

    val uiState: StateFlow<StatsUiState> = combine(
        repo.observeAllHabits(),
        repo.observeCompletionsInRange(LocalDate.now().minusDays(365), LocalDate.now()),
        selectedRangeFlow,
    ) { allHabits, completions, range ->
        val activeHabits = allHabits.filter { !it.isArchived }
        buildUiState(activeHabits, completions, range)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StatsUiState(isLoading = true),
    )

    fun setTimeRange(range: StatsTimeRange) {
        selectedRangeFlow.update { range }
    }

    private fun buildUiState(
        activeHabits: List<HabitEntity>,
        completions: List<HabitCompletionEntity>,
        range: StatsTimeRange,
    ): StatsUiState {
        val today = LocalDate.now()
        val todayEpochDay = today.toEpochDay()

        val activityMap = mutableMapOf<LocalDate, Int>()
        val dayHabitCompletions = mutableMapOf<Long, MutableSet<Int>>()

        for (completion in completions) {
            val date = LocalDate.ofEpochDay(completion.completedDateEpochDay)
            activityMap[date] = (activityMap[date] ?: 0) + 1
            dayHabitCompletions.getOrPut(completion.completedDateEpochDay) { mutableSetOf() }.add(completion.habitId)
        }

        val completedEpochDays = dayHabitCompletions.keys.sorted()
        val completedDaysSet = completedEpochDays.toSet()

        var currentStreak = 0
        var streakCheckDay = if (completedDaysSet.contains(todayEpochDay)) {
            todayEpochDay
        } else if (completedDaysSet.contains(todayEpochDay - 1)) {
            todayEpochDay - 1
        } else {
            null
        }

        while (streakCheckDay != null && completedDaysSet.contains(streakCheckDay)) {
            currentStreak++
            streakCheckDay--
        }

        var longestStreak = 0
        var tempStreak = 0
        var prevDay: Long? = null

        for (epochDay in completedEpochDays) {
            if (prevDay == null || epochDay == prevDay + 1) {
                tempStreak++
            } else {
                tempStreak = 1
            }
            prevDay = epochDay
            if (tempStreak > longestStreak) {
                longestStreak = tempStreak
            }
        }

        val rangeDays = when (range) {
            StatsTimeRange.WEEKLY -> {
                val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                (0L..6L).map { monday.plusDays(it) }
            }
            StatsTimeRange.MONTHLY -> {
                val firstOfMonth = today.with(TemporalAdjusters.firstDayOfMonth())
                val count = today.lengthOfMonth()
                (0L until count).map { firstOfMonth.plusDays(it) }
            }
        }

        val pastOrTodayRangeDays = rangeDays.filter { !it.isAfter(today) }

        val rangeDailyStats = rangeDays.map { date ->
            val epochDay = date.toEpochDay()
            val completedCount = dayHabitCompletions[epochDay]?.size ?: 0
            DayCompletionStat(
                date = date,
                completedCount = completedCount,
                totalActiveHabits = activeHabits.size,
            )
        }

        val totalPossibleCompletions = activeHabits.size * pastOrTodayRangeDays.size
        val totalActualCompletions = pastOrTodayRangeDays.sumOf { date ->
            dayHabitCompletions[date.toEpochDay()]?.size ?: 0
        }

        val overallCompletionRate = if (totalPossibleCompletions > 0) {
            (totalActualCompletions.toFloat() / totalPossibleCompletions).coerceIn(0f, 1f)
        } else {
            0f
        }

        val habitCompletionsMap = completions.groupBy { it.habitId }

        val habitStatsList = activeHabits.map { habit ->
            val habitList = habitCompletionsMap[habit.id].orEmpty()
            val habitDaysSet = habitList.map { it.completedDateEpochDay }.toSet()

            var hStreak = 0
            var hCheckDay = if (habitDaysSet.contains(todayEpochDay)) {
                todayEpochDay
            } else if (habitDaysSet.contains(todayEpochDay - 1)) {
                todayEpochDay - 1
            } else {
                null
            }
            while (hCheckDay != null && habitDaysSet.contains(hCheckDay)) {
                hStreak++
                hCheckDay--
            }

            val completedInRange = pastOrTodayRangeDays.count { habitDaysSet.contains(it.toEpochDay()) }
            val targetDays = pastOrTodayRangeDays.size
            val rate = if (targetDays > 0) {
                (completedInRange.toFloat() / targetDays).coerceIn(0f, 1f)
            } else {
                0f
            }

            HabitStatItem(
                habitId = habit.id,
                name = habit.name,
                emoji = habit.emoji,
                colorArgb = habit.colorArgb,
                completedCount = completedInRange,
                targetDaysCount = targetDays,
                completionRate = rate,
                currentStreak = hStreak,
            )
        }

        return StatsUiState(
            isLoading = false,
            selectedRange = range,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            completionRate = overallCompletionRate,
            activityDays = activityMap,
            rangeDailyStats = rangeDailyStats,
            habitStatsList = habitStatsList,
        )
    }
}
