package com.bebetter.bemora.data.repository

import com.bebetter.bemora.data.remote.api.*
import com.bebetter.bemora.data.remote.dto.*
import com.bebetter.bemora.domain.model.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class ContentRepositoryTest {
    private val movies = object : TmdbApi {
        override suspend fun getMovieDetails(movieId: Int) = TmdbMovieDto(movieId,"Movie",null,null,null,8.0)
        override suspend fun getPopularMovies() = TmdbMovieSearchResponse(1,listOf(getMovieDetails(1)))
        override suspend fun searchMovies(query: String) = getPopularMovies()
    }
    private val books = object : OpenLibraryApi {
        override suspend fun search(query: String,fields: String,limit: Int) = BookSearchResponse(listOf(BookDto("/works/OL1W","Book")))
        override suspend fun work(workId: String) = BookWorkDto(title="Book details")
    }
    private val games = object : RawgApi {
        override suspend fun search(query: String,key: String,pageSize: Int) = GameSearchResponse(listOf(GameDto(id=1,name="Game")))
        override suspend fun details(gameId: Int,key: String) = GameDto(id=gameId,name="Game details")
    }
    private fun repository(gameKey: String="test",bookApi: OpenLibraryApi=books) =
        ContentRepository(movies,bookApi,games,tmdbToken="test",gameKey=gameKey)

    @Test fun allSearchReturnsThreeCatalogsAndTypeSearchOnlyOne() = runBlocking {
        assertEquals(CatalogId.supportedTypes, repository().search("query",null).items.map { it.type })
        assertEquals(listOf(ContentType.GAME),repository().search("query",ContentType.GAME).items.map { it.type })
        assertTrue(repository().search(" ",null).items.isEmpty())
    }
    @Test fun missingKeyAndProviderFailuresPreserveOtherResults() = runBlocking {
        val missingKey=repository(gameKey="").search("query",null)
        assertEquals(listOf(ContentType.MOVIE,ContentType.BOOK),missingKey.items.map { it.type })
        assertTrue(missingKey.errors.containsKey(ContentType.GAME))
        val brokenBooks=object : OpenLibraryApi by books {
            override suspend fun search(query: String,fields: String,limit: Int): BookSearchResponse = throw IOException("secret URL")
        }
        val result=repository(bookApi=brokenBooks).search("query",null)
        assertEquals(listOf(ContentType.MOVIE,ContentType.GAME),result.items.map { it.type })
        assertFalse(result.errors.values.any { it.contains("secret") })
    }
    @Test fun detailsDispatchByProviderAndRetainCanonicalIds() = runBlocking {
        val repository=repository()
        assertEquals("Movie",repository.getContentDetails("tmdb_movie_1").title)
        assertEquals("Book details",repository.getContentDetails("openlibrary_book_OL1W").title)
        assertEquals("rawg_game_1",repository.getContentDetails("rawg_game_1").id)
    }
    @Test fun cancellationIsNotConvertedIntoCatalogError() {
        val cancelled=object : OpenLibraryApi by books {
            override suspend fun search(query: String,fields: String,limit: Int): BookSearchResponse = throw CancellationException("cancelled")
        }
        assertThrows(CancellationException::class.java) { runBlocking { repository(bookApi=cancelled).search("query",ContentType.BOOK) } }
    }
    @Test fun repeatedSuccessfulQueriesUseTheCache() = runBlocking {
        var requests = 0
        val countingBooks = object : OpenLibraryApi by books {
            override suspend fun search(query: String, fields: String, limit: Int): BookSearchResponse {
                requests++
                return books.search(query, fields, limit)
            }
        }
        val repository = repository(bookApi = countingBooks)
        val first = repository.search("query", ContentType.BOOK)
        assertEquals(first, repository.search(" query ", ContentType.BOOK))
        assertEquals(1, requests)
    }
}
