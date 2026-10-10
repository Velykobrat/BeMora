package com.bebetter.bemora.data.local

import com.bebetter.bemora.domain.model.*
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class LibraryCodecTest {
    private val items = listOf(
        TrackedContentItem(ContentItem("tmdb_movie_1","Movie",type=ContentType.MOVIE),TrackingStatus.PLANNED),
        TrackedContentItem(ContentItem("openlibrary_book_OL1W","Книга",subtitle="Author",description="Note\nwith quotes: \"text\"",type=ContentType.BOOK),TrackingStatus.IN_PROGRESS),
        TrackedContentItem(ContentItem("rawg_game_1","Game",rating=9.0,type=ContentType.GAME),TrackingStatus.COMPLETED)
    )
    @Test fun mixedLibraryRoundTripsWithoutIdCollisions() {
        assertEquals(items,LibraryCodec.decode(LibraryCodec.encode(items)))
    }
    @Test fun existingVersionOneMovieDataRemainsReadable() {
        val old = """{"version":1,"items":[{"id":"tmdb_movie_1","title":"Movie","type":"MOVIE","status":"PLANNED"}]}"""
        assertEquals(listOf(items[0]),LibraryCodec.decode(old))
        assertEquals(1,JSONObject(LibraryCodec.encode(items)).getInt("version"))
    }
    @Test fun corruptAndMismatchedEntriesDoNotHideValidItems() {
        val root=JSONObject(LibraryCodec.encode(items))
        root.getJSONArray("items").put(JSONObject().put("id","bad"))
            .put(JSONObject(root.getJSONArray("items").getJSONObject(2).toString()).put("type","BOOK"))
        assertEquals(items,LibraryCodec.decode(root.toString()))
        assertTrue(LibraryCodec.decode("broken").isEmpty())
        assertTrue(LibraryCodec.decode("{\"version\":2,\"items\":[]}").isEmpty())
    }
}
