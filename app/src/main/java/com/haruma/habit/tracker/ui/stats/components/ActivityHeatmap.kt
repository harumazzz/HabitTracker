package com.haruma.habit.tracker.ui.stats.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.haruma.habit.tracker.R
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

@Composable
fun ActivityHeatmap(
    activityDays: Map<LocalDate, Int>,
    modifier: Modifier = Modifier,
    weeksCount: Int = 12,
) {
    val today = remember { LocalDate.now() }
    val endOfWeek = remember(today) { today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)) }
    val startDate = remember(endOfWeek, weeksCount) {
        endOfWeek.minusWeeks((weeksCount - 1).toLong()).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }

    val weeks = remember(startDate, weeksCount) {
        (0 until weeksCount).map { weekIndex ->
            val weekStart = startDate.plusWeeks(weekIndex.toLong())
            (0L..6L).map { dayOffset -> weekStart.plusDays(dayOffset) }
        }
    }

    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        scrollState.scrollTo(scrollState.maxValue)
    }

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
                text = stringResource(R.string.stats_heatmap_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(end = 8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.day_mon_short),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.size(16.dp))
                    Text(
                        text = stringResource(R.string.day_wed_short),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.size(16.dp))
                    Text(
                        text = stringResource(R.string.day_fri_short),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.size(16.dp))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    weeks.forEach { weekDays ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            weekDays.forEach { date ->
                                val isFuture = date.isAfter(today)
                                val count = if (isFuture) -1 else (activityDays[date] ?: 0)
                                HeatmapCell(count = count)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.stats_heatmap_less),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.width(6.dp))

                HeatmapLegendCell(color = MaterialTheme.colorScheme.surfaceVariant)
                Spacer(modifier = Modifier.width(3.dp))
                HeatmapLegendCell(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                Spacer(modifier = Modifier.width(3.dp))
                HeatmapLegendCell(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.65f))
                Spacer(modifier = Modifier.width(3.dp))
                HeatmapLegendCell(color = MaterialTheme.colorScheme.primary)

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = stringResource(R.string.stats_heatmap_more),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun HeatmapCell(count: Int) {
    val color = when {
        count < 0 -> Color.Transparent
        count == 0 -> MaterialTheme.colorScheme.surfaceVariant
        count == 1 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
        count == 2 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)
        else -> MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = Modifier
            .size(16.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(color),
    )
}

@Composable
private fun HeatmapLegendCell(color: Color) {
    Box(
        modifier = Modifier
            .size(12.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(color),
    )
}
