package com.haruma.habit.tracker.ui.form

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
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

    val animOffsetY = remember { Animatable(1400f) }
    val animScrimAlpha = remember { Animatable(0f) }
    var isDismissing by remember { mutableStateOf(false) }

    val dismissWithAnimation: () -> Unit = {
        if (!isDismissing) {
            isDismissing = true
            coroutineScope.launch {
                launch {
                    animScrimAlpha.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(
                            durationMillis = 260,
                            easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f),
                        ),
                    )
                }
                launch {
                    animOffsetY.animateTo(
                        targetValue = 1400f,
                        animationSpec = tween(
                            durationMillis = 280,
                            easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f),
                        ),
                    )
                }.join()
                onDismiss()
            }
        }
    }

    LaunchedEffect(Unit) {
        launch {
            animScrimAlpha.animateTo(
                targetValue = 0.45f,
                animationSpec = tween(
                    durationMillis = 320,
                    easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f),
                ),
            )
        }
        launch {
            animOffsetY.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = 0.82f,
                    stiffness = 380f,
                ),
            )
        }
    }

    BackHandler {
        dismissWithAnimation()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = animScrimAlpha.value))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {
                    dismissWithAnimation()
                },
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 44.dp)
                .statusBarsPadding()
                .graphicsLayer {
                    translationY = animOffsetY.value
                }
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 16.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            detectVerticalDragGestures(
                                onVerticalDrag = { change, dragAmount ->
                                    change.consume()
                                    val next = (animOffsetY.value + dragAmount).coerceAtLeast(0f)
                                    coroutineScope.launch {
                                        animOffsetY.snapTo(next)
                                    }
                                },
                                onDragEnd = {
                                    if (animOffsetY.value > 150f) {
                                        dismissWithAnimation()
                                    } else {
                                        coroutineScope.launch {
                                            animOffsetY.animateTo(
                                                targetValue = 0f,
                                                animationSpec = spring(
                                                    dampingRatio = 0.82f,
                                                    stiffness = 400f,
                                                ),
                                            )
                                        }
                                    }
                                },
                                onDragCancel = {
                                    coroutineScope.launch {
                                        animOffsetY.animateTo(0f)
                                    }
                                },
                            )
                        }
                        .padding(top = 10.dp, bottom = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 36.dp, height = 5.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)),
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {
                            focusManager.clearFocus()
                        },
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(if (uiState.isEditMode) R.string.form_title_edit else R.string.form_title_add),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        )
                        IconButton(onClick = dismissWithAnimation) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }

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

                    if (uiState.frequencyType == FrequencyType.SPECIFIC) {
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
                        onClick = { viewModel.saveHabit(dismissWithAnimation) },
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
                viewModel.deleteHabit(dismissWithAnimation)
            },
            onDismiss = { showDeleteDialog = false },
        )
    }

    if (showTimePickerDialog) {
        val initialHour = uiState.reminderTimeMinutes?.div(60) ?: 8
        val initialMinute = uiState.reminderTimeMinutes?.rem(60) ?: 0
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
