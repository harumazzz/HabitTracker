package com.haruma.habit.tracker.ui.settings.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.haruma.habit.tracker.R

@Composable
fun RateUsDialog(
    onDismiss: () -> Unit,
    onRatingSubmitted: (Int) -> Unit,
) {
    var selectedRating by remember { mutableIntStateOf(0) }
    var rowWidthPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.rate_us_title),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(R.string.rate_us_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            rowWidthPx = coordinates.size.width
                        }
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                if (rowWidthPx > 0) {
                                    val starWidth = rowWidthPx / 5f
                                    val tappedStar = (offset.x / starWidth).toInt().coerceIn(0, 4) + 1
                                    selectedRating = tappedStar
                                }
                            }
                        }
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures { change, _ ->
                                change.consume()
                                if (rowWidthPx > 0) {
                                    val starWidth = rowWidthPx / 5f
                                    val draggedStar = (change.position.x / starWidth).toInt().coerceIn(0, 4) + 1
                                    selectedRating = draggedStar
                                }
                            }
                        },
                ) {
                    for (i in 1..5) {
                        val isSelected = i <= selectedRating
                        val scale by animateFloatAsState(
                            targetValue = if (isSelected) 1.2f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium,
                            ),
                            label = "starScale",
                        )
                        Icon(
                            imageVector = if (isSelected) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = null,
                            tint = if (isSelected) Color(0xFFFFB400) else MaterialTheme.colorScheme.surfaceContainerHighest,
                            modifier = Modifier
                                .size(48.dp)
                                .scale(scale),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onRatingSubmitted(selectedRating) },
                enabled = selectedRating > 0,
            ) {
                Text(stringResource(R.string.rate_us_submit))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.rate_us_cancel))
            }
        },
    )
}
