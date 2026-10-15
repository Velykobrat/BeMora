package com.bebetter.bemora.ui.discover

import com.bebetter.bemora.data.repository.DiscoverRepository
import com.bebetter.bemora.data.repository.catalogErrorMessage
import com.bebetter.bemora.domain.model.CatalogId
import com.bebetter.bemora.domain.model.ContentType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DiscoverController(private val repository: DiscoverRepository, private val scope: CoroutineScope) {
    private val state = MutableStateFlow(DiscoverUiState())
    val uiState = state.asStateFlow()
    private val jobs = mutableMapOf<ContentType, Job>()
    private val versions = mutableMapOf<ContentType, Long>()

    init { CatalogId.supportedTypes.forEach(::load) }

    fun load(type: ContentType) {
        require(type in CatalogId.supportedTypes)
        jobs[type]?.cancel()
        val version = (versions[type] ?: 0L) + 1
        versions[type] = version
        state.update { it.copy(sections = it.sections + (type to it.sections.getValue(type).copy(isLoading = true, errorMessage = null))) }
        jobs[type] = scope.launch {
            try {
                val items = repository.getDiscoverItems(type)
                if (versions[type] == version) state.update {
                    it.copy(sections = it.sections + (type to DiscoverSectionState(items, isLoading = false)))
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                if (versions[type] == version) state.update {
                    it.copy(sections = it.sections + (type to it.sections.getValue(type).copy(
                        isLoading = false, errorMessage = catalogErrorMessage(exception))))
                }
            }
        }
    }
}
