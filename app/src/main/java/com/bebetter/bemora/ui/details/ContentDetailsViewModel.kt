package com.bebetter.bemora.ui.details

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.bebetter.bemora.data.local.LibraryRepository
import com.bebetter.bemora.data.repository.ContentRepository
import com.bebetter.bemora.domain.model.TrackingStatus
import com.bebetter.bemora.navigation.Screen

class ContentDetailsViewModel(application: Application, savedStateHandle: SavedStateHandle) : AndroidViewModel(application) {
    private val contentId = Screen.ContentDetails.idOrNull(savedStateHandle[Screen.ContentDetails.CONTENT_ID])
        ?: Screen.MovieDetails.movieIdOrNull(savedStateHandle[Screen.MovieDetails.MOVIE_ID])?.let { "tmdb_movie_" + it }
    private val controller = ContentDetailsController(contentId, ContentRepository(),
        LibraryRepository.getInstance(application), viewModelScope)
    val uiState = controller.uiState

    fun loadDetails() = controller.loadDetails()
    fun onStatusChange(status: TrackingStatus) = controller.onStatusChange(status)
    fun saveToLibrary() = controller.saveToLibrary()
    fun removeFromLibrary() = controller.removeFromLibrary()
}
