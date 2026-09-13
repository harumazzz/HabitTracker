package com.haruma.habit.tracker.ui.form

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.haruma.habit.tracker.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitFormSheet(
    onDismiss: () -> Unit,
    viewModel: HabitFormViewModel = hiltViewModel(),
) {
    val sheetState = rememberModalBottomSheetState(true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(stringResource(R.string.form_title_add))
        }
    }
}
