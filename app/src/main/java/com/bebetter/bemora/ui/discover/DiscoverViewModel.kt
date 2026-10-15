package com.bebetter.bemora.ui.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bebetter.bemora.data.repository.ContentRepository
import com.bebetter.bemora.domain.model.ContentType

class DiscoverViewModel : ViewModel() {
    private val controller = DiscoverController(ContentRepository(), viewModelScope)
    val uiState = controller.uiState
    fun retry(type: ContentType) = controller.load(type)
}
