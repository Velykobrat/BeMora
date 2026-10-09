package com.bebetter.bemora.ui.details

import com.bebetter.bemora.domain.model.ContentItem
import com.bebetter.bemora.domain.model.TrackingStatus

data class MovieDetailsUiState(
    val movie: ContentItem? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val trackingStatus: TrackingStatus? = null,
    val selectedStatus: TrackingStatus = TrackingStatus.PLANNED,
    val isSaving: Boolean = false,
    val libraryErrorMessage: String? = null
)
