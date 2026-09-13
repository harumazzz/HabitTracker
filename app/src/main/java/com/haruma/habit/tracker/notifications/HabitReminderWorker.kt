package com.haruma.habit.tracker.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.haruma.habit.tracker.MainActivity
import com.haruma.habit.tracker.R
import com.haruma.habit.tracker.data.HabitEntity
import com.haruma.habit.tracker.data.HabitRepository
import com.haruma.habit.tracker.data.UserPreferencesRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.LocalDate

@HiltWorker
class HabitReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val habitRepository: HabitRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val notificationsEnabled = userPreferencesRepository.notificationsEnabled.first()
        if (!notificationsEnabled) {
            return Result.success()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS,
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return Result.success()
            }
        }

        val habitId = inputData.getInt(KEY_HABIT_ID, -1)
        val today = LocalDate.now()
        val dayOfWeek = today.dayOfWeek

        val completions = habitRepository.observeCompletionsToday().first()
        val completedHabitIds = completions.map { it.habitId }.toSet()

        if (habitId != -1) {
            val habit = habitRepository.getById(habitId) ?: return Result.success()
            if (habit.isArchived) return Result.success()
            if (isHabitDueToday(habit, dayOfWeek) && !completedHabitIds.contains(habit.id)) {
                showHabitNotification(habit)
            }
        } else {
            val allHabits = habitRepository.observeAllHabits().first()
            val dueUncompleted = allHabits.filter { habit ->
                !habit.isArchived &&
                    habit.reminderTimeMinutes != null &&
                    isHabitDueToday(habit, dayOfWeek) &&
                    !completedHabitIds.contains(habit.id)
            }

            when (dueUncompleted.size) {
                0 -> {}
                1 -> showHabitNotification(dueUncompleted.first())
                else -> showSummaryNotification(dueUncompleted.size)
            }
        }

        return Result.success()
    }

    private fun isHabitDueToday(habit: HabitEntity, dayOfWeek: DayOfWeek): Boolean {
        return when (habit.frequencyType) {
            "DAILY" -> true
            "WEEKLY" -> true
            "SPECIFIC_DAYS" -> {
                val dayKey = when (dayOfWeek) {
                    DayOfWeek.MONDAY -> "MON"
                    DayOfWeek.TUESDAY -> "TUE"
                    DayOfWeek.WEDNESDAY -> "WED"
                    DayOfWeek.THURSDAY -> "THU"
                    DayOfWeek.FRIDAY -> "FRI"
                    DayOfWeek.SATURDAY -> "SAT"
                    DayOfWeek.SUNDAY -> "SUN"
                }
                habit.targetDays.split(",").map { it.trim().uppercase() }.contains(dayKey)
            }
            else -> true
        }
    }

    private fun showHabitNotification(habit: HabitEntity) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            habit.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.notification_habit_title, habit.name))
            .setContentText(context.getString(R.string.notification_habit_body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(habit.id, notification)
    }

    private fun showSummaryNotification(count: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val body = if (count == 1) {
            context.getString(R.string.notification_general_body_single)
        } else {
            context.getString(R.string.notification_general_body_multiple, count)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.notification_general_title))
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(SUMMARY_NOTIFICATION_ID, notification)
    }

    companion object {
        const val CHANNEL_ID = "habit_reminders"
        const val KEY_HABIT_ID = "key_habit_id"
        private const val SUMMARY_NOTIFICATION_ID = 999999
    }
}
