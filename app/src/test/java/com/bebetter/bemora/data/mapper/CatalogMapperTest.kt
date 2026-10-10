package com.bebetter.bemora.data.mapper

import com.bebetter.bemora.data.remote.dto.BookDto
import com.bebetter.bemora.data.remote.dto.BookWorkDto
import com.bebetter.bemora.data.remote.dto.GameDto
import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test

class CatalogMapperTest {
    private val gson = Gson()

    @Test fun booksMapBothWorkKeyFormatsAndOptionalFields() {
        val book = gson.fromJson("""{"key":"/works/OL27482W","title":"The Hobbit","author_name":["Tolkien"],"first_publish_year":1937,"cover_i":12}""", BookDto::class.java).toContentItemOrNull()!!
        assertEquals("openlibrary_book_OL27482W", book.id)
        assertEquals("Tolkien", book.subtitle)
        assertEquals(1937, book.releaseYear)
        assertEquals("https://covers.openlibrary.org/b/id/12-L.jpg?default=false", book.imageUrl)
        assertEquals(book.id, BookDto(key="OL27482W",title="Hobbit").toContentItemOrNull()!!.id)
        assertNull(BookDto(key="/books/OL123M",title="Edition").toContentItemOrNull())
        assertNull(BookDto(key="OL1W",title=" ").toContentItemOrNull())
    }

    @Test fun bookDescriptionsCanBeStringsObjectsOrAbsent() {
        val summary = BookDto(key="OL1W", title="Book", authors=listOf("Author"), year=2000).toContentItemOrNull()!!
        for (json in listOf("""{"description":"Text"}""", """{"description":{"value":"Text"}}""")) {
            val details = gson.fromJson(json, BookWorkDto::class.java).withSummary(summary)
            assertEquals("Text", details.description)
            assertEquals("Author", details.subtitle)
            assertEquals(2000, details.releaseYear)
        }
        assertNull(BookWorkDto().withSummary(summary).description)
        assertNull(gson.fromJson("""{"description":{"value":123}}""", BookWorkDto::class.java).withSummary(summary).description)
    }

    @Test fun gamesNormalizeRatingsAndHandleMissingReleaseDates() {
        val game = gson.fromJson("""{"id":7,"name":"Game","rating":4.5,"ratings_count":2,"released":"2020-01-02","platforms":[{"platform":{"name":"PC"}}]}""",GameDto::class.java).toContentItemOrNull()!!
        assertEquals("rawg_game_7",game.id)
        assertEquals(9.0,game.rating!!,0.0)
        assertEquals(2020,game.releaseYear)
        assertEquals("PC",game.subtitle)
        assertNull(GameDto(id=7,name="Game",rating=4.0,ratingsCount=0).toContentItemOrNull()!!.rating)
        assertNull(GameDto(id=7,name="Game",released="TBA").toContentItemOrNull()!!.releaseYear)
        assertNull(GameDto(id=-1,name="Game").toContentItemOrNull())
        assertEquals("Game & story", GameDto(id=7,name="Game",
            descriptionHtml="<p>Game &amp; story</p>").toContentItemOrNull()!!.description)
    }
}
