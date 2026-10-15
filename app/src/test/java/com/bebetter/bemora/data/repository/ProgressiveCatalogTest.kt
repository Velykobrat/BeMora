package com.bebetter.bemora.data.repository

import com.bebetter.bemora.data.remote.api.*
import com.bebetter.bemora.data.remote.dto.*
import com.bebetter.bemora.domain.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.toList
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class ProgressiveCatalogTest {
    private val movies = object : TmdbApi {
        override suspend fun getMovieDetails(movieId: Int) = TmdbMovieDto(movieId,"Movie",null,null,null,null)
        override suspend fun getPopularMovies() = TmdbMovieSearchResponse(1,listOf(getMovieDetails(1)))
        override suspend fun searchMovies(query: String) = getPopularMovies()
    }
    private val books = object : OpenLibraryApi {
        override suspend fun search(query: String,fields: String,limit: Int) = BookSearchResponse(listOf(BookDto("/works/OL1W","Book")))
        override suspend fun work(workId: String) = BookWorkDto(title="Available book")
        override suspend fun subject(subject: String,limit: Int): BookSubjectResponse {
            assertEquals("fiction",subject)
            assertEquals(20,limit)
            return BookSubjectResponse(listOf(SubjectBookDto("/works/OL1W","Fiction",listOf(NamedDto("Author")),1990,12)))
        }
    }
    private val games = object : RawgApi {
        override suspend fun search(query: String,key: String,pageSize: Int) = GameSearchResponse(listOf(GameDto(id=1,name="Game")))
        override suspend fun details(gameId: Int,key: String) = GameDto(id=gameId,name="Game")
        override suspend fun popular(key: String,ordering: String,pageSize: Int): GameSearchResponse {
            assertEquals("test",key)
            assertEquals("-added",ordering)
            assertEquals(20,pageSize)
            return GameSearchResponse(listOf(GameDto(id=1,name="Popular game")))
        }
    }

    @Test fun fastCatalogsAreEmittedBeforeTheSlowOneCompletes() = runBlocking {
        val gate=CompletableDeferred<Unit>()
        val slowMovies=object : TmdbApi by movies {
            override suspend fun searchMovies(query: String): TmdbMovieSearchResponse {
                gate.await()
                return movies.searchMovies(query)
            }
        }
        val repository=ContentRepository(slowMovies,books,games,"test","test")
        val emissions=mutableListOf<CatalogSearchProgress>()
        val job=launch { repository.searchUpdates("query",null).toList(emissions) }
        try {
            withTimeout(5000) { while(emissions.none { it.items.size==2 }) delay(10) }
            val partial=emissions.last()
            assertEquals(listOf(ContentType.BOOK,ContentType.GAME),partial.items.map { it.type })
            assertEquals(setOf(ContentType.MOVIE),partial.loadingTypes)
            assertTrue(job.isActive)
            gate.complete(Unit)
            job.join()
            assertEquals(CatalogId.supportedTypes,emissions.last().items.map { it.type })
            assertTrue(emissions.last().loadingTypes.isEmpty())
        } finally { job.cancelAndJoin() }
    }

    @Test fun errorsDoNotErasePartialResults() = runBlocking {
        val brokenGames=object : RawgApi by games {
            override suspend fun search(query: String,key: String,pageSize: Int): GameSearchResponse = throw IOException("unavailable")
        }
        val emissions=ContentRepository(movies,books,brokenGames,"test","test").searchUpdates("query",null).toList()
        val result=emissions.last()
        assertEquals(listOf(ContentType.MOVIE,ContentType.BOOK),result.items.map { it.type })
        assertTrue(result.errors.containsKey(ContentType.GAME))
        assertTrue(result.loadingTypes.isEmpty())
    }

    @Test fun cancellingTheCollectorCancelsPendingProviderWork() = runBlocking {
        val started=CompletableDeferred<Unit>()
        val cancelled=CompletableDeferred<Unit>()
        val waitingGames=object : RawgApi by games {
            override suspend fun search(query: String,key: String,pageSize: Int): GameSearchResponse {
                started.complete(Unit)
                try { awaitCancellation() } finally { cancelled.complete(Unit) }
            }
        }
        val job=launch { ContentRepository(movies,books,waitingGames,"test","test").searchUpdates("query",ContentType.GAME).toList() }
        withTimeout(3000) { started.await() }
        job.cancelAndJoin()
        assertTrue(cancelled.isCompleted)
    }

    @Test fun bookWorkSurvivesSearchIndexFailureAndMissingMetadata() = runBlocking {
        var workRequested=false
        val brokenIndex=object : OpenLibraryApi by books {
            override suspend fun work(workId: String): BookWorkDto {
                workRequested=true
                return books.work(workId)
            }
            override suspend fun search(query: String,fields: String,limit: Int): BookSearchResponse {
                assertTrue(workRequested)
                throw IOException("index down")
            }
        }
        val result=ContentRepository(movies,brokenIndex,games,"test","test").getContentDetails("openlibrary_book_OL1W")
        assertEquals("Available book",result.title)
        assertEquals("openlibrary_book_OL1W",result.id)
        assertEquals(ContentType.BOOK,result.type)
    }

    @Test fun bookMetadataCancellationRemainsCancellation() {
        val cancelled=object : OpenLibraryApi by books {
            override suspend fun search(query: String,fields: String,limit: Int): BookSearchResponse = throw CancellationException("cancelled")
        }
        assertThrows(CancellationException::class.java) {
            runBlocking { ContentRepository(movies,cancelled,games,"test","test").getContentDetails("openlibrary_book_OL1W") }
        }
    }

    @Test fun discoverUsesRealCatalogEndpointsAndMapsSubjectBooks() = runBlocking {
        val repository=ContentRepository(movies,books,games,"test","test")
        assertEquals(ContentType.MOVIE,repository.getDiscoverItems(ContentType.MOVIE).single().type)
        val book=repository.getDiscoverItems(ContentType.BOOK).single()
        assertEquals("Author",book.subtitle)
        assertEquals(1990,book.releaseYear)
        assertEquals("https://covers.openlibrary.org/b/id/12-L.jpg?default=false",book.imageUrl)
        assertEquals(ContentType.GAME,repository.getDiscoverItems(ContentType.GAME).single().type)
    }
}
