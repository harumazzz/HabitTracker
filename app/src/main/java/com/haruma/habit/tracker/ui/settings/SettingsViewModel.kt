package com.haruma.habit.tracker.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haruma.habit.tracker.data.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: UserPreferencesRepository,
) : ViewModel() {

    val themeMode = prefs.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "system")

    fun setThemeMode(mode: String) {
        viewModelScope.launch { prefs.setThemeMode(mode) }
    }
}

fun String.isDark(isSystemDark: Boolean = false): Boolean = when (this) {
    "dark"  -> true
    "light" -> false
    else    -> isSystemDark
}
