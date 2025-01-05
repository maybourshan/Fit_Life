package com.example.fit_life

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.findNavController
import androidx.room.Room
import com.example.fit_life.data.models.Gym
import com.example.fit_life.viewmodel.GymListViewModel
import com.example.fit_life.database.FitLifeDatabase
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var gymList: MutableList<Gym>
    private lateinit var locationCallback: LocationCallback
    private val gymListViewModel: GymListViewModel by viewModels()
    private lateinit var database: FitLifeDatabase
    private var lastLatitude: Double? = null
    private var lastLongitude: Double? = null
    private val gymIdsSet = mutableSetOf<String>() // Set to keep track of unique gym IDs
    var hasShownErrorToast: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set up the toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.hide()

        // Set up navigation
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navController.addOnDestinationChangedListener { _, _, _ ->
            supportActionBar?.let {
                it.title = ""
            }
        }

        // Initialize the location client and database
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        database = Room.databaseBuilder(
            applicationContext,
            FitLifeDatabase::class.java, "fitlife-database"
        ).build()

        gymList = mutableListOf()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    if (location != null) {
                        val latitude = location.latitude
                        val longitude = location.longitude
                        if (lastLatitude == null || lastLongitude == null ||
                            (Math.abs(latitude - lastLatitude!!) > 0.01 || Math.abs(longitude - lastLongitude!!) > 0.01)) {
                            lastLatitude = latitude
                            lastLongitude = longitude
                            lifecycleScope.launch {
                                searchNearbyGyms(latitude, longitude)
                            }
                        }
                    }
                }
            }
        }
        checkLocationPermission()
    }

    // Checks if the location permission is granted and requests it if not.
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            startLocationUpdates()
        }
    }

    // Handles the result of the permission request.
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                startLocationUpdates()
            } else {
                Toast.makeText(this, getString(R.string.location_permission_needed), Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Starts location updates if the permission is granted.
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        } else {
            Toast.makeText(this, getString(R.string.location_permissions_not_granted), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates()
        }
    }

    // Searches for nearby gyms based on the user's current location.
    private suspend fun searchNearbyGyms(latitude: Double, longitude: Double) {
        val distance = 0.045 // Approximately 5 km in degrees
        val minLat = latitude - distance
        val maxLat = latitude + distance
        val minLon = longitude - distance
        val maxLon = longitude + distance
        val queries = listOf(
            getString(R.string.query_gym),
            getString(R.string.query_fitness),
            getString(R.string.query_sports),
            getString(R.string.query_kosher),
            getString(R.string.query_gym_room),
            getString(R.string.query_sport_center),
            getString(R.string.query_fitness_center),
            getString(R.string.query_trainings),
            getString(R.string.query_pilates),
            getString(R.string.query_pilates_he),
            getString(R.string.query_yoga),
            getString(R.string.query_equipment),
            getString(R.string.query_outdoor_gym),
            getString(R.string.query_outdoor_fitness_he),
            getString(R.string.query_sport_equipment),
            getString(R.string.query_sport_area),
            getString(R.string.query_outdoor_fitness_equipment)
        )

        withContext(Dispatchers.IO) {
            queries.forEach { query ->
                val url = "https://nominatim.openstreetmap.org/search?format=json&q=$query&limit=10&bounded=1&viewbox=$minLon,$minLat,$maxLon,$maxLat"
                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()

                try {
                    val response = client.newCall(request).execute()
                    if (!response.isSuccessful) {
                        runOnUiThread {
                            if (!hasShownErrorToast) {
                                Snackbar.make(findViewById(android.R.id.content), getString(R.string.server_error, response.message), Snackbar.LENGTH_LONG).show()
                                hasShownErrorToast = true
                            }
                        }
                        return@forEach
                    }

                    response.body?.let {
                        val responseString = it.string()
                        try {
                            val jsonResponse = JSONArray(responseString)
                            val newGyms = mutableListOf<Gym>()
                            for (i in 0 until jsonResponse.length()) {
                                val result = jsonResponse.getJSONObject(i)
                                val name = result.getString("display_name")
                                val lat = result.getDouble("lat")
                                val lon = result.getDouble("lon")
                                val address = result.getString("display_name")
                                if (!gymIdsSet.contains(name + address) && !gymList.any { it.name == name && it.address == address }) {
                                    newGyms.add(Gym(name = name, latitude = lat, longitude = lon, address = address))
                                    gymIdsSet.add(name + address)
                                }
                            }
                            gymList.addAll(newGyms)
                            gymList = gymList.distinctBy { it.name + it.address }.toMutableList() // Remove duplicates by name and address
                            saveGymsToDatabase(gymList)
                            runOnUiThread {
                                gymListViewModel.setGyms(gymList)
                            }
                        } catch (e: JSONException) {
                            runOnUiThread {
                                Snackbar.make(findViewById(android.R.id.content), getString(R.string.parse_error, e.message), Snackbar.LENGTH_LONG).show()
                            }
                        }
                    }
                } catch (e: IOException) {
                    runOnUiThread {
                        Snackbar.make(findViewById(android.R.id.content), getString(R.string.network_error), Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    // Saves the list of gyms to the database.
    private fun saveGymsToDatabase(gyms: List<Gym>) {
        lifecycleScope.launch(Dispatchers.IO) {
            database.gymDao().deleteAllGyms() // Deletes all existing records
            database.gymDao().insertAll(*gyms.toTypedArray())
        }
    }

    // Retrieves the list of gyms from the database.
    fun getGymList(): LiveData<List<Gym>> {
        return database.gymDao().getAllGyms()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
