package com.haruma.habit.tracker.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.haruma.habit.tracker.MainActivity
import com.haruma.habit.tracker.R
import com.haruma.habit.tracker.data.HabitRepository
import com.haruma.habit.tracker.data.HabitWidgetPreferences
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

@EntryPoint
@InstallIn(SingletonComponent::class)
interface HabitWidgetEntryPoint {
    fun habitRepository(): HabitRepository
}

class HabitAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pendingResult = goAsync()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        scope.launch {
            try {
                for (widgetId in appWidgetIds) {
                    updateSingleWidget(context, appWidgetManager, widgetId)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        for (widgetId in appWidgetIds) {
            HabitWidgetPreferences.removeWidget(context, widgetId)
        }
    }

    companion object {
        private fun createColoredFolderBitmap(context: Context, colorArgb: Int): Bitmap {
            val drawable = ContextCompat.getDrawable(context, R.drawable.ic_widget_folder)?.mutate()
            val sizePx = (18 * context.resources.displayMetrics.density).toInt().coerceAtLeast(1)
            val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable?.let {
                DrawableCompat.setTint(it, colorArgb)
                it.setBounds(0, 0, sizePx, sizePx)
                it.draw(canvas)
            }
            return bitmap
        }

        suspend fun updateSingleWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
        ) {
            val habitId = HabitWidgetPreferences.getHabitId(context, appWidgetId)
            val views = RemoteViews(context.packageName, R.layout.widget_habit)

            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val openAppPendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, openAppPendingIntent)

            if (habitId <= 0) {
                views.setTextViewText(R.id.widget_habit_name, context.getString(R.string.widget_no_habits))
                views.setTextViewText(R.id.widget_habit_category, context.getString(R.string.widget_name))
                views.setViewVisibility(R.id.widget_habit_stage, View.GONE)
                views.setViewVisibility(R.id.widget_streak_row, View.GONE)
                views.setViewVisibility(R.id.widget_status_icon, View.GONE)
                appWidgetManager.updateAppWidget(appWidgetId, views)
                return
            }

            try {
                val entryPoint = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    HabitWidgetEntryPoint::class.java
                )
                val repo = entryPoint.habitRepository()
                val habit = repo.getById(habitId)

                if (habit == null || habit.isArchived) {
                    views.setTextViewText(R.id.widget_habit_name, context.getString(R.string.widget_no_habits))
                    views.setTextViewText(R.id.widget_habit_category, context.getString(R.string.widget_name))
                    views.setViewVisibility(R.id.widget_habit_stage, View.GONE)
                    views.setViewVisibility(R.id.widget_streak_row, View.GONE)
                    views.setViewVisibility(R.id.widget_status_icon, View.GONE)
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                    return
                }

                val categoryResId = HabitWidgetHelper.getCategoryResId(habit.emoji)
                views.setTextViewText(R.id.widget_habit_category, context.getString(categoryResId))
                views.setImageViewBitmap(
                    R.id.widget_category_icon,
                    createColoredFolderBitmap(context, habit.colorArgb)
                )

                views.setTextViewText(R.id.widget_habit_name, habit.name)

                val completions = repo.observeCompletionsInRange(
                    LocalDate.now().minusDays(365),
                    LocalDate.now()
                ).first().filter { it.habitId == habit.id }

                val todayEpochDay = LocalDate.now().toEpochDay()
                val daysSet = completions.map { it.completedDateEpochDay }.toSet()
                val isCompletedToday = daysSet.contains(todayEpochDay)

                var streak = 0
                var checkDay = if (isCompletedToday) todayEpochDay else if (daysSet.contains(todayEpochDay - 1)) todayEpochDay - 1 else null
                while (checkDay != null && daysSet.contains(checkDay)) {
                    streak++
                    checkDay--
                }

                val stageResId = HabitWidgetHelper.getGrowthStageResId(streak)
                views.setTextViewText(R.id.widget_habit_stage, context.getString(stageResId))
                views.setViewVisibility(R.id.widget_habit_stage, View.VISIBLE)

                views.setTextViewText(R.id.widget_habit_streak, context.getString(R.string.widget_streak_format, streak))
                views.setViewVisibility(R.id.widget_streak_row, View.VISIBLE)

                if (isCompletedToday) {
                    views.setViewVisibility(R.id.widget_status_icon, View.VISIBLE)
                } else {
                    views.setViewVisibility(R.id.widget_status_icon, View.GONE)
                }

                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (_: Exception) {
                views.setTextViewText(R.id.widget_habit_name, context.getString(R.string.widget_name))
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, HabitAppWidgetProvider::class.java)
            val widgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (widgetIds == null || widgetIds.isEmpty()) return

            val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            scope.launch {
                for (id in widgetIds) {
                    updateSingleWidget(context, appWidgetManager, id)
                }
            }
        }
    }
}
