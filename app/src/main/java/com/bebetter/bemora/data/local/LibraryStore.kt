package com.bebetter.bemora.data.local

import com.bebetter.bemora.domain.model.ContentItem
import com.bebetter.bemora.domain.model.TrackedContentItem
import com.bebetter.bemora.domain.model.TrackingStatus
import kotlinx.coroutines.flow.StateFlow

interface LibraryStore {
    val items: StateFlow<List<TrackedContentItem>>
    suspend fun save(content: ContentItem, status: TrackingStatus)
    suspend fun remove(contentId: String)
}
