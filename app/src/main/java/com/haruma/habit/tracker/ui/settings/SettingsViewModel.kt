package com.haruma.habit.tracker.ui.settings

import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.haruma.habit.tracker.data.HabitRepository
import com.haruma.habit.tracker.data.UserPreferencesRepository
import com.haruma.habit.tracker.data.backup.BackupPayload
import com.haruma.habit.tracker.data.backup.GoogleDriveBackupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface BackupUiEvent {
    data object BackupSuccess : BackupUiEvent
    data class BackupError(val cause: Throwable?) : BackupUiEvent
    data object RestoreSuccess : BackupUiEvent
    data class RestoreError(val cause: Throwable?) : BackupUiEvent
    data object RestoreNotFound : BackupUiEvent
    data class RequestSignIn(
        val intent: Intent,
        val isForBackup: Boolean,
    ) : BackupUiEvent
    data class RequestDriveAuthorization(val intent: Intent) : BackupUiEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: UserPreferencesRepository,
    private val habitRepository: HabitRepository,
    private val googleDriveBackupManager: GoogleDriveBackupManager,
) : ViewModel() {

    val themeMode = prefs.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "system")

    val notificationsEnabled = prefs.notificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val defaultReminderMinutes = prefs.defaultReminderMinutes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 480)

    val language = prefs.language
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "en")

    val lastBackupTime = prefs.lastBackupTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), -1L)

    val backupAccountEmail = prefs.backupAccountEmail
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    private val _isBackingUp = MutableStateFlow(false)
    val isBackingUp = _isBackingUp.asStateFlow()

    private val _isRestoring = MutableStateFlow(false)
    val isRestoring = _isRestoring.asStateFlow()

    private val _pendingRestorePayload = MutableStateFlow<BackupPayload?>(null)
    val pendingRestorePayload = _pendingRestorePayload.asStateFlow()

    private val _backupUiEvent = MutableSharedFlow<BackupUiEvent>()
    val backupUiEvent = _backupUiEvent.asSharedFlow()

    private var pendingAuthorizationAccount: GoogleSignInAccount? = null
    private var pendingAuthorizationIsForBackup = true

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

    fun startBackup() {
        val account = googleDriveBackupManager.getSignedInAccount()
        if (account == null) {
            requestSignIn(isForBackup = true)
        } else {
            performBackup(account)
        }
    }

    fun startRestore() {
        val account = googleDriveBackupManager.getSignedInAccount()
        if (account == null) {
            requestSignIn(isForBackup = false)
        } else {
            performRestore(account)
        }
    }

    private fun requestSignIn(isForBackup: Boolean) {
        val intent = googleDriveBackupManager.getSignInClient().signInIntent
        viewModelScope.launch {
            _backupUiEvent.emit(BackupUiEvent.RequestSignIn(intent, isForBackup))
        }
    }

    fun handleSignInResult(data: Intent?, isForBackup: Boolean) {
        val result = runCatching {
            requireNotNull(data)
            GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException::class.java)
        }
        result.onSuccess { account ->
            if (isForBackup) performBackup(account)
            else performRestore(account)
        }.onFailure { error ->
            Log.e("HabitBackup", "Google Sign-In failed", error)
            viewModelScope.launch {
                emitAuthorizationError(error, isForBackup)
            }
        }
    }

    private suspend fun emitAuthorizationError(error: Throwable, isForBackup: Boolean) {
        val event = if (isForBackup) BackupUiEvent.BackupError(error)
        else BackupUiEvent.RestoreError(error)
        _backupUiEvent.emit(event)
    }

    private fun performBackup(account: GoogleSignInAccount) {
        viewModelScope.launch {
            _isBackingUp.value = true
            try {
                val payload = habitRepository.createBackupPayload()
                val result = googleDriveBackupManager.uploadBackup(account, payload)
                result.onSuccess { meta ->
                    prefs.setLastBackupInfo(meta.modifiedTimeMillis, account.email.orEmpty())
                    _backupUiEvent.emit(BackupUiEvent.BackupSuccess)
                }.onFailure { err ->
                    handleBackupFailure(err, account, isForBackup = true)
                }
            } catch (e: Exception) {
                Log.e("HabitBackup", "Backup failed", e)
                _backupUiEvent.emit(BackupUiEvent.BackupError(e))
            } finally {
                _isBackingUp.value = false
            }
        }
    }

    private fun performRestore(account: GoogleSignInAccount) {
        viewModelScope.launch {
            _isRestoring.value = true
            try {
                val result = googleDriveBackupManager.fetchBackup(account)
                result.onSuccess { payload ->
                    if (payload == null) {
                        _backupUiEvent.emit(BackupUiEvent.RestoreNotFound)
                    } else {
                        _pendingRestorePayload.value = payload
                    }
                }.onFailure { err ->
                    handleBackupFailure(err, account, isForBackup = false)
                }
            } catch (e: Exception) {
                Log.e("HabitBackup", "Restore failed", e)
                _backupUiEvent.emit(BackupUiEvent.RestoreError(e))
            } finally {
                _isRestoring.value = false
            }
        }
    }

    private suspend fun handleBackupFailure(
        error: Throwable,
        account: GoogleSignInAccount,
        isForBackup: Boolean,
    ) {
        Log.e("HabitBackup", "Google Drive request failed", error)
        if (error is UserRecoverableAuthIOException) {
            pendingAuthorizationAccount = account
            pendingAuthorizationIsForBackup = isForBackup
            _backupUiEvent.emit(BackupUiEvent.RequestDriveAuthorization(error.intent))
        } else {
            emitAuthorizationError(error, isForBackup)
        }
    }

    fun handleDriveAuthorizationResult(granted: Boolean) {
        val account = pendingAuthorizationAccount
        val isForBackup = pendingAuthorizationIsForBackup
        pendingAuthorizationAccount = null
        if (!granted || account == null) {
            viewModelScope.launch {
                emitAuthorizationError(IllegalStateException(), isForBackup)
            }
            return
        }
        if (isForBackup) performBackup(account)
        else performRestore(account)
    }

    fun confirmRestore() {
        val payload = _pendingRestorePayload.value ?: return
        viewModelScope.launch {
            _isRestoring.value = true
            try {
                habitRepository.restoreFromPayload(payload)
                _pendingRestorePayload.value = null
                _backupUiEvent.emit(BackupUiEvent.RestoreSuccess)
            } catch (e: Exception) {
                _backupUiEvent.emit(BackupUiEvent.RestoreError(e))
            } finally {
                _isRestoring.value = false
            }
        }
    }

    fun dismissRestoreDialog() {
        _pendingRestorePayload.value = null
    }
}

fun String.isDark(isSystemDark: Boolean = false): Boolean = when (this) {
    "dark"  -> true
    "light" -> false
    else    -> isSystemDark
}
