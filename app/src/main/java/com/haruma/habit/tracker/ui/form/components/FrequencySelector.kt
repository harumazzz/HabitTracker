package com.haruma.habit.tracker.ui.form.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.haruma.habit.tracker.R
import com.haruma.habit.tracker.ui.components.AppChip
import com.haruma.habit.tracker.ui.form.FrequencyType

@Composable
fun FrequencySelector(
    selectedFrequency: FrequencyType,
    onFrequencySelected: (FrequencyType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.form_label_frequency),
            style = MaterialTheme.typography.labelLarge,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FrequencyType.entries.forEach { frequency ->
                AppChip(
                    selected = selectedFrequency == frequency,
                    onClick = { onFrequencySelected(frequency) },
                    label = stringResource(frequency.titleResId),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
