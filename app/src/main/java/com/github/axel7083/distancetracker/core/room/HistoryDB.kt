package com.github.axel7083.distancetracker.core.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.github.axel7083.distancetracker.core.room.dao.PlaceDao
import com.github.axel7083.distancetracker.core.room.entities.PlaceEntity

@Database(entities = [
    PlaceEntity::class
], version = 1)
abstract class HistoryDB : RoomDatabase() {
    abstract fun placeDao(): PlaceDao
}