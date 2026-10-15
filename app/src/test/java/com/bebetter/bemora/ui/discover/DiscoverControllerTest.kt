package com.bebetter.bemora.ui.discover

import com.bebetter.bemora.data.repository.DiscoverRepository
import com.bebetter.bemora.domain.model.*
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class DiscoverControllerTest {
    private fun item(type: ContentType)=ContentItem(type.name,"Item",type=type)

    @Test fun slowMoviesDoNotBlockBooksAndGames() = runBlocking {
        val scope=CoroutineScope(SupervisorJob()+Dispatchers.Unconfined)
        val gate=CompletableDeferred<Unit>()
        try {
            val repository=object : DiscoverRepository {
                override suspend fun getDiscoverItems(type: ContentType): List<ContentItem> {
                    if(type==ContentType.MOVIE) gate.await()
                    return listOf(item(type))
                }
            }
            val controller=DiscoverController(repository,scope)
            assertTrue(controller.uiState.value.sections.getValue(ContentType.MOVIE).isLoading)
            for(type in listOf(ContentType.BOOK,ContentType.GAME)) {
                assertEquals(listOf(item(type)),controller.uiState.value.sections.getValue(type).items)
                assertFalse(controller.uiState.value.sections.getValue(type).isLoading)
            }
            gate.complete(Unit)
            assertFalse(controller.uiState.value.sections.getValue(ContentType.MOVIE).isLoading)
        } finally { scope.cancel() }
    }

    @Test fun failedSectionRetriesWithoutReloadingOtherSections() {
        val scope=CoroutineScope(SupervisorJob()+Dispatchers.Unconfined)
        val requests=mutableMapOf<ContentType,Int>()
        try {
            val repository=object : DiscoverRepository {
                override suspend fun getDiscoverItems(type: ContentType): List<ContentItem> {
                    val count=(requests[type] ?: 0)+1
                    requests[type]=count
                    if(type==ContentType.GAME && count==1) throw IOException("unavailable")
                    return listOf(item(type))
                }
            }
            val controller=DiscoverController(repository,scope)
            assertNotNull(controller.uiState.value.sections.getValue(ContentType.GAME).errorMessage)
            val books=controller.uiState.value.sections.getValue(ContentType.BOOK)
            controller.load(ContentType.GAME)
            assertEquals(books,controller.uiState.value.sections.getValue(ContentType.BOOK))
            assertEquals(1,requests[ContentType.BOOK])
            assertEquals(1,requests[ContentType.MOVIE])
            assertNull(controller.uiState.value.sections.getValue(ContentType.GAME).errorMessage)
            assertEquals(listOf(item(ContentType.GAME)),controller.uiState.value.sections.getValue(ContentType.GAME).items)
        } finally { scope.cancel() }
    }
}
