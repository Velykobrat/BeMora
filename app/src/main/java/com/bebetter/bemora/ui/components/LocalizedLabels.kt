package com.bebetter.bemora.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.bebetter.bemora.R
import com.bebetter.bemora.domain.model.ContentType
import com.bebetter.bemora.domain.model.TrackingStatus
import com.bebetter.bemora.ui.library.LibrarySort

@Composable
fun ContentType.localizedLabel(): String = when (this) {
    ContentType.MOVIE -> stringResource(R.string.movies)
    ContentType.TV_SERIES -> stringResource(R.string.series)
    ContentType.BOOK -> stringResource(R.string.books)
    ContentType.AUDIOBOOK -> stringResource(R.string.audiobooks)
    ContentType.GAME -> stringResource(R.string.games)
    ContentType.PODCAST -> stringResource(R.string.podcasts)
    ContentType.COMIC -> stringResource(R.string.comics)
    ContentType.MANGA -> stringResource(R.string.manga)
}

@Composable
fun TrackingStatus.localizedLabel(): String = when (this) {
    TrackingStatus.PLANNED -> stringResource(R.string.planned)
    TrackingStatus.IN_PROGRESS -> stringResource(R.string.in_progress)
    TrackingStatus.COMPLETED -> stringResource(R.string.completed)
    TrackingStatus.DROPPED -> stringResource(R.string.dropped)
}

@Composable
fun LibrarySort.localizedLabel(): String = when (this) {
    LibrarySort.ADDED -> stringResource(R.string.added_order)
    LibrarySort.TITLE -> stringResource(R.string.title_a_z)
    LibrarySort.YEAR -> stringResource(R.string.newest_release)
    LibrarySort.RATING -> stringResource(R.string.catalog_rating)
}

@Composable
fun localizedError(message: String): String {
    val translations = mapOf(
        "Catalog is not configured." to stringResource(R.string.error_0),
        "Movie catalog is not configured." to stringResource(R.string.error_1),
        "Game catalog is not configured. Add a RAWG API key to enable it." to stringResource(R.string.error_2),
        "Catalog access was denied. Check its API credentials." to stringResource(R.string.error_3),
        "This item is no longer available in the catalog." to stringResource(R.string.error_4),
        "Catalog request limit reached. Please try again later." to stringResource(R.string.error_5),
        "Catalog is temporarily unavailable. Please try again." to stringResource(R.string.error_6),
        "Unable to reach the catalog. Check your connection and try again." to stringResource(R.string.error_7),
        "Unable to load catalog data. Please try again." to stringResource(R.string.error_8),
        "Unable to search catalogs. Please try again." to stringResource(R.string.error_9),
        "Invalid or missing content ID" to stringResource(R.string.error_10),
        "Unable to update Library" to stringResource(R.string.error_11),
        "Unable to load popular movies" to stringResource(R.string.error_12),
    )
    val fallback = stringResource(R.string.error_generic)
    val sources = mapOf("Movies" to ContentType.MOVIE.localizedLabel(), "Books" to ContentType.BOOK.localizedLabel(), "Games" to ContentType.GAME.localizedLabel())
    return message.lines().joinToString("\n") { line ->
        val parts = line.split(": ", limit = 2)
        val body = translations[parts.last()] ?: fallback
        if (parts.size == 1) body else {
            val source = sources[parts.first()] ?: parts.first()
            "$source: $body"
        }
    }
}
