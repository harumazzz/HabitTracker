package com.haruma.habit.tracker.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CupertinoLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    spokeCount: Int = 12,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "CupertinoLoadingIndicator")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "progress",
    )

    val currentStep = (progress * spokeCount).toInt() % spokeCount

    Canvas(
        modifier = modifier
            .size(size)
            .progressSemantics(),
    ) {
        val radius = this.size.minDimension / 2f
        val strokeWidth = radius * 0.22f
        val innerRadius = radius * 0.52f
        val outerRadius = radius - (strokeWidth / 2f)

        for (i in 0 until spokeCount) {
            val distanceBehind = (currentStep - i + spokeCount) % spokeCount
            val alpha = 0.22f + 0.78f * ((spokeCount - 1 - distanceBehind).toFloat() / (spokeCount - 1))

            rotate(
                degrees = i * (360f / spokeCount),
                pivot = center,
            ) {
                drawLine(
                    color = color.copy(alpha = alpha),
                    start = Offset(center.x, center.y - innerRadius),
                    end = Offset(center.x, center.y - outerRadius),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}
