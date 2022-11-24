package com.github.axel7083.distancetracker.injection.bind

import com.github.axel7083.distancetracker.core.util.AndroidLocationProvider
import com.github.axel7083.distancetracker.core.util.LocationProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ApplicationModule {

    @Binds
    @Singleton
    abstract fun bindLocationProvider(androidLocationProvider: AndroidLocationProvider): LocationProvider
}