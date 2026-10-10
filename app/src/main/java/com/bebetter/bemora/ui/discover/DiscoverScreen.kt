package com.bebetter.bemora.ui.discover

import androidx.compose.ui.res.stringResource
import com.bebetter.bemora.ui.components.localizedError
import com.bebetter.bemora.R
import com.bebetter.bemora.ui.components.localizedLabel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bebetter.bemora.domain.model.ContentItem
import com.bebetter.bemora.ui.components.ContentCard

@Composable
fun DiscoverScreen(
    viewModel: DiscoverViewModel = viewModel(),
    onMovieClick: (ContentItem) -> Unit
) {
    val uiState = viewModel.uiState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "BeMora",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = stringResource(R.string.find_what_s_next),
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.trending_now),
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        uiState.errorMessage?.let { message ->
            Text(
                text = localizedError(message),
                color = MaterialTheme.colorScheme.error
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(uiState.movies) { item ->
                ContentCard(item, onClick = { onMovieClick(item) })
            }
        }
    }
}
