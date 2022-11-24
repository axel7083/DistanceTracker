package com.github.axel7083.distancetracker.core.api

import okhttp3.OkHttpClient
import org.osmdroid.util.BoundingBox
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiClient(
    private val BASE_URL: String,
    private val httpClient : OkHttpClient
) {
    val client: com.github.axel7083.distancetracker.core.api.ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create()) // Gson converter
            .build().create(com.github.axel7083.distancetracker.core.api.ApiService::class.java)
    }

    companion object {
        @JvmStatic
        fun formatViewBoxQuery(box: BoundingBox): String {
            println("box ${box.toString()}")
            return "${box.lonEast},${box.latNorth},${box.lonWest},${box.latSouth}"
        }
    }
}