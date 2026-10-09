package com.bebetter.bemora.ui.details

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bebetter.bemora.data.repository.ContentRepository
import com.bebetter.bemora.data.local.LibraryRepository
import com.bebetter.bemora.domain.model.TrackingStatus
import com.bebetter.bemora.navigation.Screen
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class MovieDetailsViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val repository = ContentRepository()
    private val libraryRepository = LibraryRepository.getInstance(application)

    var uiState by mutableStateOf(MovieDetailsUiState(isLoading = true))
        private set

    init {
        viewModelScope.launch {
            libraryRepository.items.collect { items ->
                val movie = uiState.movie ?: return@collect
                val status = items.find { it.content.id == movie.id }?.status
                if (status != uiState.trackingStatus) {
                    uiState = uiState.copy(
                        trackingStatus = status,
                        selectedStatus = status ?: TrackingStatus.PLANNED
                    )
                }
            }
        }

        val movieId = Screen.MovieDetails.movieIdOrNull(
            savedStateHandle[Screen.MovieDetails.MOVIE_ID]
        )
        if (movieId == null) {
            uiState = MovieDetailsUiState(errorMessage = "Invalid or missing movie ID")
        } else {
            viewModelScope.launch {
                try {
                    val movie = repository.getMovieDetails(movieId)
                    val status = libraryRepository.items.value.find { it.content.id == movie.id }?.status
                    uiState = MovieDetailsUiState(
                        movie = movie,
                        trackingStatus = status,
                        selectedStatus = status ?: TrackingStatus.PLANNED
                    )
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    uiState = MovieDetailsUiState(
                        errorMessage = exception.message ?: "Unable to load movie details"
                    )
                }
            }
        }
    }

    fun onStatusChange(status: TrackingStatus) {
        if (!uiState.isSaving) {
            uiState = uiState.copy(selectedStatus = status, libraryErrorMessage = null)
        }
    }

    fun saveToLibrary() {
        val movie = uiState.movie ?: return
        val status = uiState.selectedStatus
        changeLibrary {
            libraryRepository.save(movie, status)
            uiState = uiState.copy(trackingStatus = status, selectedStatus = status)
        }
    }

    fun removeFromLibrary() {
        val movie = uiState.movie ?: return
        changeLibrary {
            libraryRepository.remove(movie.id)
            uiState = uiState.copy(trackingStatus = null, selectedStatus = TrackingStatus.PLANNED)
        }
    }

    private fun changeLibrary(action: suspend () -> Unit) {
        if (uiState.isSaving) return
        uiState = uiState.copy(isSaving = true, libraryErrorMessage = null)
        viewModelScope.launch {
            try {
                action()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                uiState = uiState.copy(
                    libraryErrorMessage = exception.message ?: "Unable to update Library"
                )
            } finally {
                uiState = uiState.copy(isSaving = false)
            }
        }
    }
}
