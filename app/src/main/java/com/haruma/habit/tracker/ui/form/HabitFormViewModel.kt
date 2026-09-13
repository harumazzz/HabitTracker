package com.haruma.habit.tracker.ui.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haruma.habit.tracker.data.HabitEntity
import com.haruma.habit.tracker.data.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HabitFormViewModel @Inject constructor(
    private val repo: HabitRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HabitFormUiState())
    val uiState: StateFlow<HabitFormUiState> = _uiState.asStateFlow()

    private var existingCreatedAt: Long? = null

    init {
        val habitId = savedStateHandle.get<Int>("habitId")
        if (habitId != null && habitId > 0) {
            viewModelScope.launch {
                val habit = repo.getById(habitId)
                if (habit != null) {
                    existingCreatedAt = habit.createdAt
                    _uiState.update {
                        it.copy(
                            habitId = habit.id,
                            isEditMode = true,
                            name = habit.name,
                            icon = HabitIcon.fromKey(habit.emoji),
                            colorArgb = habit.colorArgb,
                            frequencyType = FrequencyType.fromString(habit.frequencyType),
                            targetDays = DayOfWeekConstants.parseDays(habit.targetDays),
                            targetCount = habit.targetCount,
                            reminderTimeMinutes = habit.reminderTimeMinutes,
                        )
                    }
                }
            }
        }
    }

    fun onNameChanged(name: String) {
        _uiState.update {
            it.copy(
                name = name,
                nameError = if (name.isNotBlank()) false else it.nameError,
            )
        }
    }

    fun onIconSelected(icon: HabitIcon) {
        _uiState.update { it.copy(icon = icon) }
    }

    fun onColorSelected(colorArgb: Int) {
        _uiState.update { it.copy(colorArgb = colorArgb) }
    }

    fun onFrequencySelected(frequencyType: FrequencyType) {
        _uiState.update { it.copy(frequencyType = frequencyType) }
    }

    fun onDayToggled(day: HabitDay) {
        _uiState.update { current ->
            val updatedDays = if (current.targetDays.contains(day)) {
                current.targetDays - day
            } else {
                current.targetDays + day
            }
            current.copy(targetDays = updatedDays)
        }
    }

    fun onDaysSelected(days: Set<HabitDay>) {
        _uiState.update { it.copy(targetDays = days) }
    }

    fun onTargetCountChanged(count: Int) {
        _uiState.update { it.copy(targetCount = count.coerceIn(1, 99)) }
    }

    fun onReminderTimeSelected(minutes: Int?) {
        _uiState.update { it.copy(reminderTimeMinutes = minutes) }
    }

    fun saveHabit(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = true) }
            return
        }

        viewModelScope.launch {
            val entity = HabitEntity(
                id = state.habitId ?: 0,
                name = state.name.trim(),
                emoji = state.icon.iconName,
                colorArgb = state.colorArgb,
                frequencyType = state.frequencyType.name,
                targetDays = DayOfWeekConstants.formatDays(state.targetDays),
                targetCount = state.targetCount,
                createdAt = existingCreatedAt ?: System.currentTimeMillis(),
                isArchived = false,
                reminderTimeMinutes = state.reminderTimeMinutes,
            )
            repo.save(entity)
            onSuccess()
        }
    }

    fun deleteHabit(onSuccess: () -> Unit) {
        val id = _uiState.value.habitId ?: return
        viewModelScope.launch {
            val entity = repo.getById(id)
            if (entity != null) {
                repo.delete(entity)
            }
            onSuccess()
        }
    }
}
