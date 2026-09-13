package com.haruma.habit.tracker.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haruma.habit.tracker.data.HabitRepository
import com.haruma.habit.tracker.data.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: UserPreferencesRepository,
    private val habitRepository: HabitRepository,
) : ViewModel() {

    val themeMode = prefs.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "system")

    val notificationsEnabled = prefs.notificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val defaultReminderMinutes = prefs.defaultReminderMinutes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 480)

    val language = prefs.language
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "en")

    fun setThemeMode(mode: String) {
        viewModelScope.launch { prefs.setThemeMode(mode) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { prefs.setNotificationsEnabled(enabled) }
    }

    fun setDefaultReminderMinutes(minutes: Int) {
        viewModelScope.launch { prefs.setDefaultReminderMinutes(minutes) }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch { prefs.setLanguage(language) }
    }

    fun clearAllData() {
        viewModelScope.launch { habitRepository.clearAllData() }
    }
}

fun String.isDark(isSystemDark: Boolean = false): Boolean = when (this) {
    "dark"  -> true
    "light" -> false
    else    -> isSystemDark
}
