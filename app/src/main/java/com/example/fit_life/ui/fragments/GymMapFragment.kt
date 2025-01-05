package com.example.fit_life.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.fit_life.R
import com.example.fit_life.data.models.Gym
import com.example.fit_life.databinding.FragmentGymMapBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class GymMapFragment : Fragment() {

    // View binding for the fragment layout
    private var _binding: FragmentGymMapBinding? = null
    private val binding get() = _binding!!

    // MapView to display the map
    private lateinit var map: MapView

    // Array to hold the list of gyms
    private lateinit var gyms: Array<Gym>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout and set up the binding
        _binding = FragmentGymMapBinding.inflate(inflater, container, false)

        // Load the configuration for the map
        Configuration.getInstance().load(requireContext(), androidx.preference.PreferenceManager.getDefaultSharedPreferences(requireContext()))

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the map and set the tile source
        map = binding.map
        map.setTileSource(TileSourceFactory.MAPNIK)

        // Retrieve the list of gyms from the arguments
        gyms = arguments?.getParcelableArray("gyms") as? Array<Gym> ?: arrayOf()

        // Add markers to the map for each gym
        if (gyms.isNotEmpty()) {
            for (gym in gyms) {
                val marker = Marker(map)
                marker.position = GeoPoint(gym.latitude, gym.longitude)
                marker.title = gym.name
                map.overlays.add(marker)
            }

            // Center the map on the first gym
            val firstGym = gyms[0]
            map.controller.setZoom(15.0)
            map.controller.setCenter(GeoPoint(firstGym.latitude, firstGym.longitude))
        }

        // Set the click listener for the back button
        binding.buttonBackToList.setOnClickListener {
            findNavController().navigate(R.id.action_gymMapFragment_to_gymListFragment)
        }

        // Check location permission
        checkLocationPermission()
    }

    // Function to check location permission
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            // Use coroutine to handle location updates if needed
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    startLocationUpdates()
                }
            }
        }
    }

    // Function to start location updates
    private fun startLocationUpdates() {
        // Handle location updates if needed
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clear the binding object
        _binding = null
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}
