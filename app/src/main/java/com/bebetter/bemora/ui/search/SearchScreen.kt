package com.bebetter.bemora.ui.search

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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bebetter.bemora.domain.model.ContentType
import com.bebetter.bemora.ui.components.SearchResultCard

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = viewModel()
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
            text = "Search",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        OutlinedTextField(
            value = uiState.query,
            onValueChange = { query ->
                viewModel.onQueryChange(query)
            },
            label = {
                Text("Search movies, books, games...")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = uiState.selectedType == null,
                onClick = {
                    viewModel.onTypeChange(null)
                },
                label = {
                    Text("All")
                }
            )

            FilterChip(
                selected = uiState.selectedType == ContentType.MOVIE,
                onClick = {
                    viewModel.onTypeChange(ContentType.MOVIE)
                },
                label = {
                    Text("Movies")
                }
            )

            FilterChip(
                selected = uiState.selectedType == ContentType.BOOK,
                onClick = {
                    viewModel.onTypeChange(ContentType.BOOK)
                },
                label = {
                    Text("Books")
                }
            )

            FilterChip(
                selected = uiState.selectedType == ContentType.GAME,
                onClick = {
                    viewModel.onTypeChange(ContentType.GAME)
                },
                label = {
                    Text("Games")
                }
            )
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(uiState.results) { item ->
                SearchResultCard(item)
            }
        }
    }
}