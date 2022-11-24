package com.github.axel7083.distancetracker.core.util

import android.content.res.Resources
import android.location.Location

object Convertion {
    @JvmStatic
    fun Int.toDp(): Int {
        return (this * Resources.getSystem().displayMetrics.density).toInt()
    }

    @JvmStatic
    fun distanceInMeter(startLat: Double, startLon: Double, endLat: Double, endLon: Double): Float {
        var results = FloatArray(1)
        Location.distanceBetween(startLat,startLon,endLat,endLon,results)
        return results[0]
    }
}