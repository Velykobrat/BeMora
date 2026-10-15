package com.bebetter.bemora.data.repository

import com.bebetter.bemora.domain.model.ContentItem
import com.bebetter.bemora.domain.model.ContentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class CatalogSearchResult(
    val items: List<ContentItem> = emptyList(),
    val errors: Map<ContentType, String> = emptyMap()
)

data class CatalogSearchProgress(
    val items: List<ContentItem> = emptyList(),
    val errors: Map<ContentType, String> = emptyMap(),
    val loadingTypes: Set<ContentType> = emptySet()
)

interface CatalogRepository {
    suspend fun search(query: String, type: ContentType?): CatalogSearchResult
    fun searchUpdates(query: String, type: ContentType?): Flow<CatalogSearchProgress> = flow {
        val result = search(query, type)
        emit(CatalogSearchProgress(result.items, result.errors))
    }
    suspend fun getContentDetails(id: String): ContentItem
}

interface DiscoverRepository {
    suspend fun getDiscoverItems(type: ContentType): List<ContentItem>
}
