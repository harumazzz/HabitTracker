package com.haruma.habit.tracker.di

import android.content.Context
import androidx.room.Room
import com.haruma.habit.tracker.data.AppDatabase
import com.haruma.habit.tracker.data.HabitRepository
import com.haruma.habit.tracker.data.UserPreferencesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "habit_tracker.db")
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()

    @Provides
    @Singleton
    fun provideHabitRepository(@ApplicationContext ctx: Context, db: AppDatabase): HabitRepository =
        HabitRepository(ctx, db.habitDao(), db.completionDao())

    @Provides
    @Singleton
    fun provideUserPreferencesRepository(@ApplicationContext ctx: Context): UserPreferencesRepository =
        UserPreferencesRepository(ctx)

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext ctx: Context): androidx.work.WorkManager =
        androidx.work.WorkManager.getInstance(ctx)
}
