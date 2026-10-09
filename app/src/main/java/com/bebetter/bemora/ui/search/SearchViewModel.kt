package com.bebetter.bemora.ui.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bebetter.bemora.data.repository.ContentRepository
import com.bebetter.bemora.domain.model.ContentItem
import com.bebetter.bemora.domain.model.ContentType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {

    private val repository = ContentRepository()

    private var searchJob: Job? = null
    private var unfilteredResults: List<ContentItem> = emptyList()

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
            unfilteredResults = emptyList()
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
            selectedType = type,
            results = filteredResults(type)
        )
    }

    private fun filteredResults(type: ContentType?): List<ContentItem> =
        if (type == null) unfilteredResults else unfilteredResults.filter { it.type == type }

    private suspend fun searchMovies(query: String) {

        uiState = uiState.copy(
            isLoading = true,
            errorMessage = null
        )

        try {
            val results =
                repository.searchMovies(query)

            unfilteredResults = results
            uiState = uiState.copy(
                results = filteredResults(uiState.selectedType),
                isLoading = false
            )
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {

            unfilteredResults = emptyList()
            uiState = uiState.copy(
                results = emptyList(),
                isLoading = false,
                errorMessage =
                    exception.message ?: "Unknown error"
            )
        }
    }
}
