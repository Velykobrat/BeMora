package com.bebetter.bemora.ui.details

import com.bebetter.bemora.data.local.LibraryStore
import com.bebetter.bemora.data.repository.CatalogRepository
import com.bebetter.bemora.data.repository.catalogErrorMessage
import com.bebetter.bemora.domain.model.TrackingStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ContentDetailsController(
    private val contentId: String?,
    private val repository: CatalogRepository,
    private val library: LibraryStore,
    private val scope: CoroutineScope
) {
    private val saved = library.items.value.find { it.content.id == contentId }
    private val state = MutableStateFlow(ContentDetailsUiState(
        content = saved?.content, trackingStatus = saved?.status,
        selectedStatus = saved?.status ?: TrackingStatus.PLANNED
    ))
    val uiState = state.asStateFlow()
    private var loadJob: Job? = null
    private var requestVersion = 0L

    init {
        scope.launch {
            library.items.collect { items ->
                val status = items.find { it.content.id == contentId }?.status
                state.update { current ->
                    if (current.trackingStatus == status) current else current.copy(
                        trackingStatus = status, selectedStatus = status ?: TrackingStatus.PLANNED
                    )
                }
            }
        }
        loadDetails()
    }

    fun loadDetails() {
        val id = contentId
        if (id == null) {
            state.value = ContentDetailsUiState(errorMessage = "Invalid or missing content ID")
            return
        }
        if (state.value.isSaving) return
        loadJob?.cancel()
        val version = ++requestVersion
        state.update { it.copy(isLoading = it.content == null,
            isRefreshing = it.content != null, errorMessage = null) }
        loadJob = scope.launch {
            try {
                val content = repository.getContentDetails(id)
                if (version != requestVersion) return@launch
                val status = library.items.value.find { it.content.id == id }?.status
                state.update { current -> current.copy(
                    content = content.copy(
                        subtitle = content.subtitle ?: current.content?.subtitle,
                        releaseYear = content.releaseYear ?: current.content?.releaseYear,
                        imageUrl = content.imageUrl ?: current.content?.imageUrl,
                        description = content.description ?: current.content?.description
                    ),
                    trackingStatus = status,
                    selectedStatus = if (current.content == null) status ?: current.selectedStatus else current.selectedStatus,
                    isLoading = false, isRefreshing = false, errorMessage = null
                ) }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                if (version == requestVersion) state.update { it.copy(isLoading = false,
                    isRefreshing = false, errorMessage = catalogErrorMessage(exception)) }
            }
        }
    }

    fun onStatusChange(status: TrackingStatus) {
        state.update { if (it.isSaving || it.isLoading) it else it.copy(selectedStatus = status, libraryErrorMessage = null) }
    }

    fun saveToLibrary() {
        val current = state.value
        val content = current.content ?: return
        changeLibrary { library.save(content, current.selectedStatus) }
    }

    fun removeFromLibrary() {
        val content = state.value.content ?: return
        changeLibrary { library.remove(content.id) }
    }

    private fun changeLibrary(action: suspend () -> Unit) {
        if (state.value.isSaving || state.value.isLoading) return
        state.update { it.copy(isSaving = true, libraryErrorMessage = null) }
        scope.launch {
            try {
                action()
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                state.update { it.copy(libraryErrorMessage = "Unable to update Library") }
            } finally {
                state.update { it.copy(isSaving = false) }
            }
        }
    }
}
