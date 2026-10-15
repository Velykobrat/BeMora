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
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ContentRepository(
    private val movieApi: TmdbApi = TmdbClient.api,
    private val booksApi: OpenLibraryApi = CatalogClients.books,
    private val gamesApi: RawgApi = CatalogClients.games,
    private val tmdbToken: String = BuildConfig.TMDB_TOKEN,
    private val gameKey: String = BuildConfig.RAWG_API_KEY
) : CatalogRepository, DiscoverRepository {
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
        val result = searchUpdates(query, type).last()
        return CatalogSearchResult(result.items, result.errors)
    }

    override fun searchUpdates(query: String, type: ContentType?): Flow<CatalogSearchProgress> = channelFlow {
        require(type == null || type in CatalogId.supportedTypes)
        if (query.isBlank()) {
            send(CatalogSearchProgress())
            return@channelFlow
        }
        val types = type?.let { listOf(it) } ?: CatalogId.supportedTypes
        val pending = types.toMutableSet()
        val items = mutableMapOf<ContentType, List<ContentItem>>()
        val errors = mutableMapOf<ContentType, String>()
        val mutex = Mutex()
        send(CatalogSearchProgress(loadingTypes = pending.toSet()))
        types.forEach { category ->
            launch {
                val result = try {
                    CatalogSearchResult(items = cachedSearch(query.trim(), category))
                } catch (exception: CancellationException) {
                    this@channelFlow.cancel("Catalog search cancelled", exception)
                    throw exception
                } catch (exception: Exception) {
                    CatalogSearchResult(errors = mapOf(category to catalogErrorMessage(exception)))
                }
                mutex.withLock {
                    items[category] = result.items
                    errors.putAll(result.errors)
                    pending.remove(category)
                    send(CatalogSearchProgress(
                        items = types.flatMap { items[it].orEmpty() }.distinctBy { it.id },
                        errors = types.mapNotNull { source -> errors[source]?.let { source to it } }.toMap(),
                        loadingTypes = pending.toSet()
                    ))
                }
            }
        }
    }

    override suspend fun getDiscoverItems(type: ContentType): List<ContentItem> = when (type) {
        ContentType.MOVIE -> {
            if (tmdbToken.isBlank()) throw CatalogConfigurationException("Movie catalog is not configured.")
            getPopularMovies()
        }
        ContentType.BOOK -> CatalogClients.bookRequest { booksApi.subject("fiction") }
            .works.orEmpty().mapNotNull { it.toContentItemOrNull() }
        ContentType.GAME -> gamesApi.popular(rawgKey()).results.orEmpty().mapNotNull { it.toContentItemOrNull() }
        else -> error("Unsupported catalog")
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
            // Work details remain usable when the separate search index is unavailable.
            val work = CatalogClients.bookRequest { booksApi.work(workId) }
            val summary = try {
                CatalogClients.bookRequest { booksApi.search("key:/works/" + workId, limit = 1) }
                    .docs.orEmpty().mapNotNull { it.toContentItemOrNull() }.firstOrNull { it.id == id }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                null
            }
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
