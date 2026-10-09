package com.bebetter.bemora.ui.discover

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bebetter.bemora.data.repository.ContentRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class DiscoverViewModel : ViewModel() {

    private val repository = ContentRepository()

    var uiState by mutableStateOf(DiscoverUiState(isLoading = true))
        private set

    init {
        viewModelScope.launch {
            try {
                val movies = repository.getPopularMovies()
                uiState = uiState.copy(
                    movies = movies,
                    isLoading = false
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "Unable to load popular movies"
                )
            }
        }
    }
}
