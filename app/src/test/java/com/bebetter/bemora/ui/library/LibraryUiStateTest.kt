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
}
