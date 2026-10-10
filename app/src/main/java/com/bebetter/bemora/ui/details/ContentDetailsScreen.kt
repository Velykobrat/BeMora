package com.bebetter.bemora.ui.details

import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import com.bebetter.bemora.ui.components.localizedError
import com.bebetter.bemora.R
import com.bebetter.bemora.ui.components.localizedLabel
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
import com.bebetter.bemora.domain.model.ContentType
import com.bebetter.bemora.ui.components.CatalogAttribution
import com.bebetter.bemora.domain.model.TrackingStatus

@Composable
fun ContentDetailsScreen(
    onBack: () -> Unit,
    viewModel: ContentDetailsViewModel = viewModel()
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
            Text(stringResource(R.string.back))
        }

        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        uiState.errorMessage?.let { message ->
            Text(text = localizedError(message), color = MaterialTheme.colorScheme.error)
        }

        if (uiState.errorMessage != null) {
            TextButton(onClick = viewModel::loadDetails, enabled = !uiState.isLoading && !uiState.isSaving) {
                Text(stringResource(R.string.try_again))
            }
        }

        uiState.content?.let { content ->
            AsyncImage(
            placeholder = painterResource(R.drawable.cover_placeholder),
            error = painterResource(R.drawable.cover_placeholder),
            fallback = painterResource(R.drawable.cover_placeholder),
                model = content.imageUrl,
                contentDescription = content.title,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
            Text(
                text = content.title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            content.subtitle?.let { Text(it) }
            Text(stringResource(R.string.release_year_1_s, content.releaseYear?.toString() ?: "—"))
            if (content.type != ContentType.BOOK) {
                val source = if (content.type == ContentType.GAME) "RAWG" else "TMDB"
                Text(source + " · " + (content.rating?.let { "%.1f / 10".format(it) } ?: "—"))
            }
            CatalogAttribution(listOf(content.type))
            Text(content.type.localizedLabel())
            Text(content.description?.takeIf { it.isNotBlank() } ?: stringResource(R.string.no_description_available))

            Text(
                text = uiState.trackingStatus?.let {
                    it.localizedLabel()
                } ?: stringResource(R.string.not_in_library)
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TrackingStatus.entries.forEach { status ->
                    FilterChip(
                        selected = uiState.selectedStatus == status,
                        onClick = { viewModel.onStatusChange(status) },
                        enabled = !uiState.isSaving && !uiState.isLoading,
                        label = {
                            Text(status.localizedLabel())
                        }
                    )
                }
            }
            Button(onClick = viewModel::saveToLibrary, enabled = !uiState.isSaving && !uiState.isLoading) {
                Text(if (uiState.isSaving) stringResource(R.string.saving) else if (uiState.trackingStatus == null) stringResource(R.string.add_to_library) else stringResource(R.string.save_status))
            }
            if (uiState.trackingStatus != null) {
                TextButton(onClick = viewModel::removeFromLibrary, enabled = !uiState.isSaving && !uiState.isLoading) {
                    Text(stringResource(R.string.remove_from_library))
                }
            }
            uiState.libraryErrorMessage?.let { message ->
                Text(text = localizedError(message), color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
