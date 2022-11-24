package com.github.axel7083.distancetracker.injection.app

import android.app.Application
import com.github.axel7083.distancetracker.core.notification.LocationNotification
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {

    @Inject
    lateinit var notification: LocationNotification

    override fun onCreate() {
        super.onCreate()

        notification.createChannel()
    }
}