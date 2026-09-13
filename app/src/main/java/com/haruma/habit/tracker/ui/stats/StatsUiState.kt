package com.haruma.habit.tracker.ui.stats

import java.time.LocalDate

data class StatsUiState(
    val isLoading: Boolean = true,
    val selectedRange: StatsTimeRange = StatsTimeRange.WEEKLY,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val completionRate: Float = 0f,
    val activityDays: Map<LocalDate, Int> = emptyMap(),
    val rangeDailyStats: List<DayCompletionStat> = emptyList(),
    val habitStatsList: List<HabitStatItem> = emptyList(),
    val achievements: List<Achievement> = emptyList(),
)
