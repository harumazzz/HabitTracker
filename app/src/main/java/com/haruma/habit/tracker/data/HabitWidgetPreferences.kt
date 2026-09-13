package com.haruma.habit.tracker.data

import android.content.Context

object HabitWidgetPreferences {

    private const val PREFS_NAME = "habit_widget_prefs"
    private const val KEY_PREFIX = "widget_habit_"

    fun getHabitId(context: Context, appWidgetId: Int): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt("$KEY_PREFIX$appWidgetId", -1)
    }

    fun setHabitId(context: Context, appWidgetId: Int, habitId: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt("$KEY_PREFIX$appWidgetId", habitId).apply()
    }

    fun removeWidget(context: Context, appWidgetId: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove("$KEY_PREFIX$appWidgetId").apply()
    }

    fun getAllWidgetIds(context: Context): List<Int> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.all.keys
            .filter { it.startsWith(KEY_PREFIX) }
            .mapNotNull { it.removePrefix(KEY_PREFIX).toIntOrNull() }
    }
}
