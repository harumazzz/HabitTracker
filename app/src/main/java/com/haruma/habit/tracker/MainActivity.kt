package com.haruma.habit.tracker

import android.app.LocaleManager
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.haruma.habit.tracker.navigation.AppNavHost
import com.haruma.habit.tracker.ui.splash.SplashViewModel
import com.haruma.habit.tracker.ui.settings.SettingsViewModel
import com.haruma.habit.tracker.ui.settings.isDark
import com.haruma.habit.tracker.ui.theme.HabitTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val splashViewModel: SplashViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition {
            splashViewModel.startDestination.value == null
        }

        setContent {
            val startDestination by splashViewModel.startDestination.collectAsStateWithLifecycle()
            val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()
            val language by settingsViewModel.language.collectAsStateWithLifecycle()
            val isSystemDark = isSystemInDarkTheme()
            val darkTheme = themeMode.isDark(isSystemDark)

            LaunchedEffect(language) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && language.isNotBlank()) {
                    val localeManager = getSystemService(LocaleManager::class.java)
                    val targetLocales = LocaleList.forLanguageTags(language)
                    if (localeManager?.applicationLocales != targetLocales) {
                        localeManager?.applicationLocales = targetLocales
                    }
                }
            }

            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = if (darkTheme) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                    },
                    navigationBarStyle = if (darkTheme) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                    }
                )
                onDispose {}
            }

            val context = this@MainActivity
            val locale = when (language) {
                "vi" -> Locale.forLanguageTag("vi")
                "en" -> Locale.forLanguageTag("en")
                else -> Locale.getDefault()
            }
            val currentConfig = LocalConfiguration.current
            val config = remember(locale, currentConfig) {
                Configuration(currentConfig).apply {
                    setLocale(locale)
                    setLayoutDirection(locale)
                }
            }
            val localizedContext = remember(context, config) {
                LocalizedContext(context, config)
            }

            CompositionLocalProvider(
                LocalConfiguration provides config,
                androidx.compose.ui.platform.LocalContext provides localizedContext,
            ) {
                HabitTrackerTheme(darkTheme = darkTheme) {
                    if (startDestination != null) {
                        AppNavHost(startDestination = startDestination!!)
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background),
                            contentAlignment = Alignment.Center,
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_splash_logo),
                                contentDescription = null,
                                modifier = Modifier.size(160.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

private class LocalizedContext(
    base: android.content.Context,
    configuration: Configuration,
) : android.content.ContextWrapper(base) {
    private val localizedResources: android.content.res.Resources = base.createConfigurationContext(configuration).resources

    override fun getResources(): android.content.res.Resources = localizedResources
}