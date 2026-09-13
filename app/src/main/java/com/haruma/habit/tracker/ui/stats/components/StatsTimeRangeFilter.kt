package com.haruma.habit.tracker.ui.stats.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haruma.habit.tracker.R
import com.haruma.habit.tracker.ui.stats.StatsTimeRange

@Composable
fun StatsTimeRangeFilter(
    selectedRange: StatsTimeRange,
    onRangeSelected: (StatsTimeRange) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    val trackColor = if (isDark) {
        Color(0xFF1E1F24)
    } else {
        Color(0xFFE5E5EA)
    }

    val thumbColor = if (isDark) {
        Color(0xFF2C2D33)
    } else {
        Color.White
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(trackColor)
            .padding(3.dp),
    ) {
        val tabWidth = maxWidth / 2

        val targetIndex = if (selectedRange == StatsTimeRange.WEEKLY) 0f else 1f
        val animatedIndex by animateFloatAsState(
            targetValue = targetIndex,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
            label = "ios_tab_indicator",
        )

        Box(
            modifier = Modifier
                .offset(x = tabWidth * animatedIndex)
                .width(tabWidth)
                .fillMaxHeight()
                .shadow(
                    elevation = if (isDark) 2.dp else 4.dp,
                    shape = RoundedCornerShape(9.dp),
                    clip = false,
                )
                .clip(RoundedCornerShape(9.dp))
                .background(thumbColor),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val weeklyTextColor by animateColorAsState(
                targetValue = if (selectedRange == StatsTimeRange.WEEKLY) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                label = "weekly_text_color",
            )

            val monthlyTextColor by animateColorAsState(
                targetValue = if (selectedRange == StatsTimeRange.MONTHLY) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                label = "monthly_text_color",
            )

            val interactionSource = remember { MutableInteractionSource() }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                    ) {
                        onRangeSelected(StatsTimeRange.WEEKLY)
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.stats_range_weekly),
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = 14.sp,
                    fontWeight = if (selectedRange == StatsTimeRange.WEEKLY) FontWeight.SemiBold else FontWeight.Medium,
                    color = weeklyTextColor,
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                    ) {
                        onRangeSelected(StatsTimeRange.MONTHLY)
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.stats_range_monthly),
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = 14.sp,
                    fontWeight = if (selectedRange == StatsTimeRange.MONTHLY) FontWeight.SemiBold else FontWeight.Medium,
                    color = monthlyTextColor,
                )
            }
        }
    }
}
