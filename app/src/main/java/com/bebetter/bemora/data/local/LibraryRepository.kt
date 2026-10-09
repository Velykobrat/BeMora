package com.bebetter.bemora.data.local

import android.content.Context
import android.content.SharedPreferences
import com.bebetter.bemora.domain.model.ContentItem
import com.bebetter.bemora.domain.model.ContentType
import com.bebetter.bemora.domain.model.TrackedContentItem
import com.bebetter.bemora.domain.model.TrackingStatus
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class LibraryRepository internal constructor(private val preferences: SharedPreferences) {

    private val mutex = Mutex()
    private val trackedItems = MutableStateFlow(readItems())
    val items = trackedItems.asStateFlow()

    suspend fun save(movie: ContentItem, status: TrackingStatus) {
        require(isValidMovie(movie)) { "Only TMDB movies can be tracked" }
        update { current ->
            val tracked = TrackedContentItem(movie, status)
            if (current.any { it.content.id == movie.id }) {
                current.map { if (it.content.id == movie.id) tracked else it }
            } else {
                current + tracked
            }
        }
    }

    suspend fun remove(movieId: String) {
        update { current -> current.filterNot { it.content.id == movieId } }
    }

    private suspend fun update(transform: (List<TrackedContentItem>) -> List<TrackedContentItem>) {
        withContext(Dispatchers.IO) {
            mutex.withLock {
                val updated = transform(trackedItems.value)
                val encoded = JSONObject()
                    .put("version", 1)
                    .put("items", JSONArray().apply {
                        updated.forEach { tracked ->
                            val movie = tracked.content
                            put(JSONObject()
                                .put("id", movie.id)
                                .put("title", movie.title)
                                .put("subtitle", movie.subtitle ?: JSONObject.NULL)
                                .put("description", movie.description ?: JSONObject.NULL)
                                .put("imageUrl", movie.imageUrl ?: JSONObject.NULL)
                                .put("releaseYear", movie.releaseYear ?: JSONObject.NULL)
                                .put("rating", movie.rating ?: JSONObject.NULL)
                                .put("type", movie.type.name)
                                .put("status", tracked.status.name))
                        }
                    }).toString()
                if (!preferences.edit().putString(ITEMS_KEY, encoded).commit()) {
                    throw IOException("Unable to save Library on this device")
                }
                trackedItems.value = updated
            }
        }
    }

    private fun readItems(): List<TrackedContentItem> {
        return try {
            val stored = preferences.getString(ITEMS_KEY, null) ?: return emptyList()
            val root = JSONObject(stored)
            if (root.getInt("version") != 1) return emptyList()
            val entries = root.getJSONArray("items")
            val restored = linkedMapOf<String, TrackedContentItem>()
            for (index in 0 until entries.length()) {
                try {
                    val entry = entries.getJSONObject(index)
                    val movie = ContentItem(
                        id = entry.getString("id"),
                        title = entry.getString("title"),
                        subtitle = entry.nullableString("subtitle"),
                        description = entry.nullableString("description"),
                        imageUrl = entry.nullableString("imageUrl"),
                        releaseYear = if (entry.isNull("releaseYear")) null else entry.getInt("releaseYear"),
                        rating = if (entry.isNull("rating")) null else entry.getDouble("rating"),
                        type = ContentType.valueOf(entry.getString("type"))
                    )
                    val status = TrackingStatus.valueOf(entry.getString("status"))
                    if (isValidMovie(movie)) {
                        restored[movie.id] = TrackedContentItem(movie, status)
                    }
                } catch (_: Exception) {
                    // A damaged entry must not prevent other movies from being restored.
                }
            }
            restored.values.toList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun JSONObject.nullableString(key: String): String? =
        if (isNull(key)) null else getString(key)

    private fun isValidMovie(movie: ContentItem): Boolean {
        val id = movie.id.removePrefix("tmdb_movie_")
        return movie.type == ContentType.MOVIE && movie.title.isNotBlank() &&
            movie.id.startsWith("tmdb_movie_") && id.isNotEmpty() &&
            id.all { it in '0'..'9' } && (id.toIntOrNull() ?: 0) > 0 &&
            (movie.rating == null || movie.rating.isFinite())
    }

    companion object {
        private const val ITEMS_KEY = "tracked_movies"

        @Volatile
        private var instance: LibraryRepository? = null

        fun getInstance(context: Context): LibraryRepository =
            instance ?: synchronized(this) {
                instance ?: LibraryRepository(
                    context.applicationContext.getSharedPreferences("bemora_library", Context.MODE_PRIVATE)
                ).also { instance = it }
            }
    }
}
