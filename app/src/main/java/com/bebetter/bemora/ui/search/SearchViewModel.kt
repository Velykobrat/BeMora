package com.bebetter.bemora.ui.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bebetter.bemora.data.repository.ContentRepository
import com.bebetter.bemora.domain.model.ContentType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {

    private val repository = ContentRepository()

    private var searchJob: Job? = null

    var uiState by mutableStateOf(
        SearchUiState()
    )
        private set

    fun onQueryChange(query: String) {

        uiState = uiState.copy(
            query = query
        )

        searchJob?.cancel()

        if (query.isBlank()) {
            uiState = uiState.copy(
                results = emptyList(),
                isLoading = false,
                errorMessage = null
            )

            return
        }

        searchJob = viewModelScope.launch {

            delay(500)

            searchMovies(query)
        }
    }

    fun onTypeChange(type: ContentType?) {
        uiState = uiState.copy(
            selectedType = type
        )
    }

    private suspend fun searchMovies(query: String) {

        uiState = uiState.copy(
            isLoading = true,
            errorMessage = null
        )

        try {
            val results =
                repository.searchMovies(query)

            uiState = uiState.copy(
                results = results,
                isLoading = false
            )
        } catch (exception: Exception) {

            uiState = uiState.copy(
                results = emptyList(),
                isLoading = false,
                errorMessage =
                    exception.message ?: "Unknown error"
            )
        }
    }
}