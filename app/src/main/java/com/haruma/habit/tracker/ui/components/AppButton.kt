package com.haruma.habit.tracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

enum class AppButtonVariant {
    PRIMARY,
    TONAL,
    OUTLINED,
    DANGER,
}

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: AppButtonVariant = AppButtonVariant.PRIMARY,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
) {
    val buttonModifier = modifier
        .fillMaxWidth()
        .height(52.dp)
    val contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)

    when (variant) {
        AppButtonVariant.PRIMARY -> {
            Button(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled,
                shape = CircleShape,
                contentPadding = contentPadding,
            ) {
                ButtonContent(text = text, leadingIcon = leadingIcon)
            }
        }
        AppButtonVariant.TONAL -> {
            Button(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled,
                shape = CircleShape,
                colors = ButtonDefaults.filledTonalButtonColors(),
                contentPadding = contentPadding,
            ) {
                ButtonContent(text = text, leadingIcon = leadingIcon)
            }
        }
        AppButtonVariant.OUTLINED -> {
            OutlinedButton(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled,
                shape = CircleShape,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                contentPadding = contentPadding,
            ) {
                ButtonContent(text = text, leadingIcon = leadingIcon)
            }
        }
        AppButtonVariant.DANGER -> {
            OutlinedButton(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled,
                shape = CircleShape,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
                contentPadding = contentPadding,
            ) {
                ButtonContent(text = text, leadingIcon = leadingIcon)
            }
        }
    }
}

@Composable
private fun ButtonContent(text: String, leadingIcon: ImageVector?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}
