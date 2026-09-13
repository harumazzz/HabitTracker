package com.haruma.habit.tracker.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haruma.habit.tracker.data.UserPreferencesRepository
import com.haruma.habit.tracker.navigation.OnboardingRoute
import com.haruma.habit.tracker.navigation.TodayRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    prefs: UserPreferencesRepository,
) : ViewModel() {

    val startDestination = prefs.hasSeenOnboarding
        .map { seen -> if (seen) TodayRoute else OnboardingRoute }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
