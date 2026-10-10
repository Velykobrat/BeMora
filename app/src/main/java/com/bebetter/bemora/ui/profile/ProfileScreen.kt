package com.bebetter.bemora.ui.profile

import androidx.compose.ui.res.stringResource
import com.bebetter.bemora.R
import com.bebetter.bemora.ui.components.localizedLabel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bebetter.bemora.domain.model.label
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ProfileScreen(viewModel: ProfileViewModel = viewModel()) {
    val state = viewModel.uiState
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(stringResource(R.string.profile), fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.your_library), style = MaterialTheme.typography.titleLarge)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(stringResource(R.string.label_1_d_items, state.total), style = MaterialTheme.typography.headlineMedium)
                Text(stringResource(R.string.label_1_d_completed, state.completedPercentage))
            }
        }
        state.typeCounts.forEach { (type, count) ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(type.localizedLabel())
                Text(count.toString(), fontWeight = FontWeight.Bold)
            }
        }
        state.counts.forEach { (status, count) ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(status.localizedLabel())
                    Text(count.toString(), fontWeight = FontWeight.Bold)
                }
            }
        }
        if (state.total == 0) {
            Text(stringResource(R.string.add_movies_books_or_games_to_your_library_to_start_tracking_your_))
        }
    }
}
