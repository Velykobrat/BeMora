package com.bebetter.bemora.ui.search

import com.bebetter.bemora.domain.model.ContentItem
import com.bebetter.bemora.domain.model.ContentType

data class SearchUiState(
    val query: String = "",
    val selectedType: ContentType? = null,
    val results: List<ContentItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)