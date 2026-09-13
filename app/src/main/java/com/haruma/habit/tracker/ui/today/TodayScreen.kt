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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.haruma.habit.tracker.ui.components.HabitItemCardShimmer
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
    val snackbarHostState = remember { SnackbarHostState() }

    val habits = viewModel.habits.collectAsLazyPagingItems()
    val completedHabitIds by viewModel.completedHabitIds.collectAsStateWithLifecycle()
    val todayProgress by viewModel.todayProgress.collectAsStateWithLifecycle()
    val streaks by viewModel.habitStreaks.collectAsStateWithLifecycle()

    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(habits.loadState.refresh) {
        if (habits.loadState.refresh !is LoadState.Loading) {
            isRefreshing = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.today_title),
                        fontWeight = FontWeight.Bold,
                    )
                },
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
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                habits.refresh()
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
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
                                HabitItemCard(
                                    habit = habit,
                                    isCompleted = completedHabitIds.contains(habit.id),
                                    streak = streaks[habit.id] ?: 0,
                                    onToggle = { viewModel.toggleHabit(habit.id) },
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
    }
}
