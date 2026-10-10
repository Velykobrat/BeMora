package com.bebetter.bemora.ui.profile

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bebetter.bemora.data.local.LibraryRepository
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = LibraryRepository.getInstance(application)
    var uiState by mutableStateOf(ProfileUiState(repository.items.value))
        private set

    init {
        viewModelScope.launch {
            repository.items.collect { uiState = ProfileUiState(it) }
        }
    }
}
