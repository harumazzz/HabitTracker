package com.haruma.habit.tracker.ui.form

data class HabitFormUiState(
    val habitId: Int? = null,
    val isEditMode: Boolean = false,
    val name: String = "",
    val icon: HabitIcon = HabitIcon.CHECK,
    val colorArgb: Int = ColorConstants.DEFAULT_COLOR,
    val frequencyType: FrequencyType = FrequencyType.DAILY,
    val targetDays: Set<HabitDay> = emptySet(),
    val targetCount: Int = 1,
    val reminderTimeMinutes: Int? = null,
    val nameError: Boolean = false,
)
