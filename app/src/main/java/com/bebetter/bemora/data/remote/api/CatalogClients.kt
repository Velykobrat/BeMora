package com.bebetter.bemora.data.remote.api

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object CatalogClients {
    private fun client(baseUrl: String): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                chain.proceed(chain.request().newBuilder()
                    .header("User-Agent", "BeMora (https://github.com/Velykobrat/BeMora)")
                    .header("Accept", "application/json").build())
            }.build())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val books: OpenLibraryApi by lazy { client("https://openlibrary.org/").create(OpenLibraryApi::class.java) }
    val games: RawgApi by lazy { client("https://api.rawg.io/api/").create(RawgApi::class.java) }

    private val bookMutex = Mutex()
    private var lastBookRequestNanos = 0L

    // Respect the default Open Library limit of one request per second across screens.
    suspend fun <T> bookRequest(block: suspend () -> T): T = bookMutex.withLock {
        val remaining = 1_000_000_000L - (System.nanoTime() - lastBookRequestNanos)
        if (lastBookRequestNanos != 0L && remaining > 0) delay((remaining + 999_999) / 1_000_000)
        lastBookRequestNanos = System.nanoTime()
        block()
    }
}
