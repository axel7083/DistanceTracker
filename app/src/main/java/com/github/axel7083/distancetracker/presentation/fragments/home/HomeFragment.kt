package com.github.axel7083.distancetracker.presentation.fragments.home

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.preference.PreferenceManager
import com.github.axel7083.distancetracker.R
import com.github.axel7083.distancetracker.core.api.ApiClient
import com.github.axel7083.distancetracker.core.api.data.Place
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
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

        setupMap()

        /*binding.start.setOnClickListener {
            Intent(context, LocationService::class.java).apply {
                action = LocationService.ACTION_START
                requireActivity().startService(this)
            }
        }

        binding.stop.setOnClickListener {
            Intent(context, LocationService::class.java).apply {
                action = LocationService.ACTION_STOP
                requireActivity().startService(this)
            }
        }*/

        /*binding.start.setOnClickListener {
            Intent(context, LocationService::class.java).apply {
                action = LocationService.ACTION_START
                requireActivity().startService(this)
            }
        }
        binding.stop.setOnClickListener {
            Intent(context, LocationService::class.java).apply {
                action = LocationService.ACTION_STOP
                requireActivity().startService(this)
            }
        }

        binding.search.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val response = apiClient.client.searchPlace("avenue jeanne d'arc Montréal")
                assert(response.isSuccessful)
                val body = response.body() ?: throw Exception("body null")
                println("found ${body.size} place(s)")
                for(place in body) {
                    println("place ${place.display_name}")
                }
            }
        }*/
    }

    private fun setupMap() {
        requireContext().apply {
            Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        }

        binding.map.setTileSource(TileSourceFactory.MAPNIK)
        binding.map.setMultiTouchControls(true)

        val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), binding.map)
        locationOverlay.enableMyLocation();
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

    private fun displayOverlay(items: List<OverlayItem>) {
        if(this::overlay.isInitialized)
            binding.map.overlays.remove(overlay)

        //the overlay
        val tempOverlay = ItemizedOverlayWithFocus(items, object:
            ItemizedIconOverlay.OnItemGestureListener<OverlayItem> {
            override fun onItemSingleTapUp(index:Int, item:OverlayItem):Boolean {
                println("Clicked")

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