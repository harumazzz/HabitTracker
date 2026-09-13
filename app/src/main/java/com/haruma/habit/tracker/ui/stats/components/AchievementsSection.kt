package com.haruma.habit.tracker.ui.stats.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haruma.habit.tracker.R
import com.haruma.habit.tracker.ui.stats.Achievement

@Composable
fun AchievementsSection(
    achievements: List<Achievement>,
    modifier: Modifier = Modifier,
) {
    val unlockedCount = achievements.count { it.isUnlocked }
    val totalCount = achievements.size

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.stats_achievements_title),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = stringResource(R.string.stats_achievements_summary, unlockedCount, totalCount),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                achievements.forEach { achievement ->
                    AchievementItemRow(achievement = achievement)
                }
            }
        }
    }
}

@Composable
private fun AchievementItemRow(
    achievement: Achievement,
    modifier: Modifier = Modifier,
) {
    val isUnlocked = achievement.isUnlocked
    val iconBgColor = if (isUnlocked) {
        achievement.color.copy(alpha = 0.16f)
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    }
    val iconTint = if (isUnlocked) {
        achievement.color
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isUnlocked) {
                    achievement.color.copy(alpha = 0.04f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                },
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(iconBgColor, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = achievement.icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp),
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(achievement.titleResId),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = if (isUnlocked) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                if (isUnlocked) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = stringResource(R.string.badge_unlocked),
                            tint = achievement.color,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = stringResource(R.string.badge_unlocked),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = achievement.color,
                        )
                    }
                } else {
                    Text(
                        text = achievement.progressLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = stringResource(achievement.descResId),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            if (!isUnlocked) {
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { achievement.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = achievement.color,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        }
    }
}
