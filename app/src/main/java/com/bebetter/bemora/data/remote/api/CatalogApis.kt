package com.bebetter.bemora.data.remote.api

import com.bebetter.bemora.data.remote.dto.BookSearchResponse
import com.bebetter.bemora.data.remote.dto.BookWorkDto
import com.bebetter.bemora.data.remote.dto.GameDto
import com.bebetter.bemora.data.remote.dto.GameSearchResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OpenLibraryApi {
    @GET("search.json")
    suspend fun search(
        @Query("q") query: String,
        @Query("fields") fields: String = "key,title,author_name,first_publish_year,cover_i",
        @Query("limit") limit: Int = 20
    ): BookSearchResponse

    @GET("works/{workId}.json")
    suspend fun work(@Path("workId") workId: String): BookWorkDto
}

interface RawgApi {
    @GET("games")
    suspend fun search(
        @Query("search") query: String,
        @Query("key") key: String,
        @Query("page_size") pageSize: Int = 20
    ): GameSearchResponse

    @GET("games/{gameId}")
    suspend fun details(@Path("gameId") gameId: Int, @Query("key") key: String): GameDto
}
