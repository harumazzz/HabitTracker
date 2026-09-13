package com.haruma.habit.tracker.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.haruma.habit.tracker.R
import com.haruma.habit.tracker.data.HabitEntity
import com.haruma.habit.tracker.data.HabitRepository
import com.haruma.habit.tracker.data.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val workManager: WorkManager,
    private val habitRepository: HabitRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun start() {
        createNotificationChannel()
        scope.launch {
            combine(
                userPreferencesRepository.notificationsEnabled,
                habitRepository.observeAllHabits(),
            ) { enabled, habits ->
                Pair(enabled, habits)
            }.collectLatest { (enabled, habits) ->
                if (!enabled) {
                    cancelAll()
                } else {
                    syncHabitReminders(habits)
                }
            }
        }
    }

    fun scheduleReminder(habit: HabitEntity) {
        val reminderMinutes = habit.reminderTimeMinutes ?: return
        if (habit.isArchived) {
            cancelReminder(habit.id)
            return
        }

        val now = LocalDateTime.now()
        var targetTime = now.withHour(reminderMinutes / 60)
            .withMinute(reminderMinutes % 60)
            .withSecond(0)
            .withNano(0)

        if (!targetTime.isAfter(now)) {
            targetTime = targetTime.plusDays(1)
        }

        val initialDelayMillis = Duration.between(now, targetTime).toMillis()

        val request = PeriodicWorkRequestBuilder<HabitReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(HabitReminderWorker.KEY_HABIT_ID to habit.id))
            .addTag(TAG_HABIT_REMINDER)
            .build()

        workManager.enqueueUniquePeriodicWork(
            getWorkName(habit.id),
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    fun cancelReminder(habitId: Int) {
        workManager.cancelUniqueWork(getWorkName(habitId))
    }

    fun cancelAll() {
        workManager.cancelAllWorkByTag(TAG_HABIT_REMINDER)
    }

    private fun syncHabitReminders(habits: List<HabitEntity>) {
        val activeWithReminder = habits.filter { !it.isArchived && it.reminderTimeMinutes != null }

        habits.filter { it.isArchived || it.reminderTimeMinutes == null }.forEach {
            cancelReminder(it.id)
        }

        activeWithReminder.forEach { habit ->
            scheduleReminder(habit)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                HabitReminderWorker.CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = context.getString(R.string.notification_channel_desc)
                enableLights(true)
                enableVibration(true)
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun getWorkName(habitId: Int): String = "habit_reminder_$habitId"

    companion object {
        const val TAG_HABIT_REMINDER = "tag_habit_reminder"
    }
}
