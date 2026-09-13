package com.haruma.habit.tracker.ui.stats

import java.time.LocalDate

data class DayCompletionStat(
    val date: LocalDate,
    val completedCount: Int,
    val totalActiveHabits: Int,
)
