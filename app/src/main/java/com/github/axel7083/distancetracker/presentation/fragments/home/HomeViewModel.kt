package com.github.axel7083.distancetracker.presentation.fragments.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.axel7083.distancetracker.core.room.HistoryDB
import com.github.axel7083.distancetracker.core.room.entities.PlaceEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val historyDB: HistoryDB) : ViewModel() {

    private val _placesHistory = MutableLiveData<List<PlaceEntity>>()
    val placesHistory: LiveData<List<PlaceEntity>> = _placesHistory

    fun save(placeEntity: PlaceEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            historyDB.placeDao().insert(placeEntity)
        }
    }

    fun fetchPlaces() {
        CoroutineScope(Dispatchers.IO).launch {
            val places = historyDB.placeDao().getAll()
            withContext(Dispatchers.IO) {
                _placesHistory.postValue(places)
            }
        }
    }

    // TODO: Implement the ViewModel
}