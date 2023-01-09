package com.github.axel7083.distancetracker.presentation.fragments.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.github.axel7083.distancetracker.R
import com.github.axel7083.distancetracker.core.api.ApiClient
import com.github.axel7083.distancetracker.core.api.data.Place
import com.github.axel7083.distancetracker.core.room.entities.PlaceEntity
import com.github.axel7083.distancetracker.core.service.LocationService
import com.github.axel7083.distancetracker.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.OverlayItem
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.views.util.constants.OverlayConstants
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {
    @Inject
    lateinit var apiClient: ApiClient

    private lateinit var viewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null

    private lateinit var menuItem: MenuItem

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var overlay: Overlay
    private lateinit var historyOverlay: Overlay

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        menuItem = binding.toolbar.menu.findItem(R.id.action_search)
        val searchView = menuItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String?): Boolean {
                if(s == null)
                    return false
                search(s, binding.map.boundingBox)
                return false
            }

            override fun onQueryTextChange(s: String?): Boolean = true
        })

        binding.toolbar.setOnMenuItemClickListener {
            println("itemId ${it.itemId}")
            println("itemId ${R.id.action_history}")
            when(it.itemId) {
                R.id.action_history -> {
                    findNavController().navigate(R.id.action_homeFragment_to_historyFragment)
                    true
                }
                else -> false
            }
        }

        activity?.let {
            viewModel.placesHistory.observe(it) {
                displayHistory()
            }
        }

        setupMap()

        viewModel.fetchPlaces()
    }

    private fun setupMap() {
        requireContext().apply {
            Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        }

        binding.map.setTileSource(TileSourceFactory.MAPNIK)
        binding.map.setMultiTouchControls(true)

        val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), binding.map)
        locationOverlay.enableMyLocation()

        locationOverlay.enableAutoStop = true
        locationOverlay.enableFollowLocation()

        binding.map.overlays.add(locationOverlay)

        val mapController = binding.map.controller
        mapController.setZoom(9.5)

        val startPoint = GeoPoint(48.8583, 2.2944)
        mapController.setCenter(startPoint)
    }

    fun search(query: String, box: BoundingBox) {
        binding.progressBar.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            val response = apiClient.client.searchPlace(
                query = query,
                box = ApiClient.formatViewBoxQuery(box)
            )

            if(response.isSuccessful.not())
                return@launch onError("The request failed.")

            val places = response.body() ?: return@launch onError("The response body is empty.")

            if(places.isEmpty())
                return@launch onError("Nothing found.")

            onSuccess(places)
        }
    }

    private suspend fun onError(message: String) {
        withContext(Dispatchers.Main) {
            binding.progressBar.visibility = View.GONE
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

            if(this@HomeFragment::overlay.isInitialized) {
                binding.map.overlays.remove(overlay)
                binding.map.invalidate()
            }
        }
    }

    private suspend fun onSuccess(data: List<Place>) {
        withContext(Dispatchers.Main) {
            binding.progressBar.visibility = View.GONE

            val items = data.map { OverlayItem(
                it.osm_id.toString(),
                it.display_name,
                it.type,
                GeoPoint(it.lat.toDouble(), it.lon.toDouble()))
            }
            displayOverlay(items)
        }
    }

    private fun displayHistory() {
        val items: List<OverlayItem> = viewModel.placesHistory.value?.map {
            OverlayItem(it.name, it.name, GeoPoint(it.lat, it.lon))
        } ?: return

        displayOverlay(items)
    }

    private fun displayOverlay(items: List<OverlayItem>) {
        if(this::overlay.isInitialized)
            binding.map.overlays.remove(overlay)

        //the overlay
        val tempOverlay = ItemizedOverlayWithFocus(items, object:
            ItemizedIconOverlay.OnItemGestureListener<OverlayItem> {
            override fun onItemSingleTapUp(index:Int, item:OverlayItem):Boolean {

                viewModel.save(PlaceEntity(
                    name = item.title,
                    lat = item.point.latitude,
                    lon = item.point.longitude,
                    timestamp = System.currentTimeMillis()
                ))
                Intent(context, LocationService::class.java).apply {
                    this.putExtra("endLat", item.point.latitude)
                    this.putExtra("endLon", item.point.longitude)
                    action = LocationService.ACTION_START
                    requireActivity().startService(this)
                }
                return true
            }
            override fun onItemLongPress(index:Int, item:OverlayItem):Boolean {
                return false
            }
        }, context)
        tempOverlay.setFocusItemsOnTap(true)

        binding.map.overlays.add(tempOverlay)
        overlay = tempOverlay
        binding.map.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}