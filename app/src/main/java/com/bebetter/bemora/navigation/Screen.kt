package com.bebetter.bemora.navigation

import com.bebetter.bemora.domain.model.CatalogId
import com.bebetter.bemora.domain.model.ContentItem
import com.bebetter.bemora.domain.model.ContentType

sealed class Screen(val route: String) {
    data object Discover : Screen("discover")
    data object Search : Screen("search")
    data object Library : Screen("library")
    data object Profile : Screen("profile")
    data object ContentDetails : Screen("content_details?contentId={contentId}") {
        const val CONTENT_ID = "contentId"
        fun idOrNull(value: String?): String? = value?.takeIf { CatalogId.typeOf(it) != null }
        fun routeFor(item: ContentItem): String? =
            if (CatalogId.isValid(item)) "content_details?contentId=" + item.id else null
    }

    data object MovieDetails : Screen("movie_details?movieId={movieId}") {
        const val MOVIE_ID = "movieId"

        fun movieIdOrNull(value: String?): Int? =
            value?.takeIf { it.isNotEmpty() && it.all { character -> character in '0'..'9' } }
                ?.toIntOrNull()?.takeIf { it > 0 }

        fun routeFor(item: ContentItem): String? {
            if (item.type != ContentType.MOVIE || !item.id.startsWith("tmdb_movie_")) {
                return null
            }
            val movieId = movieIdOrNull(item.id.removePrefix("tmdb_movie_")) ?: return null
            return "movie_details?movieId=$movieId"
        }
    }
}
