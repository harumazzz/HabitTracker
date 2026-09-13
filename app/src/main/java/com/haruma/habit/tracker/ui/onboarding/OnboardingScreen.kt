package com.haruma.habit.tracker.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.haruma.habit.tracker.R
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val pagerState = rememberPagerState(pageCount = { OnboardingPage.entries.size })
    val scope = rememberCoroutineScope()

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
            ) { page ->
                OnboardingPageContent(page = OnboardingPage.entries[page])
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = {
                    viewModel.finish()
                    onFinish()
                }) {
                    Text(stringResource(R.string.onboarding_skip))
                }

                if (pagerState.currentPage < OnboardingPage.entries.lastIndex) {
                    Button(onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }) {
                        Text(stringResource(R.string.onboarding_next))
                    }
                } else {
                    Button(onClick = {
                        viewModel.finish()
                        onFinish()
                    }) {
                        Text(stringResource(R.string.onboarding_get_started))
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(page.titleRes))
            Spacer(modifier = Modifier.height(8.dp))
            Text(stringResource(page.bodyRes))
        }
    }
}

enum class OnboardingPage(val titleRes: Int, val bodyRes: Int) {
    TRACK(R.string.onboarding_page1_title, R.string.onboarding_page1_body),
    STREAK(R.string.onboarding_page2_title, R.string.onboarding_page2_body),
    STATS(R.string.onboarding_page3_title, R.string.onboarding_page3_body),
}
