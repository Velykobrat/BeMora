package com.bebetter.bemora.ui.library

import com.bebetter.bemora.domain.model.ContentItem
import com.bebetter.bemora.domain.model.ContentType
import com.bebetter.bemora.domain.model.TrackedContentItem
import com.bebetter.bemora.domain.model.TrackingStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class LibraryUiStateTest {
    @Test
    fun filtersShowMatchingStatusesAndAllRestoresEveryMovie() {
        val items = TrackingStatus.entries.mapIndexed { index, status ->
            TrackedContentItem(
                ContentItem(id = "tmdb_movie_${index + 1}", title = "Movie $index", type = ContentType.MOVIE),
                status
            )
        }
        var state = LibraryUiState(items = items)
        assertEquals(items, state.visibleItems)
        for (status in TrackingStatus.entries) {
            state = state.copy(selectedStatus = status)
            assertEquals(listOf(items.single { it.status == status }), state.visibleItems)
        }
        assertEquals(items, state.copy(selectedStatus = null).visibleItems)
        assertEquals(emptyList<TrackedContentItem>(), state.copy(items = emptyList()).visibleItems)
    }
    @Test
    fun searchCombinesWithStatusAndTrimsWhitespace() {
        val items = listOf(
            movie("1", "Arrival", TrackingStatus.COMPLETED),
            movie("2", "Arrival Again", TrackingStatus.PLANNED),
            movie("3", "Dune", TrackingStatus.COMPLETED)
        )
        val state = LibraryUiState(items, TrackingStatus.COMPLETED, "  ARRIVAL  ")
        assertEquals(listOf(items[0]), state.visibleItems)
        assertEquals(emptyList<TrackedContentItem>(), state.copy(query = "missing").visibleItems)
    }

    @Test
    fun sortingUsesTitleTieBreakersAndPlacesMissingValuesLast() {
        val items = listOf(
            movie("1", "Zulu", year = null, rating = null),
            movie("2", "beta", year = 2024, rating = 8.0),
            movie("3", "Alpha", year = 2024, rating = 8.0),
            movie("4", "Delta", year = 2020, rating = 6.0)
        )
        val state = LibraryUiState(items)
        assertEquals(listOf(items[2], items[1], items[3], items[0]),
            state.copy(sort = LibrarySort.TITLE).visibleItems)
        assertEquals(listOf(items[2], items[1], items[3], items[0]),
            state.copy(sort = LibrarySort.YEAR).visibleItems)
        assertEquals(listOf(items[2], items[1], items[3], items[0]),
            state.copy(sort = LibrarySort.RATING).visibleItems)
        assertEquals(items, state.visibleItems)
    }

    @Test
    fun typeFiltersCombineWithQueryAndStatus() {
        val book = TrackedContentItem(ContentItem("openlibrary_book_OL1W", "Dune", type = ContentType.BOOK), TrackingStatus.COMPLETED)
        val game = TrackedContentItem(ContentItem("rawg_game_1", "Dune", type = ContentType.GAME), TrackingStatus.COMPLETED)
        val state = LibraryUiState(listOf(book, game), TrackingStatus.COMPLETED, "dune", selectedType = ContentType.BOOK)
        assertEquals(listOf(book), state.visibleItems)
        assertEquals(emptyList<TrackedContentItem>(), state.copy(selectedStatus = TrackingStatus.PLANNED).visibleItems)
        assertEquals(listOf(book, game), state.copy(selectedType = null).visibleItems)
    }

    private fun movie(
        id: String,
        title: String,
        status: TrackingStatus = TrackingStatus.PLANNED,
        year: Int? = null,
        rating: Double? = null
    ) = TrackedContentItem(
        ContentItem(id = "tmdb_movie_${id}", title = title, type = ContentType.MOVIE,
            releaseYear = year, rating = rating),
        status
    )
}
