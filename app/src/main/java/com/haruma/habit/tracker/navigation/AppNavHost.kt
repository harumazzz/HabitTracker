package com.haruma.habit.tracker.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.ui.res.stringResource
import com.haruma.habit.tracker.R
import com.haruma.habit.tracker.ui.form.HabitFormSheet
import com.haruma.habit.tracker.ui.onboarding.OnboardingScreen
import com.haruma.habit.tracker.ui.settings.SettingsScreen
import com.haruma.habit.tracker.ui.stats.StatsScreen
import com.haruma.habit.tracker.ui.today.TodayScreen
import kotlinx.serialization.Serializable

@Serializable object OnboardingRoute
@Serializable object TodayRoute
@Serializable object StatsRoute
@Serializable object SettingsRoute
@Serializable object AddHabitRoute
@Serializable data class EditHabitRoute(val habitId: Int)

@Composable
fun AppNavHost(startDestination: Any) {
    val navController = rememberNavController()
    val currentEntry by navController.currentBackStackEntryAsState()
    val destination = currentEntry?.destination

    val showNavBar = destination?.let {
        it.hasRoute<TodayRoute>() || it.hasRoute<StatsRoute>() || it.hasRoute<SettingsRoute>()
    } == true

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            if (showNavBar) {
                item(
                    icon = { Icon(Icons.Outlined.CheckCircle, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_today)) },
                    selected = destination?.hasRoute<TodayRoute>() == true,
                    onClick = {
                        navController.navigate(TodayRoute) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
                item(
                    icon = { Icon(Icons.Outlined.BarChart, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_stats)) },
                    selected = destination?.hasRoute<StatsRoute>() == true,
                    onClick = {
                        navController.navigate(StatsRoute) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
                item(
                    icon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_settings)) },
                    selected = destination?.hasRoute<SettingsRoute>() == true,
                    onClick = {
                        navController.navigate(SettingsRoute) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
        navigationSuiteColors = if (!showNavBar) NavigationSuiteDefaults.colors(
            navigationBarContainerColor = Color.Transparent,
            navigationBarContentColor = Color.Transparent,
        ) else NavigationSuiteDefaults.colors(),
    ) {
        NavHost(navController = navController, startDestination = startDestination) {
            composable<OnboardingRoute> {
                OnboardingScreen(
                    onFinish = {
                        navController.navigate(TodayRoute) {
                            popUpTo(OnboardingRoute) { inclusive = true }
                        }
                    },
                )
            }
            composable<TodayRoute> {
                TodayScreen(
                    onAddHabit = { navController.navigate(AddHabitRoute) },
                    onEditHabit = { navController.navigate(EditHabitRoute(it)) },
                )
            }
            composable<StatsRoute> { StatsScreen() }
            composable<SettingsRoute> { SettingsScreen() }
            composable<AddHabitRoute> {
                HabitFormSheet(onDismiss = { navController.popBackStack() })
            }
            composable<EditHabitRoute> {
                HabitFormSheet(onDismiss = { navController.popBackStack() })
            }
        }
    }
}
