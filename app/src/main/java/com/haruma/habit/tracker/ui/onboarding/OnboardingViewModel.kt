package com.haruma.habit.tracker.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haruma.habit.tracker.data.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val selectedLanguage: String = "en",
    val reminderMinutes: Int = 480,
    val notificationsEnabled: Boolean = true,
    val notificationPermissionGranted: Boolean = false,
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val prefs: UserPreferencesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val initialLang = prefs.language.first()
            val initialTime = prefs.defaultReminderMinutes.first()
            val initialNotif = prefs.notificationsEnabled.first()
            _uiState.update {
                it.copy(
                    selectedLanguage = initialLang,
                    reminderMinutes = initialTime,
                    notificationsEnabled = initialNotif,
                )
            }
        }
    }

    fun setLanguage(language: String) {
        _uiState.update { it.copy(selectedLanguage = language) }
        viewModelScope.launch {
            prefs.setLanguage(language)
        }
    }

    fun setReminderMinutes(minutes: Int) {
        _uiState.update { it.copy(reminderMinutes = minutes) }
        viewModelScope.launch {
            prefs.setDefaultReminderMinutes(minutes)
        }
    }

    fun setNotificationPermissionGranted(granted: Boolean) {
        _uiState.update {
            it.copy(
                notificationPermissionGranted = granted,
                notificationsEnabled = granted,
            )
        }
        viewModelScope.launch {
            prefs.setNotificationsEnabled(granted)
        }
    }

    fun finish() {
        val state = _uiState.value
        viewModelScope.launch {
            prefs.setLanguage(state.selectedLanguage)
            prefs.setDefaultReminderMinutes(state.reminderMinutes)
            prefs.setNotificationsEnabled(state.notificationsEnabled)
            prefs.markOnboardingComplete()
        }
    }
}
