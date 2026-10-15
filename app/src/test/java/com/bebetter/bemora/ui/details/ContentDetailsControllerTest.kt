package com.bebetter.bemora.ui.details

import com.bebetter.bemora.data.local.LibraryStore
import com.bebetter.bemora.data.repository.*
import com.bebetter.bemora.domain.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class ContentDetailsControllerTest {
    private val cached=ContentItem("openlibrary_book_OL1W","Saved book",subtitle="Saved author",releaseYear=1990,type=ContentType.BOOK)
    private class MemoryLibrary(initial: List<TrackedContentItem>) : LibraryStore {
        override val items=MutableStateFlow(initial)
        override suspend fun save(content: ContentItem,status: TrackingStatus) {
            items.value=items.value.filterNot { it.content.id==content.id }+TrackedContentItem(content,status)
        }
        override suspend fun remove(contentId: String) { items.value=items.value.filterNot { it.content.id==contentId } }
    }
    private fun repository(load: suspend () -> ContentItem) = object : CatalogRepository {
        override suspend fun search(query: String,type: ContentType?) = CatalogSearchResult()
        override suspend fun getContentDetails(id: String) = load()
    }
    private fun scope()=CoroutineScope(SupervisorJob()+Dispatchers.Unconfined)

    @Test fun savedContentAndStatusEditingDoNotWaitForTheNetwork() = runBlocking {
        val scope=scope()
        val gate=CompletableDeferred<ContentItem>()
        val library=MemoryLibrary(listOf(TrackedContentItem(cached,TrackingStatus.PLANNED)))
        try {
            val controller=ContentDetailsController(cached.id,repository { gate.await() },library,scope)
            assertEquals(cached,controller.uiState.value.content)
            assertTrue(controller.uiState.value.isRefreshing)
            assertFalse(controller.uiState.value.isLoading)
            controller.onStatusChange(TrackingStatus.COMPLETED)
            controller.saveToLibrary()
            assertEquals(TrackingStatus.COMPLETED,library.items.value.single().status)
            gate.complete(cached.copy(title="Fresh title",subtitle=null,releaseYear=null))
            assertEquals("Fresh title",controller.uiState.value.content!!.title)
            assertEquals("Saved author",controller.uiState.value.content!!.subtitle)
            assertEquals(1990,controller.uiState.value.content!!.releaseYear)
            assertEquals(TrackingStatus.COMPLETED,controller.uiState.value.selectedStatus)
            assertFalse(controller.uiState.value.isRefreshing)
        } finally { scope.cancel() }
    }

    @Test fun refreshDoesNotOverwriteAnUnsavedStatusChoice() = runBlocking {
        val scope=scope()
        val gate=CompletableDeferred<ContentItem>()
        try {
            val controller=ContentDetailsController(cached.id,repository { gate.await() },
                MemoryLibrary(listOf(TrackedContentItem(cached,TrackingStatus.PLANNED))),scope)
            controller.onStatusChange(TrackingStatus.DROPPED)
            gate.complete(cached)
            assertEquals(TrackingStatus.DROPPED,controller.uiState.value.selectedStatus)
            assertEquals(TrackingStatus.PLANNED,controller.uiState.value.trackingStatus)
        } finally { scope.cancel() }
    }

    @Test fun removingWhileRefreshingDoesNotReAddTheItem() = runBlocking {
        val scope=scope()
        val gate=CompletableDeferred<ContentItem>()
        val library=MemoryLibrary(listOf(TrackedContentItem(cached,TrackingStatus.PLANNED)))
        try {
            val controller=ContentDetailsController(cached.id,repository { gate.await() },library,scope)
            controller.removeFromLibrary()
            assertTrue(library.items.value.isEmpty())
            gate.complete(cached)
            assertNull(controller.uiState.value.trackingStatus)
            assertTrue(library.items.value.isEmpty())
        } finally { scope.cancel() }
    }

    @Test fun refreshFailureKeepsSavedContentUsable() {
        val scope=scope()
        val library=MemoryLibrary(listOf(TrackedContentItem(cached,TrackingStatus.PLANNED)))
        try {
            val controller=ContentDetailsController(cached.id,repository { throw IOException("offline") },library,scope)
            assertEquals(cached,controller.uiState.value.content)
            assertFalse(controller.uiState.value.isLoading)
            assertFalse(controller.uiState.value.isRefreshing)
            assertNotNull(controller.uiState.value.errorMessage)
            controller.onStatusChange(TrackingStatus.IN_PROGRESS)
            controller.saveToLibrary()
            assertEquals(TrackingStatus.IN_PROGRESS,library.items.value.single().status)
        } finally { scope.cancel() }
    }

    @Test fun uncachedContentLoadsAndCanBeSaved() = runBlocking {
        val scope=scope()
        val gate=CompletableDeferred<ContentItem>()
        val library=MemoryLibrary(emptyList())
        try {
            val controller=ContentDetailsController(cached.id,repository { gate.await() },library,scope)
            assertTrue(controller.uiState.value.isLoading)
            assertNull(controller.uiState.value.content)
            gate.complete(cached)
            assertFalse(controller.uiState.value.isLoading)
            controller.saveToLibrary()
            assertEquals(cached,library.items.value.single().content)
        } finally { scope.cancel() }
    }
}
