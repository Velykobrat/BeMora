package com.bebetter.bemora.data.repository

import java.io.IOException
import retrofit2.HttpException

class CatalogConfigurationException(message: String) : IllegalStateException(message)

// Avoid displaying request URLs or provider credentials from raw exception messages.
fun catalogErrorMessage(exception: Exception): String = when (exception) {
    is CatalogConfigurationException -> exception.message ?: "Catalog is not configured."
    is HttpException -> when (exception.code()) {
        401, 403 -> "Catalog access was denied. Check its API credentials."
        404 -> "This item is no longer available in the catalog."
        429 -> "Catalog request limit reached. Please try again later."
        else -> "Catalog is temporarily unavailable. Please try again."
    }
    is IOException -> "Unable to reach the catalog. Check your connection and try again."
    else -> "Unable to load catalog data. Please try again."
}
