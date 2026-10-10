package com.bebetter.bemora.data.remote.dto

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class BookSearchResponse(val docs: List<BookDto>? = null)
data class BookDto(
    val key: String? = null,
    val title: String? = null,
    @SerializedName("author_name") val authors: List<String?>? = null,
    @SerializedName("first_publish_year") val year: Int? = null,
    @SerializedName("cover_i") val coverId: Long? = null
)
data class BookWorkDto(
    val title: String? = null,
    val description: JsonElement? = null,
    val covers: List<Long?>? = null
)
data class GameSearchResponse(val results: List<GameDto>? = null)
data class GameDto(
    val id: Int? = null,
    val name: String? = null,
    val released: String? = null,
    @SerializedName("background_image") val imageUrl: String? = null,
    val rating: Double? = null,
    @SerializedName("ratings_count") val ratingsCount: Int? = null,
    @SerializedName("description_raw") val description: String? = null,
    val platforms: List<GamePlatformDto>? = null,
    @SerializedName("description") val descriptionHtml: String? = null
)
data class GamePlatformDto(val platform: NamedDto? = null)
data class NamedDto(val name: String? = null)
