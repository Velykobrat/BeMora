package com.bebetter.bemora.navigation

import com.bebetter.bemora.domain.model.ContentItem
import com.bebetter.bemora.domain.model.ContentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MovieDetailsRouteTest {
    @Test
    fun movieRoutePassesOnlyNumericId() {
        val movie = ContentItem(id = "tmdb_movie_123", title = "Example", type = ContentType.MOVIE)
        assertEquals("movie_details?movieId=123", Screen.MovieDetails.routeFor(movie))
    }

    @Test
    fun unsupportedContentAndMalformedIdsHaveNoMovieRoute() {
        val movie = ContentItem(id = "tmdb_movie_123", title = "Example", type = ContentType.MOVIE)
        for (type in ContentType.entries.filter { it != ContentType.MOVIE }) {
            assertNull(Screen.MovieDetails.routeFor(movie.copy(type = type)))
        }
        for (id in listOf("123", "movie_123", "tmdb_movie_", "tmdb_movie_abc", "tmdb_movie_0", "tmdb_movie_-1")) {
            assertNull(Screen.MovieDetails.routeFor(movie.copy(id = id)))
        }
    }

    @Test
    fun invalidOrMissingRouteIdsAreRejected() {
        for (id in listOf(null, "", "abc", "0", "-1", "+1", " 1", "1.5", "2147483648")) {
            assertNull(Screen.MovieDetails.movieIdOrNull(id))
        }
        assertEquals(123, Screen.MovieDetails.movieIdOrNull("123"))
    }
}
