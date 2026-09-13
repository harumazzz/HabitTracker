package com.haruma.habit.tracker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haruma.habit.tracker.R

@Composable
fun HabitProgressCircle(
    currentCount: Int,
    targetCount: Int,
    habitColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val effectiveTarget = targetCount.coerceAtLeast(1)
    val isCompleted = currentCount >= effectiveTarget
    val rawProgress = (currentCount.toFloat() / effectiveTarget).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = rawProgress,
        label = "progress_anim",
    )

    val animatedBgColor by animateColorAsState(
        targetValue = if (isCompleted) habitColor else habitColor.copy(alpha = 0.08f),
        label = "bg_color_anim",
    )

    val contentDesc = if (isCompleted) {
        stringResource(R.string.cd_habit_completed)
    } else {
        stringResource(R.string.cd_increment_habit)
    }

    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(animatedBgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, radius = 24.dp),
                role = Role.Button,
                onClickLabel = contentDesc,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (!isCompleted) {
            CircularProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxSize(),
                color = habitColor,
                trackColor = habitColor.copy(alpha = 0.2f),
                strokeWidth = 3.dp,
                gapSize = 0.dp,
            )
        }

        if (isCompleted) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = contentDesc,
                modifier = Modifier.size(22.dp),
                tint = Color.White,
            )
        } else {
            Text(
                text = "$currentCount/$effectiveTarget",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = habitColor,
                maxLines = 1,
            )
        }
    }
}
