package com.bebetter.bemora.data.remote.api

import com.bebetter.bemora.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object TmdbClient {

    private const val BASE_URL =
        "https://api.themoviedb.org/3/"

    private val okHttpClient =
        OkHttpClient.Builder()
            .addInterceptor { chain ->

                val request = chain.request()
                    .newBuilder()
                    .addHeader(
                        "Authorization",
                        "Bearer ${BuildConfig.TMDB_TOKEN}"
                    )
                    .addHeader(
                        "accept",
                        "application/json"
                    )
                    .build()

                chain.proceed(request)
            }
            .build()

    val api: TmdbApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
            .create(TmdbApi::class.java)
    }
}