package com.github.axel7083.distancetracker.injection.provide

import android.app.Application
import android.app.NotificationManager
import android.location.LocationManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import github.axel7083.distancetracker.core.api.ApiClient
import github.axel7083.distancetracker.core.notification.LocationNotification
import github.axel7083.distancetracker.core.util.AndroidLocationProvider
import github.axel7083.distancetracker.core.util.Constants
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {

    @Provides
    @Singleton
    fun provideLocationManager(application: Application): LocationManager =
        application.getSystemService(LocationManager::class.java)

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(application: Application): FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application)

    @Provides
    @Singleton
    fun provideAndroidLocationProvider(
        application: Application,
        locationManager: LocationManager,
        fusedLocationProviderClient: FusedLocationProviderClient,
    ): AndroidLocationProvider = AndroidLocationProvider(
        context = application,
        manager = locationManager,
        client = fusedLocationProviderClient
    )

    @Provides
    @Singleton
    fun provideHttpClient() = OkHttpClient
        .Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideApiClient(okHttpClient: OkHttpClient): ApiClient = ApiClient(Constants.BASE_URL, okHttpClient)

    @Provides
    @Singleton
    fun provideNotificationManager(application: Application): NotificationManager =
        application.getSystemService(NotificationManager::class.java)

    @Provides
    @Singleton
    fun provideLocationNotification(
        application: Application,
        notificationManager: NotificationManager,
    ): LocationNotification = LocationNotification(
        context = application.baseContext,
        manager = notificationManager
    )
}