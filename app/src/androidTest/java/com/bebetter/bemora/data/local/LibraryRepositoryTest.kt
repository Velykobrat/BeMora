package com.bebetter.bemora.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.bebetter.bemora.domain.model.ContentItem
import com.bebetter.bemora.domain.model.ContentType
import com.bebetter.bemora.domain.model.TrackedContentItem
import com.bebetter.bemora.domain.model.TrackingStatus
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LibraryRepositoryTest {
    private lateinit var preferences: SharedPreferences
    private val movieA = ContentItem(
        id = "tmdb_movie_1",
        title = "Movie A",
        subtitle = "Movie",
        description = "A description with quotes: \"hello\" and a newline\n",
        imageUrl = "https://example.com/poster.jpg",
        releaseYear = 2024,
        rating = 8.5,
        type = ContentType.MOVIE
    )
    private val movieB = ContentItem(id = "tmdb_movie_2", title = "Movie B", type = ContentType.MOVIE)

    @Before
    fun setUp() {
        preferences = InstrumentationRegistry.getInstrumentation().targetContext
            .getSharedPreferences("library_repository_test", Context.MODE_PRIVATE)
        assertTrue(preferences.edit().clear().commit())
    }

    @After
    fun tearDown() {
        preferences.edit().clear().commit()
    }

    @Test
    fun booksGamesAndMoviesSurviveRestartAndCanBeUpdatedIndependently() = runBlocking {
        val book = ContentItem("openlibrary_book_OL1W", "Book", type = ContentType.BOOK)
        val game = ContentItem("rawg_game_1", "Game", type = ContentType.GAME)
        val repository = LibraryRepository(preferences)
        for (item in listOf(movieA, book, game)) repository.save(item, TrackingStatus.PLANNED)
        val restored = LibraryRepository(preferences)
        assertEquals(3, restored.items.value.size)
        restored.save(book, TrackingStatus.COMPLETED)
        restored.remove(game.id)
        assertEquals(listOf(TrackedContentItem(movieA, TrackingStatus.PLANNED),
            TrackedContentItem(book, TrackingStatus.COMPLETED)), LibraryRepository(preferences).items.value)
    }

    @Test
    fun updatesAndRemovalSurviveRepositoryRecreation() = runBlocking {
        val repository = LibraryRepository(preferences)
        repository.save(movieA, TrackingStatus.PLANNED)
        repository.save(movieB, TrackingStatus.COMPLETED)
        assertEquals(2, repository.items.value.size)
        assertEquals(repository.items.value, LibraryRepository(preferences).items.value)

        repository.save(movieA, TrackingStatus.IN_PROGRESS)
        assertEquals(2, repository.items.value.size)
        repository.remove(movieB.id)

        val restored = LibraryRepository(preferences)
        assertEquals(listOf(TrackedContentItem(movieA, TrackingStatus.IN_PROGRESS)), restored.items.value)
        assertEquals(repository.items.value, restored.items.value)
    }

    @Test
    fun corruptEntriesAreSkippedAndDuplicateIdsAreRestoredOnce() = runBlocking {
        LibraryRepository(preferences).save(movieA, TrackingStatus.PLANNED)
        val root = JSONObject(preferences.getString("tracked_movies", null)!!)
        root.getJSONArray("items")
            .put(JSONObject().put("id", "damaged"))
            .put(JSONObject(root.getJSONArray("items").getJSONObject(0).toString())
                .put("status", "COMPLETED"))
            .put(JSONObject(root.getJSONArray("items").getJSONObject(0).toString())
                .put("status", "UNKNOWN_STATUS"))
        assertTrue(preferences.edit().putString("tracked_movies", root.toString()).commit())
        assertEquals(
            listOf(TrackedContentItem(movieA, TrackingStatus.COMPLETED)),
            LibraryRepository(preferences).items.value
        )
    }

    @Test
    fun invalidStoredDataDoesNotCrashAndCanBeReplaced() = runBlocking {
        for (stored in listOf("not JSON", "{}", "{\"version\":2,\"items\":[]}")) {
            preferences.edit().putString("tracked_movies", stored).commit()
            val repository = LibraryRepository(preferences)
            assertTrue(repository.items.value.isEmpty())
            repository.save(movieB, TrackingStatus.DROPPED)
            assertEquals(
                listOf(TrackedContentItem(movieB, TrackingStatus.DROPPED)),
                LibraryRepository(preferences).items.value
            )
        }
        preferences.edit().putInt("tracked_movies", 123).commit()
        assertTrue(LibraryRepository(preferences).items.value.isEmpty())
    }
}
