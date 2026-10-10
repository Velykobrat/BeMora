package com.bebetter.bemora.data.repository

import com.bebetter.bemora.domain.model.ContentItem
import com.bebetter.bemora.domain.model.ContentType

data class CatalogSearchResult(
    val items: List<ContentItem> = emptyList(),
    val errors: Map<ContentType, String> = emptyMap()
)

interface CatalogRepository {
    suspend fun search(query: String, type: ContentType?): CatalogSearchResult
    suspend fun getContentDetails(id: String): ContentItem
}
