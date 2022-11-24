package com.github.axel7083.distancetracker.core.api

import com.github.axel7083.distancetracker.core.api.data.Place
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("search")
    suspend fun searchPlace(
        @Query("q") query: String,
        @Query("viewbox") box: String,
        @Query("bounded") bounded: Int = 1,
        @Query("format") format: String = "jsonv2"
    ): Response<List<Place>>
}