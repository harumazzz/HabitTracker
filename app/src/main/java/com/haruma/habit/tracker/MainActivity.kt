package com.haruma.habit.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.haruma.habit.tracker.navigation.AppNavHost
import com.haruma.habit.tracker.ui.splash.SplashViewModel
import com.haruma.habit.tracker.ui.settings.SettingsViewModel
import com.haruma.habit.tracker.ui.settings.isDark
import com.haruma.habit.tracker.ui.theme.HabitTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val splashViewModel: SplashViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition {
            splashViewModel.startDestination.value == null
        }

        enableEdgeToEdge()

        setContent {
            val startDestination by splashViewModel.startDestination.collectAsStateWithLifecycle()
            val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()
            val isSystemDark = isSystemInDarkTheme()

            if (startDestination != null) {
                HabitTrackerTheme(darkTheme = themeMode.isDark(isSystemDark)) {
                    AppNavHost(startDestination = startDestination!!)
                }
            }
        }
    }
}