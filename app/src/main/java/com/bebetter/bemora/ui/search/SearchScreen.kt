package com.bebetter.bemora.ui.search

import androidx.compose.ui.res.stringResource
import com.bebetter.bemora.ui.components.localizedError
import com.bebetter.bemora.R
import com.bebetter.bemora.ui.components.localizedLabel
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.bebetter.bemora.ui.components.CatalogAttribution
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bebetter.bemora.domain.model.ContentItem
import com.bebetter.bemora.domain.model.ContentType
import com.bebetter.bemora.ui.components.SearchResultCard

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = viewModel(),
    onContentClick: (ContentItem) -> Unit
) {
    val uiState = viewModel.uiState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Text(
            text = stringResource(R.string.search),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        OutlinedTextField(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
            value = uiState.query,
            onValueChange = { query ->
                viewModel.onQueryChange(query)
            },
            label = {
                Text(stringResource(R.string.search_movies_books_games))
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = uiState.selectedType == null,
                onClick = {
                    viewModel.onTypeChange(null)
                },
                label = {
                    Text(stringResource(R.string.all))
                }
            )

            FilterChip(
                selected = uiState.selectedType == ContentType.MOVIE,
                onClick = {
                    viewModel.onTypeChange(ContentType.MOVIE)
                },
                label = {
                    Text(stringResource(R.string.movies))
                }
            )

            FilterChip(
                selected = uiState.selectedType == ContentType.BOOK,
                onClick = {
                    viewModel.onTypeChange(ContentType.BOOK)
                },
                label = {
                    Text(stringResource(R.string.books))
                }
            )

            FilterChip(
                selected = uiState.selectedType == ContentType.GAME,
                onClick = {
                    viewModel.onTypeChange(ContentType.GAME)
                },
                label = {
                    Text(stringResource(R.string.games))
                }
            )
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 16.dp)
            )
        }

        uiState.errorMessage?.let { message ->
            Text(
                text = localizedError(message),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        if (uiState.errorMessage != null) {
            TextButton(onClick = viewModel::retry) { Text(stringResource(R.string.try_again)) }
        }
        CatalogAttribution(uiState.results.map { it.type }.distinct())

        if (
            uiState.query.isNotBlank() &&
            !uiState.isLoading &&
            uiState.errorMessage == null &&
            uiState.results.isEmpty()
        ) {
            Text(
                text = stringResource(R.string.no_results_found),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(uiState.results, key = { it.id }) { item ->
                SearchResultCard(item, onClick = { onContentClick(item) })
            }
        }
    }
}
