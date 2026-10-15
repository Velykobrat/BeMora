package com.bebetter.bemora.ui.discover

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bebetter.bemora.R
import com.bebetter.bemora.domain.model.CatalogId
import com.bebetter.bemora.domain.model.ContentItem
import com.bebetter.bemora.domain.model.ContentType
import com.bebetter.bemora.ui.components.CatalogAttribution
import com.bebetter.bemora.ui.components.ContentCard
import com.bebetter.bemora.ui.components.localizedError

@Composable
fun DiscoverScreen(viewModel: DiscoverViewModel = viewModel(), onContentClick: (ContentItem) -> Unit) {
    val state by viewModel.uiState.collectAsState()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        item(key = "header") {
            Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("BeMora", fontSize = 32.sp, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.find_what_s_next), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        CatalogId.supportedTypes.forEach { type ->
            item(key = type.name) {
                val section = state.sections.getValue(type)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val title = when (type) {
                            ContentType.MOVIE -> R.string.discover_movies
                            ContentType.BOOK -> R.string.discover_books
                            else -> R.string.discover_games
                        }
                        Text(stringResource(title), fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                        if (section.isLoading) CircularProgressIndicator()
                        section.errorMessage?.let { message ->
                            Text(localizedError(message), color = MaterialTheme.colorScheme.error)
                            TextButton(onClick = { viewModel.retry(type) }, enabled = !section.isLoading) {
                                Text(stringResource(R.string.try_again))
                            }
                        }
                        if (!section.isLoading && section.errorMessage == null && section.items.isEmpty()) {
                            Text(stringResource(R.string.no_results_found))
                        }
                        if (section.items.isNotEmpty()) CatalogAttribution(listOf(type))
                    }
                    if (section.items.isNotEmpty()) {
                        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(section.items, key = { it.id }) { item ->
                                ContentCard(item, onClick = { onContentClick(item) })
                            }
                        }
                    }
                }
            }
        }
    }
}
