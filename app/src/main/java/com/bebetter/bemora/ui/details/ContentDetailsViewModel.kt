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
import com.bebetter.bemora.data.repository.catalogErrorMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class ContentDetailsViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val repository = ContentRepository()
    private val libraryRepository = LibraryRepository.getInstance(application)

    var uiState by mutableStateOf(ContentDetailsUiState(isLoading = true))
        private set

    private val contentId: String? = Screen.ContentDetails.idOrNull(
        savedStateHandle[Screen.ContentDetails.CONTENT_ID]
    ) ?: Screen.MovieDetails.movieIdOrNull(
        savedStateHandle[Screen.MovieDetails.MOVIE_ID]
    )?.let { "tmdb_movie_" + it }

    private var loadJob: kotlinx.coroutines.Job? = null

    init {
        viewModelScope.launch {
            libraryRepository.items.collect { items ->
                val content = uiState.content ?: return@collect
                val status = items.find { it.content.id == content.id }?.status
                if (status != uiState.trackingStatus) {
                    uiState = uiState.copy(
                        trackingStatus = status,
                        selectedStatus = status ?: TrackingStatus.PLANNED
                    )
                }
            }
        }

        loadDetails()
    }

    fun loadDetails() {
        val id = contentId
        if (id == null) {
            uiState = ContentDetailsUiState(errorMessage = "Invalid or missing content ID")
            return
        }
        if (uiState.isSaving) return
        loadJob?.cancel()
        uiState = uiState.copy(isLoading = true, errorMessage = null)
        loadJob = viewModelScope.launch {
            try {
                val content = repository.getContentDetails(id)
                val status = libraryRepository.items.value.find { it.content.id == id }?.status
                uiState = ContentDetailsUiState(content = content, trackingStatus = status,
                    selectedStatus = status ?: TrackingStatus.PLANNED)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                val saved = libraryRepository.items.value.find { it.content.id == id }
                uiState = ContentDetailsUiState(content = saved?.content,
                    trackingStatus = saved?.status, selectedStatus = saved?.status ?: TrackingStatus.PLANNED,
                    errorMessage = catalogErrorMessage(exception))
            }
        }
    }

    fun onStatusChange(status: TrackingStatus) {
        if (!uiState.isSaving && !uiState.isLoading) {
            uiState = uiState.copy(selectedStatus = status, libraryErrorMessage = null)
        }
    }

    fun saveToLibrary() {
        val content = uiState.content ?: return
        val status = uiState.selectedStatus
        changeLibrary {
            libraryRepository.save(content, status)
            uiState = uiState.copy(trackingStatus = status, selectedStatus = status)
        }
    }

    fun removeFromLibrary() {
        val content = uiState.content ?: return
        changeLibrary {
            libraryRepository.remove(content.id)
            uiState = uiState.copy(trackingStatus = null, selectedStatus = TrackingStatus.PLANNED)
        }
    }

    private fun changeLibrary(action: suspend () -> Unit) {
        if (uiState.isSaving || uiState.isLoading) return
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
