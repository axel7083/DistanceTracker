package com.github.axel7083.distancetracker.core.room.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(indices = [Index(value = ["name"], unique = true)])
data class PlaceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val name: String,
    val lat: Double,
    val lon: Double,
    val timestamp: Long,
): java.io.Serializable
