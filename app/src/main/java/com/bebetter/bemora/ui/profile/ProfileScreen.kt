package com.bebetter.bemora.ui.profile

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
        Text("Profile", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Text("Your Library", style = MaterialTheme.typography.titleLarge)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("${state.total} items", style = MaterialTheme.typography.headlineMedium)
                Text("${state.completedPercentage}% completed")
            }
        }
        state.typeCounts.forEach { (type, count) ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(type.label)
                Text(count.toString(), fontWeight = FontWeight.Bold)
            }
        }
        state.counts.forEach { (status, count) ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(status.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() })
                    Text(count.toString(), fontWeight = FontWeight.Bold)
                }
            }
        }
        if (state.total == 0) {
            Text("Add movies, books or games to your Library to start tracking your progress.")
        }
    }
}
