package com.haruma.habit.tracker

import android.app.Application
import android.content.Context
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import androidx.hilt.work.HiltWorkerFactory

@HiltAndroidApp
class HabitTrackerApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()
}
