package com.haruma.habit.tracker.ui.form

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector

enum class HabitIcon(val iconName: String, val imageVector: ImageVector) {
    CHECK("check", Icons.Default.Check),
    FITNESS("fitness", Icons.Default.FitnessCenter),
    WATER("water", Icons.Default.WaterDrop),
    BOOK("book", Icons.Default.MenuBook),
    MEDITATION("meditation", Icons.Default.SelfImprovement),
    RUN("run", Icons.Default.DirectionsRun),
    BIKE("bike", Icons.Default.DirectionsBike),
    RESTAURANT("restaurant", Icons.Default.Restaurant),
    BEDTIME("bedtime", Icons.Default.Bedtime),
    EDIT("edit", Icons.Default.Edit),
    STAR("star", Icons.Default.Star),
    FAVORITE("favorite", Icons.Default.Favorite),
    LIGHTBULB("lightbulb", Icons.Default.Lightbulb),
    MUSIC("music", Icons.Default.MusicNote),
    SAVINGS("savings", Icons.Default.Savings);

    companion object {
        fun fromKey(key: String): HabitIcon =
            entries.firstOrNull {
                it.iconName.equals(key, ignoreCase = true) || it.name.equals(key, ignoreCase = true)
            } ?: CHECK
    }
}
