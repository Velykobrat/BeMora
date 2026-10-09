package com.bebetter.bemora.ui.details

import com.bebetter.bemora.domain.model.ContentItem

data class MovieDetailsUiState(
    val movie: ContentItem? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
