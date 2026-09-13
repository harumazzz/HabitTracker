package com.haruma.habit.tracker.ui.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.haruma.habit.tracker.data.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HabitFormViewModel @Inject constructor(
    private val repo: HabitRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel()
