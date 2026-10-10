package com.bebetter.bemora.ui.library

import com.bebetter.bemora.domain.model.TrackedContentItem
import com.bebetter.bemora.domain.model.TrackingStatus
import com.bebetter.bemora.domain.model.ContentType
import java.util.Locale

enum class LibrarySort(val label: String) {
    ADDED("Added order"),
    TITLE("Title A–Z"),
    YEAR("Newest release"),
    RATING("Catalog rating")
}

data class LibraryUiState(
    val items: List<TrackedContentItem> = emptyList(),
    val selectedStatus: TrackingStatus? = null,
    val query: String = "",
    val sort: LibrarySort = LibrarySort.ADDED,
    val selectedType: ContentType? = null
) {
    val visibleItems: List<TrackedContentItem>
        get() {
            val filtered = items.filter {
                (selectedType == null || it.content.type == selectedType) &&
                (selectedStatus == null || it.status == selectedStatus) &&
                    it.content.title.contains(query.trim(), ignoreCase = true)
            }
            val byTitle = compareBy<TrackedContentItem> {
                it.content.title.lowercase(Locale.ROOT)
            }.thenBy { it.content.id }
            return when (sort) {
                LibrarySort.ADDED -> filtered
                LibrarySort.TITLE -> filtered.sortedWith(byTitle)
                LibrarySort.YEAR -> filtered.sortedWith(
                    compareByDescending<TrackedContentItem> { it.content.releaseYear ?: Int.MIN_VALUE }
                        .then(byTitle)
                )
                LibrarySort.RATING -> filtered.sortedWith(
                    compareByDescending<TrackedContentItem> { it.content.rating ?: Double.NEGATIVE_INFINITY }
                        .then(byTitle)
                )
            }
        }
}
