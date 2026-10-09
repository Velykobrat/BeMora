package com.bebetter.bemora.ui.discover

import com.bebetter.bemora.domain.model.ContentItem

data class DiscoverUiState(
    val movies: List<ContentItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
