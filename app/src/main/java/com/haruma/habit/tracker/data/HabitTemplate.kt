package com.haruma.habit.tracker.data

import androidx.annotation.StringRes
import com.haruma.habit.tracker.R

enum class HabitCategory(
    @param:StringRes val titleResId: Int,
) {
    ALL(R.string.templates_tab_all),
    HEALTH(R.string.templates_tab_health),
    PRODUCTIVITY(R.string.templates_tab_productivity),
    MINDFULNESS(R.string.templates_tab_mindfulness),
}

data class HabitTemplate(
    val id: String,
    @param:StringRes val nameResId: Int,
    @param:StringRes val descResId: Int,
    val category: HabitCategory,
    val emoji: String,
    val colorArgb: Int,
    val targetCount: Int = 1,
    val frequencyType: String = "DAILY",
) {
    companion object {
        val ALL_TEMPLATES = listOf(
            HabitTemplate(
                id = "water",
                nameResId = R.string.template_water_name,
                descResId = R.string.template_water_desc,
                category = HabitCategory.HEALTH,
                emoji = "water",
                colorArgb = 0xFF2196F3.toInt(),
                targetCount = 8,
            ),
            HabitTemplate(
                id = "steps",
                nameResId = R.string.template_steps_name,
                descResId = R.string.template_steps_desc,
                category = HabitCategory.HEALTH,
                emoji = "run",
                colorArgb = 0xFF4CAF50.toInt(),
                targetCount = 1,
            ),
            HabitTemplate(
                id = "stretch",
                nameResId = R.string.template_stretch_name,
                descResId = R.string.template_stretch_desc,
                category = HabitCategory.HEALTH,
                emoji = "fitness",
                colorArgb = 0xFFFF9800.toInt(),
                targetCount = 1,
            ),
            HabitTemplate(
                id = "sleep",
                nameResId = R.string.template_sleep_name,
                descResId = R.string.template_sleep_desc,
                category = HabitCategory.HEALTH,
                emoji = "bedtime",
                colorArgb = 0xFF9C27B0.toInt(),
                targetCount = 1,
            ),
            HabitTemplate(
                id = "read",
                nameResId = R.string.template_reading_name,
                descResId = R.string.template_reading_desc,
                category = HabitCategory.PRODUCTIVITY,
                emoji = "book",
                colorArgb = 0xFF009688.toInt(),
                targetCount = 1,
            ),
            HabitTemplate(
                id = "vocab",
                nameResId = R.string.template_vocab_name,
                descResId = R.string.template_vocab_desc,
                category = HabitCategory.PRODUCTIVITY,
                emoji = "lightbulb",
                colorArgb = 0xFF3F51B5.toInt(),
                targetCount = 5,
            ),
            HabitTemplate(
                id = "plan",
                nameResId = R.string.template_plan_name,
                descResId = R.string.template_plan_desc,
                category = HabitCategory.PRODUCTIVITY,
                emoji = "edit",
                colorArgb = 0xFF673AB7.toInt(),
                targetCount = 1,
            ),
            HabitTemplate(
                id = "clean",
                nameResId = R.string.template_clean_name,
                descResId = R.string.template_clean_desc,
                category = HabitCategory.PRODUCTIVITY,
                emoji = "star",
                colorArgb = 0xFFFF5722.toInt(),
                targetCount = 1,
            ),
            HabitTemplate(
                id = "meditate",
                nameResId = R.string.template_meditation_name,
                descResId = R.string.template_meditation_desc,
                category = HabitCategory.MINDFULNESS,
                emoji = "meditation",
                colorArgb = 0xFF00BCD4.toInt(),
                targetCount = 1,
            ),
            HabitTemplate(
                id = "gratitude",
                nameResId = R.string.template_gratitude_name,
                descResId = R.string.template_gratitude_desc,
                category = HabitCategory.MINDFULNESS,
                emoji = "favorite",
                colorArgb = 0xFFE91E63.toInt(),
                targetCount = 1,
            ),
            HabitTemplate(
                id = "screenfree",
                nameResId = R.string.template_screenfree_name,
                descResId = R.string.template_screenfree_desc,
                category = HabitCategory.MINDFULNESS,
                emoji = "bedtime",
                colorArgb = 0xFF607D8B.toInt(),
                targetCount = 1,
            ),
        )
    }
}
