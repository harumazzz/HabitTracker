package com.haruma.habit.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.haruma.habit.tracker.navigation.AppNavHost
import com.haruma.habit.tracker.ui.splash.SplashViewModel
import com.haruma.habit.tracker.ui.settings.SettingsViewModel
import com.haruma.habit.tracker.ui.theme.HabitTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val splashViewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition {
            splashViewModel.startDestination.value == null
        }

        enableEdgeToEdge()

        setContent {
            val startDestination by splashViewModel.startDestination.collectAsStateWithLifecycle()

            if (startDestination != null) {
                HabitTrackerTheme {
                    AppNavHost(startDestination = startDestination!!)
                }
            }
        }
    }
}