package com.haruma.habit.tracker.ui.today

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.haruma.habit.tracker.R
import com.haruma.habit.tracker.ui.components.AppButton
import com.haruma.habit.tracker.ui.components.AppButtonVariant
import com.haruma.habit.tracker.ui.components.AppErrorView
import com.haruma.habit.tracker.ui.components.CustomToastHost
import com.haruma.habit.tracker.ui.components.HabitItemCardShimmer
import com.haruma.habit.tracker.ui.components.rememberCustomToastState
import com.haruma.habit.tracker.ui.templates.HabitTemplatesSheet
import com.haruma.habit.tracker.ui.today.components.HabitItemCard
import com.haruma.habit.tracker.ui.today.components.TodayEmptyView
import com.haruma.habit.tracker.ui.today.components.TodayHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    onAddHabit: () -> Unit,
    onEditHabit: (Int) -> Unit,
    viewModel: TodayViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val toastState = rememberCustomToastState()

    val habits = viewModel.habits.collectAsLazyPagingItems()
    val completedHabitIds by viewModel.completedHabitIds.collectAsStateWithLifecycle()
    val todayCompletions by viewModel.todayCompletionsMap.collectAsStateWithLifecycle()
    val todayProgress by viewModel.todayProgress.collectAsStateWithLifecycle()
    val streaks by viewModel.habitStreaks.collectAsStateWithLifecycle()

    var isRefreshing by remember { mutableStateOf(false) }
    var showTemplatesSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is TodayEvent.TargetAlreadyReached -> {
                    toastState.show(context.getString(R.string.toast_target_already_reached, event.habitName))
                }
                is TodayEvent.HabitAdded -> {
                    toastState.show(context.getString(R.string.toast_habit_added_from_template, event.habitName))
                }
            }
        }
    }

    LaunchedEffect(habits.loadState.refresh) {
        if (habits.loadState.refresh !is LoadState.Loading) {
            isRefreshing = false
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.today_title),
                        fontWeight = FontWeight.Bold,
                    )
                },
                actions = {
                    IconButton(onClick = { showTemplatesSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = stringResource(R.string.templates_sheet_title),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddHabit) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.today_add_habit),
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    isRefreshing = true
                    habits.refresh()
                },
                modifier = Modifier.fillMaxSize(),
            ) {
                val refreshState = habits.loadState.refresh

                when {
                    refreshState is LoadState.Error && habits.itemCount == 0 -> {
                        AppErrorView(
                            onRetry = { habits.retry() },
                        )
                    }
                    refreshState is LoadState.Loading && habits.itemCount == 0 && !isRefreshing -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            TodayHeader(
                                completedCount = 0,
                                totalCount = 0,
                            )
                            repeat(5) {
                                HabitItemCardShimmer()
                            }
                        }
                    }
                    refreshState is LoadState.NotLoading && habits.itemCount == 0 -> {
                        TodayEmptyView(
                            onAddHabit = onAddHabit,
                            onExploreTemplates = { showTemplatesSheet = true },
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 88.dp),
                        ) {
                            item(key = "today_header") {
                                TodayHeader(
                                    completedCount = todayProgress.first,
                                    totalCount = todayProgress.second,
                                )
                            }

                            items(
                                count = habits.itemCount,
                                key = habits.itemKey { it.id },
                            ) { index ->
                                val habit = habits[index]
                                if (habit != null) {
                                    val count = todayCompletions[habit.id] ?: 0
                                    val isCompleted = completedHabitIds.contains(habit.id)
                                    HabitItemCard(
                                        habit = habit,
                                        currentCount = count,
                                        isCompleted = isCompleted,
                                        streak = streaks[habit.id] ?: 0,
                                        onIncrement = { viewModel.incrementHabit(habit) },
                                        onClick = { onEditHabit(habit.id) },
                                    )
                                }
                            }

                            if (habits.loadState.append is LoadState.Loading) {
                                item(key = "append_loading") {
                                    HabitItemCardShimmer()
                                }
                            }

                            if (habits.loadState.append is LoadState.Error) {
                                item(key = "append_error") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        AppButton(
                                            text = stringResource(R.string.error_retry),
                                            onClick = { habits.retry() },
                                            variant = AppButtonVariant.TONAL,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            CustomToastHost(
                state = toastState,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }
    }

    if (showTemplatesSheet) {
        HabitTemplatesSheet(
            onDismiss = { showTemplatesSheet = false },
            onSelectTemplate = { template ->
                val habitName = context.getString(template.nameResId)
                viewModel.addHabitFromTemplate(template, habitName)
            },
        )
    }
}
