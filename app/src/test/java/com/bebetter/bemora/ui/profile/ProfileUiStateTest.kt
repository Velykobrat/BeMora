package com.bebetter.bemora.ui.profile

import com.bebetter.bemora.domain.model.ContentItem
import com.bebetter.bemora.domain.model.ContentType
import com.bebetter.bemora.domain.model.TrackedContentItem
import com.bebetter.bemora.domain.model.TrackingStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileUiStateTest {
    @Test
    fun statisticsCountEachCatalogSeparately() {
        val types = listOf(ContentType.MOVIE, ContentType.BOOK, ContentType.GAME)
        val state = ProfileUiState(types.map { type ->
            TrackedContentItem(ContentItem(type.name, "Item", type = type), TrackingStatus.COMPLETED)
        })
        assertEquals(3, state.total)
        types.forEach { assertEquals(1, state.typeCounts[it]) }
        assertEquals(100, state.completedPercentage)
    }

    @Test
    fun emptyLibraryHasZeroCountsAndPercentage() {
        val state = ProfileUiState()
        assertEquals(0, state.total)
        assertEquals(0, state.completedPercentage)
        TrackingStatus.entries.forEach { assertEquals(0, state.counts[it]) }
    }

    @Test
    fun statisticsFollowStatusChangesAndRemoval() {
        val items = TrackingStatus.entries.mapIndexed { index, status ->
            TrackedContentItem(
                ContentItem(id = "tmdb_movie_${index + 1}", title = "Movie",
                    type = ContentType.MOVIE), status
            )
        }
        val state = ProfileUiState(items)
        assertEquals(4, state.total)
        assertEquals(25, state.completedPercentage)
        TrackingStatus.entries.forEach { assertEquals(1, state.counts[it]) }
        val changed = state.copy(items = items.map { it.copy(status = TrackingStatus.COMPLETED) })
        assertEquals(100, changed.completedPercentage)
        assertEquals(4, changed.counts[TrackingStatus.COMPLETED])
        assertEquals(3, changed.copy(items = changed.items.drop(1)).total)
    }
}
