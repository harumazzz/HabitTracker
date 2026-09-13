package com.haruma.habit.tracker.navigation

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import androidx.core.util.Consumer
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
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

    val context = LocalContext.current
    DisposableEffect(navController, startDestination) {
        val activity = context as? ComponentActivity
        val listener = Consumer<Intent> { intent ->
            if (startDestination !is OnboardingRoute) {
                navController.handleDeepLink(intent)
            }
        }
        activity?.addOnNewIntentListener(listener)
        onDispose {
            activity?.removeOnNewIntentListener(listener)
        }
    }

    val showNavBar = destination?.let {
        it.hasRoute<TodayRoute>() || it.hasRoute<StatsRoute>() || it.hasRoute<SettingsRoute>()
    } == true

    val iosEasing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)

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
        NavHost(
            navController = navController,
            startDestination = startDestination,
            enterTransition = {
                val isInitialTab = initialState.destination.let {
                    it.hasRoute<TodayRoute>() || it.hasRoute<StatsRoute>() || it.hasRoute<SettingsRoute>()
                }
                val isTargetTab = targetState.destination.let {
                    it.hasRoute<TodayRoute>() || it.hasRoute<StatsRoute>() || it.hasRoute<SettingsRoute>()
                }
                if (isInitialTab && isTargetTab) {
                    fadeIn(animationSpec = tween(220)) + scaleIn(initialScale = 0.98f, animationSpec = tween(220))
                } else {
                    slideInHorizontally(
                        initialOffsetX = { fullWidth -> fullWidth },
                        animationSpec = tween(durationMillis = 380, easing = iosEasing),
                    ) + fadeIn(animationSpec = tween(durationMillis = 380))
                }
            },
            exitTransition = {
                val isInitialTab = initialState.destination.let {
                    it.hasRoute<TodayRoute>() || it.hasRoute<StatsRoute>() || it.hasRoute<SettingsRoute>()
                }
                val isTargetTab = targetState.destination.let {
                    it.hasRoute<TodayRoute>() || it.hasRoute<StatsRoute>() || it.hasRoute<SettingsRoute>()
                }
                if (isInitialTab && isTargetTab) {
                    fadeOut(animationSpec = tween(180)) + scaleOut(targetScale = 1.02f, animationSpec = tween(180))
                } else {
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> -fullWidth / 4 },
                        animationSpec = tween(durationMillis = 380, easing = iosEasing),
                    ) + fadeOut(
                        animationSpec = tween(durationMillis = 380, easing = iosEasing),
                        targetAlpha = 0.75f,
                    )
                }
            },
            popEnterTransition = {
                val isInitialTab = initialState.destination.let {
                    it.hasRoute<TodayRoute>() || it.hasRoute<StatsRoute>() || it.hasRoute<SettingsRoute>()
                }
                val isTargetTab = targetState.destination.let {
                    it.hasRoute<TodayRoute>() || it.hasRoute<StatsRoute>() || it.hasRoute<SettingsRoute>()
                }
                if (isInitialTab && isTargetTab) {
                    fadeIn(animationSpec = tween(220)) + scaleIn(initialScale = 0.98f, animationSpec = tween(220))
                } else {
                    slideInHorizontally(
                        initialOffsetX = { fullWidth -> -fullWidth / 4 },
                        animationSpec = tween(durationMillis = 350, easing = iosEasing),
                    ) + fadeIn(animationSpec = tween(durationMillis = 350))
                }
            },
            popExitTransition = {
                val isInitialTab = initialState.destination.let {
                    it.hasRoute<TodayRoute>() || it.hasRoute<StatsRoute>() || it.hasRoute<SettingsRoute>()
                }
                val isTargetTab = targetState.destination.let {
                    it.hasRoute<TodayRoute>() || it.hasRoute<StatsRoute>() || it.hasRoute<SettingsRoute>()
                }
                if (isInitialTab && isTargetTab) {
                    fadeOut(animationSpec = tween(180)) + scaleOut(targetScale = 1.02f, animationSpec = tween(180))
                } else {
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> fullWidth },
                        animationSpec = tween(durationMillis = 350, easing = iosEasing),
                    )
                }
            },
        ) {
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
            dialog<AddHabitRoute>(
                deepLinks = if (startDestination !is OnboardingRoute) {
                    listOf(
                        navDeepLink {
                            uriPattern = "habittracker://add_habit"
                        },
                    )
                } else {
                    emptyList()
                },
                dialogProperties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    decorFitsSystemWindows = false,
                    dismissOnClickOutside = false,
                ),
            ) {
                HabitFormSheet(onDismiss = { navController.popBackStack() })
            }
            dialog<EditHabitRoute>(
                dialogProperties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    decorFitsSystemWindows = false,
                    dismissOnClickOutside = false,
                ),
            ) {
                HabitFormSheet(onDismiss = { navController.popBackStack() })
            }
        }
    }
}
