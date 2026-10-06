package com.bebetter.bemora.data.mapper

import com.bebetter.bemora.data.remote.dto.TmdbMovieDto
import com.bebetter.bemora.domain.model.ContentItem
import com.bebetter.bemora.domain.model.ContentType

fun TmdbMovieDto.toContentItem(): ContentItem {

    val year = releaseDate
        ?.take(4)
        ?.toIntOrNull()

    val posterUrl = posterPath?.let {
        "https://image.tmdb.org/t/p/w500$it"
    }

    return ContentItem(
        id = "tmdb_movie_$id",
        title = title,
        subtitle = "Movie",
        description = description,
        imageUrl = posterUrl,
        releaseYear = year,
        rating = rating,
        type = ContentType.MOVIE
    )
}