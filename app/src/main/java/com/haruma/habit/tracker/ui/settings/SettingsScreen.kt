package com.haruma.habit.tracker.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.haruma.habit.tracker.R
import com.haruma.habit.tracker.ui.components.AppSwitch
import com.haruma.habit.tracker.ui.components.AppTimePickerDialog
import com.haruma.habit.tracker.ui.components.ConfirmDeleteDialog
import com.haruma.habit.tracker.ui.settings.components.SettingDivider
import com.haruma.habit.tracker.ui.settings.components.SettingGroupCard
import com.haruma.habit.tracker.ui.settings.components.SettingItemRow
import com.haruma.habit.tracker.ui.settings.components.SettingSectionHeader
import com.haruma.habit.tracker.ui.settings.components.ThemeSelectionDialog
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
    val defaultReminderMinutes by viewModel.defaultReminderMinutes.collectAsStateWithLifecycle()

    var showThemeDialog by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    val versionName = remember(context) {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0"
        } catch (_: Exception) {
            "1.0"
        }
    }

    val themeLabel = when (themeMode) {
        "light" -> stringResource(R.string.settings_theme_light)
        "dark" -> stringResource(R.string.settings_theme_dark)
        else -> stringResource(R.string.settings_theme_system)
    }

    val reminderTimeText = remember(defaultReminderMinutes) {
        val hours = defaultReminderMinutes / 60
        val minutes = defaultReminderMinutes % 60
        String.format(Locale.getDefault(), "%02d:%02d", hours, minutes)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        fontWeight = FontWeight.Bold,
                    )
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            SettingSectionHeader(title = stringResource(R.string.settings_section_appearance))
            SettingGroupCard {
                SettingItemRow(
                    title = stringResource(R.string.settings_theme_label),
                    subtitle = themeLabel,
                    icon = Icons.Outlined.Palette,
                    iconTint = MaterialTheme.colorScheme.primary,
                    showChevron = true,
                    onClick = { showThemeDialog = true },
                )
            }

            SettingSectionHeader(title = stringResource(R.string.settings_section_notifications))
            SettingGroupCard {
                SettingItemRow(
                    title = stringResource(R.string.settings_notif_enabled),
                    icon = Icons.Outlined.Notifications,
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    trailing = {
                        AppSwitch(
                            checked = notificationsEnabled,
                            onCheckedChange = { viewModel.setNotificationsEnabled(it) },
                        )
                    },
                )
                SettingDivider()
                SettingItemRow(
                    title = stringResource(R.string.settings_default_reminder),
                    subtitle = reminderTimeText,
                    icon = Icons.Outlined.Schedule,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    enabled = notificationsEnabled,
                    showChevron = true,
                    onClick = { showTimePicker = true },
                )
            }

            SettingSectionHeader(title = stringResource(R.string.settings_section_data))
            SettingGroupCard {
                SettingItemRow(
                    title = stringResource(R.string.settings_clear_data),
                    icon = Icons.Outlined.Delete,
                    iconTint = MaterialTheme.colorScheme.error,
                    titleColor = MaterialTheme.colorScheme.error,
                    showChevron = true,
                    onClick = { showClearDataDialog = true },
                )
            }

            SettingSectionHeader(title = stringResource(R.string.settings_section_about))
            SettingGroupCard {
                SettingItemRow(
                    title = stringResource(R.string.settings_version, versionName),
                    icon = Icons.Outlined.Info,
                    iconTint = MaterialTheme.colorScheme.primary,
                )
                SettingDivider()
                SettingItemRow(
                    title = stringResource(R.string.settings_terms_of_use),
                    icon = Icons.Outlined.Description,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    trailing = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp),
                        )
                    },
                    onClick = {
                        runCatching { uriHandler.openUri(WebConstants.TERMS_OF_USE_URL) }
                    },
                )
                SettingDivider()
                SettingItemRow(
                    title = stringResource(R.string.settings_privacy_policy),
                    icon = Icons.Outlined.PrivacyTip,
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    trailing = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp),
                        )
                    },
                    onClick = {
                        runCatching { uriHandler.openUri(WebConstants.PRIVACY_POLICY_URL) }
                    },
                )
                SettingDivider()
                SettingItemRow(
                    title = stringResource(R.string.settings_about_us),
                    icon = Icons.Outlined.People,
                    iconTint = MaterialTheme.colorScheme.primary,
                    trailing = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp),
                        )
                    },
                    onClick = {
                        runCatching { uriHandler.openUri(WebConstants.ABOUT_US_URL) }
                    },
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = themeMode,
            onThemeSelected = { viewModel.setThemeMode(it) },
            onDismiss = { showThemeDialog = false },
        )
    }

    if (showTimePicker) {
        AppTimePickerDialog(
            initialHour = defaultReminderMinutes / 60,
            initialMinute = defaultReminderMinutes % 60,
            onTimeSelected = { hour, minute ->
                viewModel.setDefaultReminderMinutes(hour * 60 + minute)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false },
        )
    }

    if (showClearDataDialog) {
        ConfirmDeleteDialog(
            title = stringResource(R.string.settings_clear_data_confirm_title),
            message = stringResource(R.string.settings_clear_data_confirm_body),
            onConfirm = {
                viewModel.clearAllData()
                showClearDataDialog = false
            },
            onDismiss = { showClearDataDialog = false },
        )
    }
}
