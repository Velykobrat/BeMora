package com.bebetter.bemora.data.mapper

import com.bebetter.bemora.data.remote.dto.BookDto
import com.bebetter.bemora.data.remote.dto.BookWorkDto
import com.bebetter.bemora.data.remote.dto.GameDto
import com.bebetter.bemora.domain.model.CatalogId
import com.bebetter.bemora.domain.model.ContentItem
import com.bebetter.bemora.domain.model.ContentType

fun BookDto.toContentItemOrNull(): ContentItem? {
    val workId = key?.removePrefix("/works/") ?: return null
    val item = ContentItem(
        id = "openlibrary_book_" + workId,
        title = title?.takeIf { it.isNotBlank() } ?: return null,
        subtitle = authors?.filterNotNull()?.filter { it.isNotBlank() }?.joinToString(", ")?.takeIf { it.isNotBlank() },
        imageUrl = bookCoverUrl(coverId),
        releaseYear = year,
        type = ContentType.BOOK
    )
    return item.takeIf { CatalogId.isValid(it) }
}

fun BookWorkDto.withSummary(summary: ContentItem): ContentItem {
    val text = description?.let {
        when {
            it.isJsonPrimitive && it.asJsonPrimitive.isString -> it.asString
            it.isJsonObject -> it.asJsonObject.get("value")?.takeIf { value ->
                value.isJsonPrimitive && value.asJsonPrimitive.isString
            }?.asString
            else -> null
        }
    }
    return summary.copy(
        title = title?.takeIf { it.isNotBlank() } ?: summary.title,
        description = text,
        imageUrl = summary.imageUrl ?: bookCoverUrl(covers?.filterNotNull()?.firstOrNull { it > 0 })
    )
}

private fun bookCoverUrl(id: Long?): String? = id?.takeIf { it > 0 }?.let {
    "https://covers.openlibrary.org/b/id/" + it + "-L.jpg?default=false"
}

fun GameDto.toContentItemOrNull(): ContentItem? {
    val item = ContentItem(
        id = "rawg_game_" + (id ?: return null),
        title = name?.takeIf { it.isNotBlank() } ?: return null,
        subtitle = platforms?.mapNotNull { it.platform?.name }?.distinct()?.joinToString(", ")?.takeIf { it.isNotBlank() },
        description = description ?: descriptionHtml?.replace(Regex("<[^>]*>"), " ")
            ?.replace("&amp;", "&")?.replace("&quot;", "\"")?.replace("&#39;", "'")
            ?.replace("&lt;", "<")?.replace("&gt;", ">")?.replace("&nbsp;", " ")?.trim(),
        imageUrl = imageUrl,
        releaseYear = released?.take(4)?.toIntOrNull(),
        // RAWG uses a five-point scale; normalize to the same ten-point scale as TMDB.
        rating = rating?.takeIf { it.isFinite() && it > 0 && it <= 5 && ratingsCount != 0 }?.times(2),
        type = ContentType.GAME
    )
    return item.takeIf { CatalogId.isValid(it) }
}
