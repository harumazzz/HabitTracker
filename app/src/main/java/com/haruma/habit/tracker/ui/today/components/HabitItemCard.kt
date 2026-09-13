package com.haruma.habit.tracker.ui.today.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.haruma.habit.tracker.R
import com.haruma.habit.tracker.data.HabitEntity
import com.haruma.habit.tracker.ui.components.HabitProgressCircle
import com.haruma.habit.tracker.ui.form.FrequencyType
import com.haruma.habit.tracker.ui.form.HabitIcon

@Composable
fun HabitItemCard(
    habit: HabitEntity,
    currentCount: Int,
    isCompleted: Boolean,
    streak: Int,
    onIncrement: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val habitColor = Color(habit.colorArgb)
    val habitIcon = HabitIcon.fromKey(habit.emoji)
    val frequency = FrequencyType.fromString(habit.frequencyType)

    val cardContainerColor = if (isDark) {
        Color(0xFF26282E)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardContainerColor,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(habitColor),
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = habitColor.copy(alpha = 0.12f),
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = habitIcon.imageVector,
                        contentDescription = stringResource(R.string.cd_habit_icon),
                        modifier = Modifier.size(24.dp),
                        tint = habitColor,
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = if (isCompleted) {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(frequency.titleResId),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        if (streak > 0) {
                            Spacer(modifier = Modifier.width(10.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFF9800).copy(alpha = 0.12f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = stringResource(R.string.cd_streak_icon),
                                    modifier = Modifier.size(14.dp),
                                    tint = Color(0xFFF57C00),
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = stringResource(R.string.today_streak_days, streak),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Medium,
                                    ),
                                    color = Color(0xFFE65100),
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                HabitProgressCircle(
                    currentCount = currentCount,
                    targetCount = habit.targetCount,
                    habitColor = habitColor,
                    onClick = onIncrement,
                )
            }
        }
    }
}
