package com.haruma.habit.tracker.ui.widget

import androidx.annotation.StringRes
import com.haruma.habit.tracker.R

object HabitWidgetHelper {

    @StringRes
    fun getCategoryResId(iconKey: String): Int {
        return when (iconKey.lowercase()) {
            "water", "bedtime", "restaurant" -> R.string.widget_category_health
            "fitness", "run", "bike" -> R.string.widget_category_exercise
            "book", "edit", "lightbulb" -> R.string.widget_category_study
            "savings" -> R.string.widget_category_finance
            "meditation" -> R.string.widget_category_mindfulness
            else -> R.string.widget_category_lifestyle
        }
    }

    @StringRes
    fun getGrowthStageResId(streak: Int): Int {
        return when {
            streak < 7 -> R.string.widget_stage_seed
            streak < 21 -> R.string.widget_stage_sprout
            streak < 66 -> R.string.widget_stage_plant
            else -> R.string.widget_stage_tree
        }
    }
}
