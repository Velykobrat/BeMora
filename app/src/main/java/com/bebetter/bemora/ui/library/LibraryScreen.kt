package com.bebetter.bemora.ui.library

import androidx.compose.ui.res.stringResource
import com.bebetter.bemora.R
import com.bebetter.bemora.ui.components.localizedLabel
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bebetter.bemora.domain.model.CatalogId
import com.bebetter.bemora.domain.model.label
import com.bebetter.bemora.ui.components.CatalogAttribution
import com.bebetter.bemora.domain.model.ContentItem
import com.bebetter.bemora.domain.model.TrackingStatus
import com.bebetter.bemora.ui.components.SearchResultCard

@Composable
fun LibraryScreen(
    onContentClick: (ContentItem) -> Unit,
    viewModel: LibraryViewModel = viewModel()
) {
    val uiState = viewModel.uiState
    val visibleItems = uiState.visibleItems

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(stringResource(R.string.library), fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(selected = uiState.selectedType == null,
                onClick = { viewModel.onTypeChange(null) }, label = { Text(stringResource(R.string.all_types)) })
            CatalogId.supportedTypes.forEach { type ->
                FilterChip(selected = uiState.selectedType == type,
                    onClick = { viewModel.onTypeChange(type) }, label = { Text(type.localizedLabel()) })
            }
        }
        OutlinedTextField(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
            value = uiState.query,
            onValueChange = viewModel::onQueryChange,
            label = { Text(stringResource(R.string.search_your_library)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LibrarySort.entries.forEach { sort ->
                FilterChip(
                    selected = uiState.sort == sort,
                    onClick = { viewModel.onSortChange(sort) },
                    label = { Text(sort.localizedLabel()) }
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = uiState.selectedStatus == null,
                onClick = { viewModel.onStatusChange(null) },
                label = { Text(stringResource(R.string.all)) }
            )
            TrackingStatus.entries.forEach { status ->
                FilterChip(
                    selected = uiState.selectedStatus == status,
                    onClick = { viewModel.onStatusChange(status) },
                    label = {
                        Text(status.localizedLabel())
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        if (visibleItems.isEmpty()) {
            Text(
                when {
                    uiState.items.isEmpty() -> stringResource(R.string.your_library_is_empty_add_movies_books_or_games_from_search)
                    uiState.query.isNotBlank() -> stringResource(R.string.no_items_match_your_search_and_filters)
                    else -> stringResource(R.string.no_items_match_your_filters)
                }
            )
        }

        CatalogAttribution(visibleItems.map { it.content.type }.distinct())

        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            items(visibleItems, key = { it.content.id }) { tracked ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SearchResultCard(tracked.content, onClick = { onContentClick(tracked.content) })
                    Text(tracked.status.localizedLabel())
                }
            }
        }
    }
}
