package com.haruma.habit.tracker.ui.stats

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class Achievement(
    val id: String,
    @param:StringRes val titleResId: Int,
    @param:StringRes val descResId: Int,
    val icon: ImageVector,
    val color: Color,
    val isUnlocked: Boolean,
    val progress: Float,
    val progressLabel: String,
)
