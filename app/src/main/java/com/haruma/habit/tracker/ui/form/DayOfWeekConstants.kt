package com.haruma.habit.tracker.ui.form

object DayOfWeekConstants {
    val ORDERED_DAYS = HabitDay.entries

    fun parseDays(targetDays: String): Set<HabitDay> {
        return targetDays.split(",")
            .mapNotNull { HabitDay.fromCode(it.trim()) }
            .toSet()
    }

    fun formatDays(days: Set<HabitDay>): String {
        return ORDERED_DAYS.filter { days.contains(it) }
            .joinToString(",") { it.code }
    }
}
