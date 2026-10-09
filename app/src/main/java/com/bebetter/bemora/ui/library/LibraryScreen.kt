package com.bebetter.bemora.ui.library

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bebetter.bemora.domain.model.ContentItem
import com.bebetter.bemora.domain.model.TrackingStatus
import com.bebetter.bemora.ui.components.SearchResultCard

@Composable
fun LibraryScreen(
    onMovieClick: (ContentItem) -> Unit,
    viewModel: LibraryViewModel = viewModel()
) {
    val uiState = viewModel.uiState
    val visibleItems = uiState.visibleItems

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(modifier = Modifier.height(24.dp))
        Text("Library", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = uiState.selectedStatus == null,
                onClick = { viewModel.onStatusChange(null) },
                label = { Text("All") }
            )
            TrackingStatus.entries.forEach { status ->
                FilterChip(
                    selected = uiState.selectedStatus == status,
                    onClick = { viewModel.onStatusChange(status) },
                    label = {
                        Text(status.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() })
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        if (visibleItems.isEmpty()) {
            Text(if (uiState.items.isEmpty()) "Your Library is empty. Add movies from their details." else "No movies with this status.")
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            items(visibleItems, key = { it.content.id }) { tracked ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SearchResultCard(tracked.content, onClick = { onMovieClick(tracked.content) })
                    Text(tracked.status.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() })
                }
            }
        }
    }
}
