package com.bebetter.bemora.data.local

import com.bebetter.bemora.domain.model.CatalogId
import com.bebetter.bemora.domain.model.ContentItem
import com.bebetter.bemora.domain.model.ContentType
import com.bebetter.bemora.domain.model.TrackedContentItem
import com.bebetter.bemora.domain.model.TrackingStatus
import org.json.JSONArray
import org.json.JSONObject

internal object LibraryCodec {
    fun encode(items: List<TrackedContentItem>): String {
        return JSONObject()
            .put("version", 1)
            .put("items", JSONArray().apply {
                items.forEach { tracked ->
                    val content = tracked.content
                    put(JSONObject()
                        .put("id", content.id)
                        .put("title", content.title)
                        .put("subtitle", content.subtitle ?: JSONObject.NULL)
                        .put("description", content.description ?: JSONObject.NULL)
                        .put("imageUrl", content.imageUrl ?: JSONObject.NULL)
                        .put("releaseYear", content.releaseYear ?: JSONObject.NULL)
                        .put("rating", content.rating ?: JSONObject.NULL)
                        .put("type", content.type.name)
                        .put("status", tracked.status.name))
                }
            }).toString()
    }

    fun decode(stored: String?): List<TrackedContentItem> {
        return try {
            if (stored == null) return emptyList()
            val root = JSONObject(stored)
            if (root.getInt("version") != 1) return emptyList()
            val entries = root.getJSONArray("items")
            val restored = linkedMapOf<String, TrackedContentItem>()
            for (index in 0 until entries.length()) {
                try {
                    val entry = entries.getJSONObject(index)
                    val content = ContentItem(
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
                    if (CatalogId.isValid(content)) {
                        restored[content.id] = TrackedContentItem(content, status)
                    }
                } catch (_: Exception) {
                    // A damaged entry must not prevent other items from being restored.
                }
            }
            restored.values.toList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun JSONObject.nullableString(key: String): String? =
        if (isNull(key)) null else getString(key)
}
