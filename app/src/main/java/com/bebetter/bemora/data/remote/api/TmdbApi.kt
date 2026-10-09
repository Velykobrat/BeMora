package com.bebetter.bemora.data.remote.api

import com.bebetter.bemora.data.remote.dto.TmdbMovieSearchResponse
import com.bebetter.bemora.data.remote.dto.TmdbMovieDto
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Path

interface TmdbApi {

    @GET("movie/{movieId}")
    suspend fun getMovieDetails(@Path("movieId") movieId: Int): TmdbMovieDto

    @GET("movie/popular")
    suspend fun getPopularMovies(): TmdbMovieSearchResponse

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String
    ): TmdbMovieSearchResponse
}
