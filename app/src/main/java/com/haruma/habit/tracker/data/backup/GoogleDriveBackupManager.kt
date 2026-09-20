package com.haruma.habit.tracker.data.backup

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleDriveBackupManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
    }

    companion object {
        const val BACKUP_FILE_NAME = "habit_tracker_backup.json"
    }

    fun getSignInClient(): GoogleSignInClient {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
            .build()
        return GoogleSignIn.getClient(context, options)
    }

    fun getSignedInAccount(): GoogleSignInAccount? {
        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return null
        return account.takeIf {
            GoogleSignIn.hasPermissions(it, Scope(DriveScopes.DRIVE_APPDATA))
        }
    }

    private fun getDriveService(account: GoogleSignInAccount): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(DriveScopes.DRIVE_APPDATA),
        )
        credential.selectedAccount = account.account
        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential,
        )
            .setApplicationName("HabitTracker")
            .build()
    }

    suspend fun uploadBackup(
        account: GoogleSignInAccount,
        payload: BackupPayload,
    ): Result<DriveBackupMetadata> = withContext(Dispatchers.IO) {
        runCatching {
            val driveService = getDriveService(account)
            val jsonString = json.encodeToString(payload)
            val contentByteArray = jsonString.toByteArray(Charsets.UTF_8)
            val mediaContent = ByteArrayContent("application/json", contentByteArray)

            val fileList = driveService.files().list()
                .setSpaces("appDataFolder")
                .setQ("name = '$BACKUP_FILE_NAME' and trashed = false")
                .setFields("files(id, name, modifiedTime, size)")
                .execute()

            val existingFile = fileList.files?.firstOrNull()
            val uploadedFile = if (existingFile != null) {
                driveService.files().update(existingFile.id, File(), mediaContent)
                    .setFields("id, name, modifiedTime, size")
                    .execute()
            } else {
                val fileMetadata = File().apply {
                    name = BACKUP_FILE_NAME
                    parents = listOf("appDataFolder")
                    mimeType = "application/json"
                }
                driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id, name, modifiedTime, size")
                    .execute()
            }

            DriveBackupMetadata(
                fileId = uploadedFile.id.orEmpty(),
                fileName = uploadedFile.name ?: BACKUP_FILE_NAME,
                modifiedTimeMillis = uploadedFile.modifiedTime?.value ?: System.currentTimeMillis(),
                sizeBytes = uploadedFile.size?.toLong() ?: contentByteArray.size.toLong(),
                habitsCount = payload.habits.size,
                completionsCount = payload.completions.size,
            )
        }
    }

    suspend fun fetchBackup(account: GoogleSignInAccount): Result<BackupPayload?> =
        withContext(Dispatchers.IO) {
            runCatching {
                val driveService = getDriveService(account)
                val fileList = driveService.files().list()
                    .setSpaces("appDataFolder")
                    .setQ("name = '$BACKUP_FILE_NAME' and trashed = false")
                    .setFields("files(id, name, modifiedTime, size)")
                    .execute()

                val file = fileList.files?.firstOrNull() ?: return@runCatching null
                val inputStream = driveService.files().get(file.id).executeMediaAsInputStream()
                val jsonString = inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                json.decodeFromString<BackupPayload>(jsonString)
            }
        }
}
