package com.haruma.habit.tracker.ui.form

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.haruma.habit.tracker.R
import com.haruma.habit.tracker.ui.components.AppButton
import com.haruma.habit.tracker.ui.components.AppButtonVariant
import com.haruma.habit.tracker.ui.components.AppNumberStepper
import com.haruma.habit.tracker.ui.components.AppTextField
import com.haruma.habit.tracker.ui.components.AppTimePickerDialog
import com.haruma.habit.tracker.ui.components.ConfirmDeleteDialog
import com.haruma.habit.tracker.ui.form.components.ColorPaletteRow
import com.haruma.habit.tracker.ui.form.components.DayOfWeekSelector
import com.haruma.habit.tracker.ui.form.components.FrequencySelector
import com.haruma.habit.tracker.ui.form.components.HabitPreviewHeader
import com.haruma.habit.tracker.ui.form.components.IconPickerRow
import com.haruma.habit.tracker.ui.form.components.ReminderSelectorCard
import com.skydoves.flexible.bottomsheet.material3.FlexibleBottomSheet
import com.skydoves.flexible.core.FlexibleSheetSize
import com.skydoves.flexible.core.rememberFlexibleBottomSheetState
import kotlinx.coroutines.launch

@Composable
fun HabitFormSheet(
    onDismiss: () -> Unit,
    viewModel: HabitFormViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showTimePickerDialog by remember { mutableStateOf(false) }

    val sheetState = rememberFlexibleBottomSheetState(
        flexibleSheetSize = FlexibleSheetSize(
            fullyExpanded = 0.95f,
            intermediatelyExpanded = 0.90f,
            slightlyExpanded = 0.15f,
        ),
        isModal = false,
        skipSlightlyExpanded = true,
    )

    BackHandler {
        coroutineScope.launch {
            sheetState.hide()
            onDismiss()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    coroutineScope.launch {
                        sheetState.hide()
                        onDismiss()
                    }
                },
            ),
        contentAlignment = Alignment.BottomCenter,
    ) {
        FlexibleBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(),
        ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(if (uiState.isEditMode) R.string.form_title_edit else R.string.form_title_add),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                )
                IconButton(onClick = {
                    coroutineScope.launch {
                        sheetState.hide()
                        onDismiss()
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.cd_close),
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .animateContentSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        focusManager.clearFocus()
                    },
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                HabitPreviewHeader(
                    icon = uiState.icon,
                    colorArgb = uiState.colorArgb,
                )

                AppTextField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChanged,
                    label = stringResource(R.string.form_label_name),
                    isError = uiState.nameError,
                    errorMessage = if (uiState.nameError) stringResource(R.string.form_error_name_empty) else null,
                    leadingIcon = Icons.Default.Edit,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                )

                IconPickerRow(
                    selectedIcon = uiState.icon,
                    onIconSelected = viewModel::onIconSelected,
                )

                ColorPaletteRow(
                    selectedColorArgb = uiState.colorArgb,
                    onColorSelected = viewModel::onColorSelected,
                )

                FrequencySelector(
                    selectedFrequency = uiState.frequencyType,
                    onFrequencySelected = viewModel::onFrequencySelected,
                )

                AnimatedVisibility(
                    visible = uiState.frequencyType == FrequencyType.SPECIFIC,
                    enter = expandVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow,
                        ),
                    ) + fadeIn(animationSpec = tween(220)),
                    exit = shrinkVertically(
                        animationSpec = tween(200),
                    ) + fadeOut(animationSpec = tween(150)),
                ) {
                    DayOfWeekSelector(
                        selectedDays = uiState.targetDays,
                        onDayToggled = viewModel::onDayToggled,
                    )
                }

                AppNumberStepper(
                    title = stringResource(R.string.form_label_target),
                    value = uiState.targetCount,
                    onValueChange = viewModel::onTargetCountChanged,
                )

                ReminderSelectorCard(
                    reminderTimeMinutes = uiState.reminderTimeMinutes,
                    onOpenTimePicker = { showTimePickerDialog = true },
                    onClearReminder = { viewModel.onReminderTimeSelected(null) },
                )

                Spacer(modifier = Modifier.height(8.dp))

                AppButton(
                    text = stringResource(R.string.form_save),
                    onClick = {
                        viewModel.saveHabit {
                            coroutineScope.launch {
                                sheetState.hide()
                                onDismiss()
                            }
                        }
                    },
                    variant = AppButtonVariant.PRIMARY,
                )

                if (uiState.isEditMode) {
                    AppButton(
                        text = stringResource(R.string.form_delete),
                        onClick = { showDeleteDialog = true },
                        variant = AppButtonVariant.DANGER,
                        leadingIcon = Icons.Default.Delete,
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
    }

    if (showDeleteDialog) {
        ConfirmDeleteDialog(
            title = stringResource(R.string.form_delete_confirm_title),
            message = stringResource(R.string.form_delete_confirm_body),
            onConfirm = {
                showDeleteDialog = false
                viewModel.deleteHabit {
                    coroutineScope.launch {
                        sheetState.hide()
                        onDismiss()
                    }
                }
            },
            onDismiss = { showDeleteDialog = false },
        )
    }

    if (showTimePickerDialog) {
        val safeReminder = uiState.reminderTimeMinutes?.takeIf { it in 0 until 1440 }
        val initialHour = (safeReminder?.div(60) ?: 8).coerceIn(0, 23)
        val initialMinute = (safeReminder?.rem(60) ?: 0).coerceIn(0, 59)
        AppTimePickerDialog(
            initialHour = initialHour,
            initialMinute = initialMinute,
            onTimeSelected = { hour, minute ->
                viewModel.onReminderTimeSelected(hour * 60 + minute)
                showTimePickerDialog = false
            },
            onDismiss = { showTimePickerDialog = false },
        )
    }
}
