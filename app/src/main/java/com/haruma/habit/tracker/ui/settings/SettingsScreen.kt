package com.haruma.habit.tracker.ui.settings

import android.app.Activity
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.haruma.habit.tracker.R
import com.haruma.habit.tracker.ui.components.AppSwitch
import com.haruma.habit.tracker.ui.components.AppTimePickerDialog
import com.haruma.habit.tracker.ui.components.ConfirmDeleteDialog
import com.haruma.habit.tracker.ui.components.CupertinoLoadingIndicator
import com.haruma.habit.tracker.ui.components.CustomToastHost
import com.haruma.habit.tracker.ui.components.rememberCustomToastState
import com.haruma.habit.tracker.ui.settings.components.ConfirmRestoreDialog
import com.haruma.habit.tracker.ui.settings.components.LanguageSelectionDialog
import com.haruma.habit.tracker.ui.settings.components.SettingDivider
import com.haruma.habit.tracker.ui.settings.components.SettingGroupCard
import com.haruma.habit.tracker.ui.settings.components.SettingItemRow
import com.haruma.habit.tracker.ui.settings.components.SettingSectionHeader
import com.haruma.habit.tracker.ui.settings.components.ThemeSelectionDialog
import com.haruma.habit.tracker.ui.settings.components.RateUsDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateToFeedback: (rating: Int) -> Unit = {},
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
    val defaultReminderMinutes by viewModel.defaultReminderMinutes.collectAsStateWithLifecycle()
    val lastBackupTime by viewModel.lastBackupTime.collectAsStateWithLifecycle()
    val backupAccountEmail by viewModel.backupAccountEmail.collectAsStateWithLifecycle()
    val isBackingUp by viewModel.isBackingUp.collectAsStateWithLifecycle()
    val isRestoring by viewModel.isRestoring.collectAsStateWithLifecycle()
    val pendingRestorePayload by viewModel.pendingRestorePayload.collectAsStateWithLifecycle()

    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showRateUsDialog by remember { mutableStateOf(false) }

    val toastState = rememberCustomToastState()
    var isSignInForBackup by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        viewModel.setNotificationsEnabled(isGranted)
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        viewModel.handleSignInResult(result.data, isForBackup = isSignInForBackup)
    }

    val driveAuthorizationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        viewModel.handleDriveAuthorizationResult(result.resultCode == Activity.RESULT_OK)
    }

    val backupSuccessMsg = stringResource(R.string.settings_backup_success)
    val restoreSuccessMsg = stringResource(R.string.settings_restore_success)
    val restoreNotFoundMsg = stringResource(R.string.settings_restore_not_found)
    val genericErrorMsg = stringResource(R.string.error_generic)

    LaunchedEffect(Unit) {
        viewModel.backupUiEvent.collect { event ->
            when (event) {
                is BackupUiEvent.BackupSuccess -> toastState.show(backupSuccessMsg)
                is BackupUiEvent.RestoreSuccess -> toastState.show(restoreSuccessMsg)
                is BackupUiEvent.RestoreNotFound -> toastState.show(restoreNotFoundMsg)
                is BackupUiEvent.BackupError -> toastState.show(
                    context.getString(
                        R.string.settings_backup_failed,
                        event.cause?.localizedMessage ?: genericErrorMsg,
                    ),
                )
                is BackupUiEvent.RestoreError -> toastState.show(
                    context.getString(
                        R.string.settings_restore_failed,
                        event.cause?.localizedMessage ?: genericErrorMsg,
                    ),
                )
                is BackupUiEvent.RequestSignIn -> {
                    isSignInForBackup = event.isForBackup
                    googleSignInLauncher.launch(event.intent)
                }
                is BackupUiEvent.RequestDriveAuthorization -> {
                    driveAuthorizationLauncher.launch(event.intent)
                }
            }
        }
    }

    val lastBackupText = remember(lastBackupTime, backupAccountEmail) {
        if (lastBackupTime > 0) {
            val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val dateStr = format.format(Date(lastBackupTime))
            if (backupAccountEmail.isNotBlank()) "$dateStr ($backupAccountEmail)" else dateStr
        } else {
            null
        }
    }

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

    val languageLabel = when (language) {
        "vi" -> stringResource(R.string.onboarding_lang_vi)
        else -> stringResource(R.string.onboarding_lang_en)
    }

    val reminderTimeText = remember(defaultReminderMinutes) {
        if (defaultReminderMinutes >= 0) {
            val hours = defaultReminderMinutes / 60
            val minutes = defaultReminderMinutes % 60
            String.format(Locale.getDefault(), "%02d:%02d", hours, minutes)
        } else {
            null
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        fontWeight = FontWeight.Bold,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
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
                    SettingDivider()
                    SettingItemRow(
                        title = stringResource(R.string.settings_language_label),
                        subtitle = languageLabel,
                        icon = Icons.Outlined.Translate,
                        iconTint = MaterialTheme.colorScheme.secondary,
                        showChevron = true,
                        onClick = { showLanguageDialog = true },
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
                                onCheckedChange = { checked ->
                                    if (checked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        if (ContextCompat.checkSelfPermission(
                                                context,
                                                Manifest.permission.POST_NOTIFICATIONS,
                                            ) != PackageManager.PERMISSION_GRANTED
                                        ) {
                                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                            return@AppSwitch
                                        }
                                    }
                                    viewModel.setNotificationsEnabled(checked)
                                },
                            )
                        },
                    )
                    SettingDivider()
                    SettingItemRow(
                        title = stringResource(R.string.settings_default_reminder),
                        subtitle = reminderTimeText ?: stringResource(R.string.form_reminder_none),
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
                        title = stringResource(R.string.settings_backup_drive),
                        subtitle = lastBackupText?.let {
                            stringResource(R.string.settings_backup_drive_subtitle, it)
                        } ?: stringResource(R.string.settings_backup_never),
                        icon = Icons.Outlined.CloudUpload,
                        iconTint = MaterialTheme.colorScheme.primary,
                        showChevron = true,
                        enabled = !isBackingUp && !isRestoring,
                        trailing = if (isBackingUp) {
                            { CupertinoLoadingIndicator(modifier = Modifier.size(20.dp)) }
                        } else null,
                        onClick = { viewModel.startBackup() },
                    )
                    SettingDivider()
                    SettingItemRow(
                        title = stringResource(R.string.settings_restore_drive),
                        subtitle = stringResource(R.string.settings_restore_drive_subtitle),
                        icon = Icons.Outlined.CloudDownload,
                        iconTint = MaterialTheme.colorScheme.secondary,
                        showChevron = true,
                        enabled = !isBackingUp && !isRestoring,
                        trailing = if (isRestoring) {
                            { CupertinoLoadingIndicator(modifier = Modifier.size(20.dp)) }
                        } else null,
                        onClick = { viewModel.startRestore() },
                    )
                    SettingDivider()
                    SettingItemRow(
                        title = stringResource(R.string.settings_clear_data),
                        icon = Icons.Outlined.Delete,
                        iconTint = MaterialTheme.colorScheme.error,
                        titleColor = MaterialTheme.colorScheme.error,
                        showChevron = true,
                        enabled = !isBackingUp && !isRestoring,
                        onClick = { showClearDataDialog = true },
                    )
                }

            SettingSectionHeader(title = stringResource(R.string.settings_section_about))
            SettingGroupCard {
                SettingItemRow(
                    title = stringResource(R.string.settings_rate_us),
                    icon = Icons.Outlined.Star,
                    iconTint = Color(0xFFFFB400),
                    showChevron = true,
                    onClick = { showRateUsDialog = true },
                )
                SettingDivider()
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

        CustomToastHost(
            state = toastState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = padding.calculateTopPadding() + 8.dp),
        )
    }
}

    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = themeMode,
            onThemeSelected = { viewModel.setThemeMode(it) },
            onDismiss = { showThemeDialog = false },
        )
    }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = language,
            onLanguageSelected = { viewModel.setLanguage(it) },
            onDismiss = { showLanguageDialog = false },
        )
    }

    if (showTimePicker) {
        val safeMinutes = if (defaultReminderMinutes >= 0) defaultReminderMinutes else 480
        AppTimePickerDialog(
            initialHour = safeMinutes / 60,
            initialMinute = safeMinutes % 60,
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

    if (showRateUsDialog) {
        RateUsDialog(
            onDismiss = { showRateUsDialog = false },
            onRatingSubmitted = { rating ->
                showRateUsDialog = false
                if (rating >= 4) {
                    runCatching {
                        uriHandler.openUri(
                            "market://details?id=${context.packageName}"
                        )
                    }
                } else {
                    onNavigateToFeedback(rating)
                }
            },
        )
    }

    pendingRestorePayload?.let { payload ->
        ConfirmRestoreDialog(
            payload = payload,
            onConfirm = { viewModel.confirmRestore() },
            onDismiss = { viewModel.dismissRestoreDialog() },
        )
    }
}
