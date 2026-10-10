package com.bebetter.bemora.ui.profile

import com.bebetter.bemora.domain.model.TrackedContentItem
import com.bebetter.bemora.domain.model.TrackingStatus

data class ProfileUiState(val items: List<TrackedContentItem> = emptyList()) {
    val total: Int get() = items.size
    val counts: Map<TrackingStatus, Int>
        get() = TrackingStatus.entries.associateWith { status -> items.count { it.status == status } }
    val completedPercentage: Int
        get() = if (total == 0) 0 else
            (items.count { it.status == TrackingStatus.COMPLETED }.toLong() * 100 / total).toInt()
}
