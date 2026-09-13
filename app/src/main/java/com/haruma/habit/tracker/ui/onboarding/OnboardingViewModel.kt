package com.haruma.habit.tracker.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haruma.habit.tracker.data.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val prefs: UserPreferencesRepository,
) : ViewModel() {

    fun finish() {
        viewModelScope.launch { prefs.markOnboardingComplete() }
    }
}
