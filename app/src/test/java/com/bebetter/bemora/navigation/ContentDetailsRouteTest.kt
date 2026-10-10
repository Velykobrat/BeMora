package com.bebetter.bemora.navigation

import com.bebetter.bemora.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class ContentDetailsRouteTest {
    @Test fun allCatalogsHaveValidatedRoutes() {
        for ((id,type) in listOf("tmdb_movie_1" to ContentType.MOVIE,
            "openlibrary_book_OL123W" to ContentType.BOOK, "rawg_game_1" to ContentType.GAME)) {
            assertEquals("content_details?contentId="+id, Screen.ContentDetails.routeFor(ContentItem(id,title="Item",type=type)))
            assertEquals(id,Screen.ContentDetails.idOrNull(id))
        }
    }
    @Test fun invalidAndMismatchedIdsAreRejected() {
        for (id in listOf("rawg_game_0","rawg_game_9999999999999","openlibrary_book_OL1W/../x",
            "openlibrary_book_OL0W","tmdb_movie_-1","rawg_game_1&contentId=x")) assertNull(Screen.ContentDetails.idOrNull(id))
        assertNull(Screen.ContentDetails.routeFor(ContentItem("rawg_game_1","Wrong",type=ContentType.BOOK)))
    }
}
