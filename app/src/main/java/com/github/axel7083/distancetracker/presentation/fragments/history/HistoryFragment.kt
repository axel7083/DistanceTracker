package com.github.axel7083.distancetracker.presentation.fragments.history

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.axel7083.distancetracker.R
import com.github.axel7083.distancetracker.core.service.LocationService
import com.github.axel7083.distancetracker.databinding.FragmentHistoryBinding
import com.github.axel7083.distancetracker.databinding.FragmentHomeBinding
import com.github.axel7083.distancetracker.presentation.adapters.PlaceAdapter
import com.github.axel7083.distancetracker.presentation.fragments.home.HomeViewModel

class HistoryFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel

    private var _binding: FragmentHistoryBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var adapter: PlaceAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.placesList.layoutManager = LinearLayoutManager(requireContext())
        activity?.let {
            viewModel.placesHistory.observe(it) { places ->
                if(this::adapter.isInitialized)
                    adapter.updateDataSet(places)
                else
                    binding.placesList.adapter = PlaceAdapter(places) {
                        Intent(context, LocationService::class.java).apply {
                            this.putExtra("endLat", it.lat)
                            this.putExtra("endLon",it.lon)
                            action = LocationService.ACTION_START
                            requireActivity().startService(this)
                        }
                    }
            }
        }


    }
}