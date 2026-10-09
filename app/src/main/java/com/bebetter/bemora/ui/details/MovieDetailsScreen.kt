package com.bebetter.bemora.ui.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.bebetter.bemora.domain.model.TrackingStatus

@Composable
fun MovieDetailsScreen(
    onBack: () -> Unit,
    viewModel: MovieDetailsViewModel = viewModel()
) {
    val uiState = viewModel.uiState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("Back")
        }

        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        uiState.errorMessage?.let { message ->
            Text(text = message, color = MaterialTheme.colorScheme.error)
        }

        uiState.movie?.let { movie ->
            AsyncImage(
                model = movie.imageUrl,
                contentDescription = "Poster for ${movie.title}",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
            Text(
                text = movie.title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text("Release year: ${movie.releaseYear ?: "Unknown"}")
            Text("Rating: ${movie.rating ?: "Not rated"}")
            Text("Type: ${movie.type.name.lowercase().replaceFirstChar { it.uppercase() }}")
            Text(movie.description?.takeIf { it.isNotBlank() } ?: "No description available")

            Text(
                text = uiState.trackingStatus?.let {
                    "Library status: ${it.name.lowercase().replace('_', ' ').replaceFirstChar { character -> character.uppercase() }}"
                } ?: "Not in Library"
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TrackingStatus.entries.forEach { status ->
                    FilterChip(
                        selected = uiState.selectedStatus == status,
                        onClick = { viewModel.onStatusChange(status) },
                        enabled = !uiState.isSaving,
                        label = {
                            Text(status.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() })
                        }
                    )
                }
            }
            Button(onClick = viewModel::saveToLibrary, enabled = !uiState.isSaving) {
                Text(if (uiState.isSaving) "Saving..." else if (uiState.trackingStatus == null) "Add to Library" else "Save status")
            }
            if (uiState.trackingStatus != null) {
                TextButton(onClick = viewModel::removeFromLibrary, enabled = !uiState.isSaving) {
                    Text("Remove from Library")
                }
            }
            uiState.libraryErrorMessage?.let { message ->
                Text(text = message, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
