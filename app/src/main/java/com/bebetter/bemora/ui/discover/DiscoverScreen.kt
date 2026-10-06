package com.bebetter.bemora.ui.discover

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bebetter.bemora.domain.model.ContentItem
import com.bebetter.bemora.domain.model.ContentType
import com.bebetter.bemora.ui.components.ContentCard

@Composable
fun DiscoverScreen() {

    val contentItems = listOf(
        ContentItem(
            id = "movie_dune_2",
            title = "Dune: Part Two",
            imageUrl = "https://image.tmdb.org/t/p/w500/1pdfLvkbY9ohJlCjQH2CZjjYVvJ.jpg",
            releaseYear = 2024,
            rating = 8.5,
            type = ContentType.MOVIE
        ),
        ContentItem(
            id = "movie_oppenheimer",
            title = "Oppenheimer",
            imageUrl = "https://image.tmdb.org/t/p/w500/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg",
            releaseYear = 2023,
            rating = 8.6,
            type = ContentType.MOVIE
        ),
        ContentItem(
            id = "movie_batman",
            title = "The Batman",
            imageUrl = "https://image.tmdb.org/t/p/w500/74xTEgt7R36Fpooo50r9T25onhq.jpg",
            releaseYear = 2022,
            rating = 7.8,
            type = ContentType.MOVIE
        ),
        ContentItem(
            id = "movie_interstellar",
            title = "Interstellar",
            imageUrl = "https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg",
            releaseYear = 2014,
            rating = 8.7,
            type = ContentType.MOVIE
        )
    )

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
            text = "Find what's next.",
            fontSize = 16.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Trending now",
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(contentItems) { item ->
                ContentCard(item)
            }
        }
    }
}