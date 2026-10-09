package com.bebetter.bemora.ui.details

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bebetter.bemora.data.repository.ContentRepository
import com.bebetter.bemora.navigation.Screen
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class MovieDetailsViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val repository = ContentRepository()

    var uiState by mutableStateOf(MovieDetailsUiState(isLoading = true))
        private set

    init {
        val movieId = Screen.MovieDetails.movieIdOrNull(
            savedStateHandle[Screen.MovieDetails.MOVIE_ID]
        )
        if (movieId == null) {
            uiState = MovieDetailsUiState(errorMessage = "Invalid or missing movie ID")
        } else {
            viewModelScope.launch {
                try {
                    val movie = repository.getMovieDetails(movieId)
                    uiState = MovieDetailsUiState(movie = movie)
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
}
