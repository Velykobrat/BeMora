package com.bebetter.bemora.data.remote.api

import com.bebetter.bemora.data.remote.dto.TmdbMovieSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface TmdbApi {

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String
    ): TmdbMovieSearchResponse
}