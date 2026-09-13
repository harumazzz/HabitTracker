package com.haruma.habit.tracker.ui.stats

data class HabitStatItem(
    val habitId: Int,
    val name: String,
    val emoji: String,
    val colorArgb: Int,
    val completedCount: Int,
    val targetDaysCount: Int,
    val completionRate: Float,
    val currentStreak: Int,
)
