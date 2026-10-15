package com.bebetter.bemora.ui.discover

import com.bebetter.bemora.domain.model.CatalogId
import com.bebetter.bemora.domain.model.ContentItem
import com.bebetter.bemora.domain.model.ContentType

data class DiscoverSectionState(
    val items: List<ContentItem> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

data class DiscoverUiState(
    val sections: Map<ContentType, DiscoverSectionState> = CatalogId.supportedTypes.associateWith { DiscoverSectionState() }
)
