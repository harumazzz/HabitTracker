package com.haruma.habit.tracker.ui.today

import androidx.lifecycle.ViewModel
import com.haruma.habit.tracker.data.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val repo: HabitRepository,
) : ViewModel()
