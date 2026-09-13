package com.haruma.habit.tracker.ui.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.haruma.habit.tracker.ui.components.AppTimePickerDialog
import com.haruma.habit.tracker.ui.onboarding.components.LanguageSelectionPage
import com.haruma.habit.tracker.ui.onboarding.components.NotificationPermissionPage
import com.haruma.habit.tracker.ui.onboarding.components.OnboardingBottomBar
import com.haruma.habit.tracker.ui.onboarding.components.ReminderSetupPage
import com.haruma.habit.tracker.ui.onboarding.components.StreakInspirationPage
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var showTimePicker by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        viewModel.setNotificationPermissionGranted(isGranted)
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val isGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
            if (isGranted) {
                viewModel.setNotificationPermissionGranted(true)
            }
        } else {
            viewModel.setNotificationPermissionGranted(true)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) { page ->
                when (page) {
                    0 -> LanguageSelectionPage(
                        selectedLanguage = uiState.selectedLanguage,
                        onLanguageSelected = { viewModel.setLanguage(it) },
                    )
                    1 -> StreakInspirationPage()
                    2 -> ReminderSetupPage(
                        currentMinutes = uiState.reminderMinutes,
                        onTimeSelected = { viewModel.setReminderMinutes(it) },
                        onOpenCustomTime = { showTimePicker = true },
                    )
                    3 -> NotificationPermissionPage(
                        isGranted = uiState.notificationPermissionGranted,
                        onRequestPermission = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                viewModel.setNotificationPermissionGranted(true)
                            }
                        },
                    )
                }
            }

            OnboardingBottomBar(
                currentPage = pagerState.currentPage,
                pageCount = 4,
                onSkip = {
                    viewModel.finish()
                    onFinish()
                },
                onNext = {
                    if (pagerState.currentPage < 3) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        viewModel.finish()
                        onFinish()
                    }
                },
            )
        }
    }

    if (showTimePicker) {
        val safeMinutes = uiState.reminderMinutes?.takeIf { it in 0 until 1440 } ?: 480
        AppTimePickerDialog(
            initialHour = (safeMinutes / 60).coerceIn(0, 23),
            initialMinute = (safeMinutes % 60).coerceIn(0, 59),
            onTimeSelected = { hour, minute ->
                viewModel.setReminderMinutes(hour * 60 + minute)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false },
        )
    }
}
