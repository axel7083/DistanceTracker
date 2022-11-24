package com.github.axel7083.distancetracker.core.api.data

data class Place(
    val place_id: Long,
    val license: String,
    val osm_type: String,
    val osm_id: Long,
    val boundingbox: List<String>,
    val lat: String,
    val lon: String,
    val display_name: String,
    val place_rank: Long,
    val category: String,
    val type: String,
    val importance: Float
)