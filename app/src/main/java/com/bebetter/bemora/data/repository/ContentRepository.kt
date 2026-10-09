package com.bebetter.bemora.data.repository

import com.bebetter.bemora.data.mapper.toContentItem
import com.bebetter.bemora.data.remote.api.TmdbClient
import com.bebetter.bemora.domain.model.ContentItem

class ContentRepository {

    suspend fun getMovieDetails(movieId: Int): ContentItem {
        require(movieId > 0) { "Invalid movie ID" }
        return TmdbClient.api.getMovieDetails(movieId).toContentItem()
    }

    suspend fun getPopularMovies(): List<ContentItem> {
        val response = TmdbClient.api.getPopularMovies()

        return response.results.map { movie ->
            movie.toContentItem()
        }
    }

    suspend fun searchMovies(
        query: String
    ): List<ContentItem> {

        if (query.isBlank()) {
            return emptyList()
        }

        val response =
            TmdbClient.api.searchMovies(query)

        return response.results.map { movie ->
            movie.toContentItem()
        }
    }
}
