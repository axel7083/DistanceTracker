package com.github.axel7083.distancetracker.core.service

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.graphics.Point
import android.location.Location
import androidx.annotation.CallSuper
import androidx.lifecycle.lifecycleScope
import com.github.axel7083.distancetracker.core.notification.LocationNotification
import com.github.axel7083.distancetracker.core.util.Convertion
import com.github.axel7083.distancetracker.core.util.Convertion.toDp
import com.github.axel7083.distancetracker.core.util.LocationProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LocationService : PipOverlayService() {
    companion object {
        const val ACTION_START = "actionStart"
        const val ACTION_STOP = "actionStop"
        const val INTERVAL = 10_000L
    }

    @Inject
    lateinit var notification: LocationNotification

    @Inject
    lateinit var location: LocationProvider

    private var locationJob: Job? = null

    private lateinit var startingLocation: Location
    private lateinit var destinationLocation: Location
    private var totalDistance: Float = -1f

    override fun getNotificationId(): Int = LocationNotification.ID

    override fun getForegroundNotification(): Notification = notification.createNotification()

    override fun getInitialWindowSize(): Point = Point(180.toDp(), 50.toDp())

    override fun onServiceRun() {
        println("onServiceRun")
    }

    override fun stopService() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        return when (intent?.action) {
            ACTION_START -> {
                start(intent)
                super.onStartCommand(intent, flags, startId)
            }
            ACTION_STOP -> {
                stopService()
                return Service.START_NOT_STICKY
            }
            else -> {
                throw Exception("Missing action in intent.")
            }
        }
    }

    private fun start(intent: Intent?) {
        if(intent == null)
            throw Exception("intent cannot be null.")
        val endLat = intent.getDoubleExtra("endLat", -1.0)
        val endLon = intent.getDoubleExtra("endLon", -1.0)

        if(endLat == -1.0 || endLon == -1.0)
            throw Exception("endLat and endLon must be defined.")

        destinationLocation = Location("").apply {
            this.latitude = endLat
            this.longitude = endLon
        }
        observe()
    }

    private fun observe() {
        observeLocation()
    }

    private fun observeLocation() {
        locationJob?.cancel()
        locationJob = lifecycleScope.launch {
            location.getLocation(INTERVAL)
                .catch { it.printStackTrace() }
                .collectLatest { location ->

                    println("location $location")
                    if(this@LocationService::startingLocation.isInitialized.not())
                        startingLocation = location

                    val distance = Convertion.distanceInMeter(
                        startLat = location.latitude,
                        startLon = location.longitude,
                        endLat = destinationLocation.latitude,
                        endLon = destinationLocation.longitude
                    )

                    if(totalDistance == -1f)
                        totalDistance = distance

                    pipView.setProgress(100-(distance/totalDistance*100).toInt())

                    val formatted = if(distance > 1500)
                        String.format("%.1fkm",distance/1000)
                    else
                        String.format("%.1fm",distance)

                    pipView.setTitle(formatted)
                    notification.updateContentText(
                        formatted
                    )
                }
        }
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
    }
}