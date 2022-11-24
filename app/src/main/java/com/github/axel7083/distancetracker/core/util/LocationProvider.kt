package com.github.axel7083.distancetracker.core.util

import android.location.Location
import kotlinx.coroutines.flow.Flow

interface LocationProvider {

    fun getLocation(interval: Long): Flow<Location>

    class LocationException(message: String) : Exception(message)
}