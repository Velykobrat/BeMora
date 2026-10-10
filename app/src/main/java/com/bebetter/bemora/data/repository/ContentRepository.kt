package com.bebetter.bemora.data.repository

import com.bebetter.bemora.BuildConfig
import com.bebetter.bemora.data.mapper.toContentItem
import com.bebetter.bemora.data.mapper.toContentItemOrNull
import com.bebetter.bemora.data.mapper.withSummary
import com.bebetter.bemora.data.remote.api.CatalogClients
import com.bebetter.bemora.data.remote.api.TmdbApi
import com.bebetter.bemora.data.remote.api.OpenLibraryApi
import com.bebetter.bemora.data.remote.api.RawgApi
import com.bebetter.bemora.data.remote.api.TmdbClient
import com.bebetter.bemora.domain.model.CatalogId
import com.bebetter.bemora.domain.model.ContentItem
import com.bebetter.bemora.domain.model.ContentType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope

class ContentRepository(
    private val movieApi: TmdbApi = TmdbClient.api,
    private val booksApi: OpenLibraryApi = CatalogClients.books,
    private val gamesApi: RawgApi = CatalogClients.games,
    private val tmdbToken: String = BuildConfig.TMDB_TOKEN,
    private val gameKey: String = BuildConfig.RAWG_API_KEY
) : CatalogRepository {
    private data class CachedSearch(val timestamp: Long, val items: List<ContentItem>)
    private val searches = linkedMapOf<Pair<ContentType, String>, CachedSearch>()

    private suspend fun cachedSearch(query: String, type: ContentType): List<ContentItem> {
        val key = type to query
        val cached = synchronized(searches) { searches[key] }
        if (cached != null && System.nanoTime() - cached.timestamp < 300_000_000_000L) return cached.items
        val items = searchCategory(query, type)
        synchronized(searches) {
            searches[key] = CachedSearch(System.nanoTime(), items)
            if (searches.size > 30) searches.remove(searches.keys.first())
        }
        return items
    }

    suspend fun getMovieDetails(movieId: Int): ContentItem {
        require(movieId > 0) { "Invalid movie ID" }
        if (tmdbToken.isBlank()) throw CatalogConfigurationException("Movie catalog is not configured.")
        return movieApi.getMovieDetails(movieId).toContentItem()
    }

    suspend fun getPopularMovies(): List<ContentItem> =
        movieApi.getPopularMovies().results.map { it.toContentItem() }

    suspend fun searchMovies(query: String): List<ContentItem> =
        if (query.isBlank()) emptyList() else movieApi.searchMovies(query).results.map { it.toContentItem() }

    override suspend fun search(query: String, type: ContentType?): CatalogSearchResult {
        if (query.isBlank()) return CatalogSearchResult()
        require(type == null || type in CatalogId.supportedTypes)
        val types = type?.let { listOf(it) } ?: CatalogId.supportedTypes
        return supervisorScope {
            val results = types.map { category ->
                async {
                    try {
                        CatalogSearchResult(items = cachedSearch(query.trim(), category))
                    } catch (exception: CancellationException) {
                        throw exception
                    } catch (exception: Exception) {
                        CatalogSearchResult(errors = mapOf(category to catalogErrorMessage(exception)))
                    }
                }
            }.map { it.await() }
            CatalogSearchResult(results.flatMap { it.items }.distinctBy { it.id },
                results.flatMap { it.errors.entries }.associate { it.toPair() })
        }
    }

    private suspend fun searchCategory(query: String, type: ContentType): List<ContentItem> = when (type) {
        ContentType.MOVIE -> {
            if (tmdbToken.isBlank()) throw CatalogConfigurationException("Movie catalog is not configured.")
            searchMovies(query)
        }
        ContentType.BOOK -> CatalogClients.bookRequest { booksApi.search(query) }
            .docs.orEmpty().mapNotNull { it.toContentItemOrNull() }
        ContentType.GAME -> gamesApi.search(query, rawgKey())
            .results.orEmpty().mapNotNull { it.toContentItemOrNull() }
        else -> error("Unsupported catalog")
    }

    override suspend fun getContentDetails(id: String): ContentItem = when (CatalogId.typeOf(id)) {
        ContentType.MOVIE -> getMovieDetails(id.removePrefix("tmdb_movie_").toInt())
        ContentType.BOOK -> {
            val workId = id.removePrefix("openlibrary_book_")
            val summary = CatalogClients.bookRequest {
                booksApi.search("key:/works/" + workId, limit = 1)
            }.docs.orEmpty().mapNotNull { it.toContentItemOrNull() }.firstOrNull { it.id == id }
            val work = CatalogClients.bookRequest { booksApi.work(workId) }
            work.withSummary(summary ?: ContentItem(id = id,
                title = work.title?.takeIf { it.isNotBlank() } ?: error("Book title is missing"),
                type = ContentType.BOOK))
        }
        ContentType.GAME -> gamesApi.details(id.removePrefix("rawg_game_").toInt(), rawgKey())
            .toContentItemOrNull()?.takeIf { it.id == id } ?: error("Invalid game details")
        else -> error("Invalid content ID")
    }

    private fun rawgKey(): String = gameKey.trim().ifEmpty {
        throw CatalogConfigurationException("Game catalog is not configured. Add a RAWG API key to enable it.")
    }
}
