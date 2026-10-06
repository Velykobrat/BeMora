package com.bebetter.bemora.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TmdbMovieSearchResponse(
    val page: Int,
    val results: List<TmdbMovieDto>
)

data class TmdbMovieDto(
    val id: Int,
    val title: String,

    @SerializedName("overview")
    val description: String?,

    @SerializedName("poster_path")
    val posterPath: String?,

    @SerializedName("release_date")
    val releaseDate: String?,

    @SerializedName("vote_average")
    val rating: Double?
)