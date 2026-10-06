package com.bebetter.bemora.domain.model

data class ContentItem(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val releaseYear: Int? = null,
    val rating: Double? = null,
    val type: ContentType
)