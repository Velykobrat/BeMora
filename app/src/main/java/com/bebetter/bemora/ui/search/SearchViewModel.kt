package com.bebetter.bemora.ui.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bebetter.bemora.data.repository.CatalogRepository
import com.bebetter.bemora.data.repository.ContentRepository
import com.bebetter.bemora.domain.model.CatalogId
import com.bebetter.bemora.domain.model.ContentType
import com.bebetter.bemora.domain.model.label
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchViewModel(private val repository: CatalogRepository = ContentRepository()) : ViewModel() {
    private var searchJob: Job? = null
    private var requestVersion = 0L
    var uiState by mutableStateOf(SearchUiState())
        private set

    fun onQueryChange(query: String) {
        uiState = uiState.copy(query = query)
        startSearch(debounce = true)
    }

    fun onTypeChange(type: ContentType?) {
        require(type == null || type in CatalogId.supportedTypes)
        if (uiState.selectedType == type) return
        uiState = uiState.copy(selectedType = type)
        startSearch(debounce = false)
    }

    fun retry() = startSearch(debounce = false)

    private fun startSearch(debounce: Boolean) {
        searchJob?.cancel()
        val version = ++requestVersion
        val query = uiState.query.trim()
        val type = uiState.selectedType
        uiState = uiState.copy(results = emptyList(), errorMessage = null, isLoading = query.isNotBlank())
        if (query.isBlank()) return
        searchJob = viewModelScope.launch {
            if (debounce) delay(500)
            try {
                val result = repository.search(query, type)
                if (version == requestVersion) {
                    uiState = uiState.copy(results = result.items, isLoading = false,
                        errorMessage = result.errors.entries.joinToString("\n") {
                            it.key.label + ": " + it.value
                        }.takeIf { it.isNotBlank() })
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                if (version == requestVersion) uiState = uiState.copy(isLoading = false,
                    errorMessage = "Unable to search catalogs. Please try again.")
            }
        }
    }
}
