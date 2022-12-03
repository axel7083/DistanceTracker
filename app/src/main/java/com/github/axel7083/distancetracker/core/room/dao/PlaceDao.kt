package com.github.axel7083.distancetracker.core.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.IGNORE
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Update
import com.github.axel7083.distancetracker.core.room.entities.PlaceEntity

@Dao
interface PlaceDao {
    @Query("SELECT * FROM placeentity order by timestamp desc")
    fun getAll(): List<PlaceEntity>

    @Update
    fun update(place: PlaceEntity)

    @Insert(onConflict = REPLACE)
    fun insert(place: PlaceEntity)
}