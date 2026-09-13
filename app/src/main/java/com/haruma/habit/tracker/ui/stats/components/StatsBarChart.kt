package com.haruma.habit.tracker.ui.stats.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.haruma.habit.tracker.R
import com.haruma.habit.tracker.ui.stats.DayCompletionStat
import com.haruma.habit.tracker.ui.stats.StatsTimeRange
import java.time.DayOfWeek
import java.time.LocalDate

@Composable
fun StatsBarChart(
    dailyStats: List<DayCompletionStat>,
    timeRange: StatsTimeRange,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = stringResource(R.string.stats_chart_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(16.dp))

            val maxCompletions = dailyStats.maxOfOrNull { it.completedCount }?.coerceAtLeast(1) ?: 1
            val today = LocalDate.now()

            if (timeRange == StatsTimeRange.WEEKLY) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    dailyStats.forEach { stat ->
                        WeeklyBarItem(
                            stat = stat,
                            maxVal = maxCompletions,
                            isToday = stat.date == today,
                            isFuture = stat.date.isAfter(today),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            } else {
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .horizontalScroll(scrollState),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    dailyStats.forEach { stat ->
                        MonthlyBarItem(
                            stat = stat,
                            maxVal = maxCompletions,
                            isToday = stat.date == today,
                            isFuture = stat.date.isAfter(today),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeeklyBarItem(
    stat: DayCompletionStat,
    maxVal: Int,
    isToday: Boolean,
    isFuture: Boolean,
    modifier: Modifier = Modifier,
) {
    val targetFraction = if (isFuture) 0f else (stat.completedCount.toFloat() / maxVal).coerceIn(0f, 1f)
    val animatedHeightFraction by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = tween(durationMillis = 600),
        label = "weekly_bar_height",
    )

    val barColor = when {
        isToday -> MaterialTheme.colorScheme.primary
        isFuture -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        stat.completedCount > 0 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    }

    val dayLabel = when (stat.date.dayOfWeek) {
        DayOfWeek.MONDAY -> stringResource(R.string.day_mon_short)
        DayOfWeek.TUESDAY -> stringResource(R.string.day_tue_short)
        DayOfWeek.WEDNESDAY -> stringResource(R.string.day_wed_short)
        DayOfWeek.THURSDAY -> stringResource(R.string.day_thu_short)
        DayOfWeek.FRIDAY -> stringResource(R.string.day_fri_short)
        DayOfWeek.SATURDAY -> stringResource(R.string.day_sat_short)
        DayOfWeek.SUNDAY -> stringResource(R.string.day_sun_short)
        null -> ""
    }

    Column(
        modifier = modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
    ) {
        if (!isFuture && stat.completedCount > 0) {
            Text(
                text = stat.completedCount.toString(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Spacer(modifier = Modifier.height(14.dp))
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .width(18.dp)
                .height(84.dp)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(fraction = if (stat.completedCount > 0) animatedHeightFraction.coerceAtLeast(0.08f) else 0f)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                    .background(barColor),
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = dayLabel,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MonthlyBarItem(
    stat: DayCompletionStat,
    maxVal: Int,
    isToday: Boolean,
    isFuture: Boolean,
    modifier: Modifier = Modifier,
) {
    val targetFraction = if (isFuture) 0f else (stat.completedCount.toFloat() / maxVal).coerceIn(0f, 1f)
    val animatedHeightFraction by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = tween(durationMillis = 600),
        label = "monthly_bar_height",
    )

    val barColor = when {
        isToday -> MaterialTheme.colorScheme.primary
        isFuture -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        stat.completedCount > 0 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    }

    Column(
        modifier = modifier
            .width(22.dp)
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
    ) {
        if (!isFuture && stat.completedCount > 0) {
            Text(
                text = stat.completedCount.toString(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Spacer(modifier = Modifier.height(14.dp))
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .width(14.dp)
                .height(84.dp)
                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 3.dp, bottomEnd = 3.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(fraction = if (stat.completedCount > 0) animatedHeightFraction.coerceAtLeast(0.08f) else 0f)
                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 3.dp, bottomEnd = 3.dp))
                    .background(barColor),
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = stat.date.dayOfMonth.toString(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
