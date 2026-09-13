package com.haruma.habit.tracker.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.haruma.habit.tracker.R
import com.haruma.habit.tracker.ui.stats.components.ActivityHeatmap
import com.haruma.habit.tracker.ui.stats.components.HabitProgressList
import com.haruma.habit.tracker.ui.stats.components.StatsBarChart
import com.haruma.habit.tracker.ui.stats.components.StatsSummaryCard
import com.haruma.habit.tracker.ui.stats.components.StatsTimeRangeFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: StatsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.stats_title),
                        fontWeight = FontWeight.Bold,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    StatsTimeRangeFilter(
                        selectedRange = uiState.selectedRange,
                        onRangeSelected = { viewModel.setTimeRange(it) },
                    )
                }

                item {
                    StatsSummaryCard(
                        currentStreak = uiState.currentStreak,
                        longestStreak = uiState.longestStreak,
                        completionRate = uiState.completionRate,
                    )
                }

                item {
                    StatsBarChart(
                        dailyStats = uiState.rangeDailyStats,
                        timeRange = uiState.selectedRange,
                    )
                }

                item {
                    ActivityHeatmap(
                        activityDays = uiState.activityDays,
                    )
                }

                item {
                    HabitProgressList(
                        habits = uiState.habitStatsList,
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
