package com.haruma.habit.tracker.ui.stats

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haruma.habit.tracker.R
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

        val achievements = calculateAchievements(
            activeHabits = activeHabits,
            completions = completions,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            dayHabitCompletions = dayHabitCompletions,
        )

        return StatsUiState(
            isLoading = false,
            selectedRange = range,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            completionRate = overallCompletionRate,
            activityDays = activityMap,
            rangeDailyStats = rangeDailyStats,
            habitStatsList = habitStatsList,
            achievements = achievements,
        )
    }

    private fun calculateAchievements(
        activeHabits: List<HabitEntity>,
        completions: List<HabitCompletionEntity>,
        currentStreak: Int,
        longestStreak: Int,
        dayHabitCompletions: Map<Long, Set<Int>>,
    ): List<Achievement> {
        val totalCompletions = completions.size
        val maxStreak = maxOf(currentStreak, longestStreak)
        val hasPerfectDay = activeHabits.isNotEmpty() && dayHabitCompletions.values.any { it.size >= activeHabits.size }

        return listOf(
            Achievement(
                id = "first_step",
                titleResId = R.string.badge_first_step_title,
                descResId = R.string.badge_first_step_desc,
                icon = Icons.Default.Star,
                color = Color(0xFFFFB300),
                isUnlocked = totalCompletions >= 1,
                progress = if (totalCompletions >= 1) 1f else 0f,
                progressLabel = if (totalCompletions >= 1) "1/1" else "0/1",
            ),
            Achievement(
                id = "streak_3",
                titleResId = R.string.badge_streak_3_title,
                descResId = R.string.badge_streak_3_desc,
                icon = Icons.Default.LocalFireDepartment,
                color = Color(0xFFFF7043),
                isUnlocked = maxStreak >= 3,
                progress = (maxStreak.toFloat() / 3f).coerceIn(0f, 1f),
                progressLabel = "${minOf(maxStreak, 3)}/3",
            ),
            Achievement(
                id = "streak_7",
                titleResId = R.string.badge_streak_7_title,
                descResId = R.string.badge_streak_7_desc,
                icon = Icons.Default.EmojiEvents,
                color = Color(0xFFFFA000),
                isUnlocked = maxStreak >= 7,
                progress = (maxStreak.toFloat() / 7f).coerceIn(0f, 1f),
                progressLabel = "${minOf(maxStreak, 7)}/7",
            ),
            Achievement(
                id = "streak_30",
                titleResId = R.string.badge_streak_30_title,
                descResId = R.string.badge_streak_30_desc,
                icon = Icons.Default.MilitaryTech,
                color = Color(0xFF7E57C2),
                isUnlocked = maxStreak >= 30,
                progress = (maxStreak.toFloat() / 30f).coerceIn(0f, 1f),
                progressLabel = "${minOf(maxStreak, 30)}/30",
            ),
            Achievement(
                id = "architect",
                titleResId = R.string.badge_architect_title,
                descResId = R.string.badge_architect_desc,
                icon = Icons.Default.WorkspacePremium,
                color = Color(0xFF26A69A),
                isUnlocked = activeHabits.size >= 3,
                progress = (activeHabits.size.toFloat() / 3f).coerceIn(0f, 1f),
                progressLabel = "${minOf(activeHabits.size, 3)}/3",
            ),
            Achievement(
                id = "perfect_day",
                titleResId = R.string.badge_perfect_day_title,
                descResId = R.string.badge_perfect_day_desc,
                icon = Icons.Default.AutoAwesome,
                color = Color(0xFFEC407A),
                isUnlocked = hasPerfectDay,
                progress = if (hasPerfectDay) 1f else 0f,
                progressLabel = if (hasPerfectDay) "1/1" else "0/1",
            ),
            Achievement(
                id = "century",
                titleResId = R.string.badge_century_title,
                descResId = R.string.badge_century_desc,
                icon = Icons.Default.FitnessCenter,
                color = Color(0xFF42A5F5),
                isUnlocked = totalCompletions >= 100,
                progress = (totalCompletions.toFloat() / 100f).coerceIn(0f, 1f),
                progressLabel = "${minOf(totalCompletions, 100)}/100",
            ),
        )
    }
}

