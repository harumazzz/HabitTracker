package com.haruma.habit.tracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.haruma.habit.tracker.R

@Composable
fun AppCheckbox(
    checked: Boolean,
    onCheckedChange: (() -> Unit)?,
    modifier: Modifier = Modifier,
    checkedColor: Color = MaterialTheme.colorScheme.primary,
    uncheckedColor: Color = MaterialTheme.colorScheme.outlineVariant,
    checkmarkColor: Color = Color.White,
    size: Dp = 26.dp,
    enabled: Boolean = true,
) {
    val scaleAnim by animateFloatAsState(
        targetValue = if (checked) 1f else 0.92f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "checkboxScale",
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (checked) checkedColor else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "checkboxBackground",
    )

    val borderColor by animateColorAsState(
        targetValue = if (checked) checkedColor else uncheckedColor,
        animationSpec = tween(durationMillis = 200),
        label = "checkboxBorder",
    )

    val clickableModifier = if (onCheckedChange != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(bounded = false, radius = size),
            enabled = enabled,
            role = Role.Checkbox,
            onClick = onCheckedChange,
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .size(size)
            .scale(scaleAnim)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(
                border = BorderStroke(1.5.dp, borderColor),
                shape = CircleShape,
            )
            .then(clickableModifier),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(
            visible = checked,
            enter = scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
            ) + fadeIn(animationSpec = tween(150)),
            exit = scaleOut(animationSpec = tween(100)) + fadeOut(animationSpec = tween(100)),
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = stringResource(R.string.cd_complete_habit),
                modifier = Modifier.size(size * 0.62f),
                tint = checkmarkColor,
            )
        }
    }
}
