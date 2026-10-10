package com.bebetter.bemora.domain.model

object CatalogId {
    val supportedTypes = listOf(ContentType.MOVIE, ContentType.BOOK, ContentType.GAME)

    fun typeOf(id: String?): ContentType? = when {
        id == null -> null
        id.startsWith("tmdb_movie_") && positiveNumber(id.removePrefix("tmdb_movie_")) -> ContentType.MOVIE
        id.startsWith("openlibrary_book_") &&
            Regex("OL[1-9][0-9]*W").matches(id.removePrefix("openlibrary_book_")) -> ContentType.BOOK
        id.startsWith("rawg_game_") && positiveNumber(id.removePrefix("rawg_game_")) -> ContentType.GAME
        else -> null
    }

    fun isValid(item: ContentItem): Boolean = typeOf(item.id) == item.type &&
        item.title.isNotBlank() && (item.rating == null || item.rating.isFinite())

    private fun positiveNumber(value: String): Boolean = value.isNotEmpty() &&
        value.all { it in '0'..'9' } && (value.toIntOrNull() ?: 0) > 0
}

val ContentType.label: String
    get() = when (this) {
        ContentType.MOVIE -> "Movies"
        ContentType.BOOK -> "Books"
        ContentType.GAME -> "Games"
        else -> name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }
    }
