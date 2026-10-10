package com.bebetter.bemora.ui.library

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bebetter.bemora.data.local.LibraryRepository
import com.bebetter.bemora.domain.model.TrackingStatus
import com.bebetter.bemora.domain.model.ContentType
import kotlinx.coroutines.launch

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LibraryRepository.getInstance(application)

    var uiState by mutableStateOf(LibraryUiState(items = repository.items.value))
        private set

    init {
        viewModelScope.launch {
            repository.items.collect { items ->
                uiState = uiState.copy(items = items)
            }
        }
    }

    fun onQueryChange(query: String) {
        uiState = uiState.copy(query = query)
    }

    fun onSortChange(sort: LibrarySort) {
        uiState = uiState.copy(sort = sort)
    }

    fun onTypeChange(type: ContentType?) {
        uiState = uiState.copy(selectedType = type)
    }

    fun onStatusChange(status: TrackingStatus?) {
        uiState = uiState.copy(selectedStatus = status)
    }
}
