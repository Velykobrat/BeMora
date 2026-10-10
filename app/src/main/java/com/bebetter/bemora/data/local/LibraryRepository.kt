package com.bebetter.bemora.data.local

import android.content.Context
import android.content.SharedPreferences
import com.bebetter.bemora.domain.model.ContentItem
import com.bebetter.bemora.domain.model.CatalogId
import com.bebetter.bemora.domain.model.TrackedContentItem
import com.bebetter.bemora.domain.model.TrackingStatus
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class LibraryRepository internal constructor(private val preferences: SharedPreferences) {

    private val mutex = Mutex()
    private val trackedItems = MutableStateFlow(readItems())
    val items = trackedItems.asStateFlow()

    suspend fun save(content: ContentItem, status: TrackingStatus) {
        require(CatalogId.isValid(content)) { "Only supported catalog items can be tracked" }
        update { current ->
            val tracked = TrackedContentItem(content, status)
            if (current.any { it.content.id == content.id }) {
                current.map { if (it.content.id == content.id) tracked else it }
            } else {
                current + tracked
            }
        }
    }

    suspend fun remove(contentId: String) {
        update { current -> current.filterNot { it.content.id == contentId } }
    }

    private suspend fun update(transform: (List<TrackedContentItem>) -> List<TrackedContentItem>) {
        withContext(Dispatchers.IO) {
            mutex.withLock {
                val updated = transform(trackedItems.value)
                val encoded = LibraryCodec.encode(updated)
                if (!preferences.edit().putString(ITEMS_KEY, encoded).commit()) {
                    throw IOException("Unable to save Library on this device")
                }
                trackedItems.value = updated
            }
        }
    }

    private fun readItems(): List<TrackedContentItem> = try {
        LibraryCodec.decode(preferences.getString(ITEMS_KEY, null))
    } catch (_: Exception) {
        emptyList()
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
