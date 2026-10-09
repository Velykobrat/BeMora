package com.bebetter.bemora.ui.library

import com.bebetter.bemora.domain.model.TrackedContentItem
import com.bebetter.bemora.domain.model.TrackingStatus

data class LibraryUiState(
    val items: List<TrackedContentItem> = emptyList(),
    val selectedStatus: TrackingStatus? = null
) {
    val visibleItems: List<TrackedContentItem>
        get() = if (selectedStatus == null) items else items.filter { it.status == selectedStatus }
}
