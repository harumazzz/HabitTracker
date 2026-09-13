package com.haruma.habit.tracker.ui.templates

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.haruma.habit.tracker.R
import com.haruma.habit.tracker.data.HabitCategory
import com.haruma.habit.tracker.data.HabitTemplate
import com.haruma.habit.tracker.ui.form.HabitIcon

import androidx.compose.runtime.rememberCoroutineScope
import com.skydoves.flexible.bottomsheet.material3.FlexibleBottomSheet
import com.skydoves.flexible.core.FlexibleSheetSize
import com.skydoves.flexible.core.rememberFlexibleBottomSheetState
import kotlinx.coroutines.launch

@Composable
fun HabitTemplatesSheet(
    onDismiss: () -> Unit,
    onSelectTemplate: (HabitTemplate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberFlexibleBottomSheetState(
        flexibleSheetSize = FlexibleSheetSize(
            fullyExpanded = 0.95f,
            intermediatelyExpanded = 0.82f,
            slightlyExpanded = 0.15f,
        ),
        isModal = true,
        skipSlightlyExpanded = true,
    )
    var selectedCategory by remember { mutableStateOf(HabitCategory.ALL) }

    val filteredTemplates = remember(selectedCategory) {
        if (selectedCategory == HabitCategory.ALL) {
            HabitTemplate.ALL_TEMPLATES
        } else {
            HabitTemplate.ALL_TEMPLATES.filter { it.category == selectedCategory }
        }
    }

    FlexibleBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(top = 4.dp),
            ) {
                Text(
                    text = stringResource(R.string.templates_sheet_title),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.templates_sheet_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(HabitCategory.entries) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(stringResource(category.titleResId)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 4.dp,
                    bottom = 20.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(
                    items = filteredTemplates,
                    key = { it.id },
                ) { template ->
                    TemplateItemCard(
                        template = template,
                        onAdd = {
                            coroutineScope.launch {
                                sheetState.hide()
                                onSelectTemplate(template)
                                onDismiss()
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun TemplateItemCard(
    template: HabitTemplate,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val templateColor = Color(template.colorArgb)
    val habitIcon = HabitIcon.fromKey(template.emoji)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(
                        color = templateColor.copy(alpha = 0.15f),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = habitIcon.imageVector,
                    contentDescription = null,
                    tint = templateColor,
                    modifier = Modifier.size(24.dp),
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = stringResource(template.nameResId),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(template.descResId),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            FilledTonalButton(
                onClick = onAdd,
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                shape = CircleShape,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.templates_add_button),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
            }
        }
    }
}
