package com.example.slrry_10

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.example.slrry_10.repository.LocationRepositoryImpl
import com.example.slrry_10.repository.FirebaseUserRepoImpl
import com.example.slrry_10.ui.MapViewComponent
import com.example.slrry_10.ui.MapsHubScreen
import com.example.slrry_10.ui.theme.SLRRY_10Theme
import com.example.slrry_10.model.LocationModel
import com.example.slrry_10.viewmodel.StartRunViewModel
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.location.FusedLocationProviderClient
import org.maplibre.android.maps.MapLibreMap

class MapsHubActivity : ComponentActivity() {
    private lateinit var vm: StartRunViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        vm = ViewModelProvider(
            this,
            object : androidx.lifecycle.ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return StartRunViewModel(FirebaseUserRepoImpl(), LocationRepositoryImpl()) as T
                }
            }
        )[StartRunViewModel::class.java]

        setContent {
            SLRRY_10Theme {
                val uiState by vm.uiState.collectAsState()

                var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }

                // Initialize demo owners and default tab when opening from Dashboard.
                LaunchedEffect(Unit) { vm.openMaps() }

                Box(modifier = Modifier.fillMaxSize()) {
                    MapViewComponent(
                        mapView = null,
                        mapLibreMap = mapLibreMap,
                        uiState = uiState,
                        onMapReady = { mapLibreMap = it },
                        showMap = true
                    )

                    MapsHubScreen(
                        uiState = uiState,
                        viewModel = vm,
                        mapLibreMap = mapLibreMap,
                        onBack = { finish() }
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        startLocationUpdates()
    }

    override fun onStop() {
        super.onStop()
        stopLocationUpdates()
    }

    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    private fun startLocationUpdates() {
        if (!hasLocationPermission()) return

        // Quick first fix to center the map immediately.
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    vm.updateLocation(LocationModel(latitude = loc.latitude, longitude = loc.longitude, timestamp = System.currentTimeMillis()))
                }
            }
        } catch (_: Exception) {}

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1500L)
            .setMinUpdateIntervalMillis(800L)
            .build()

        val cb = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                vm.updateLocation(LocationModel(latitude = loc.latitude, longitude = loc.longitude, timestamp = System.currentTimeMillis()))
            }
        }
        locationCallback = cb

        try {
            fusedLocationClient.requestLocationUpdates(request, cb, mainLooper)
        } catch (_: SecurityException) {
            // Permission revoked mid-session.
        }
    }

    private fun stopLocationUpdates() {
        val cb = locationCallback ?: return
        try {
            fusedLocationClient.removeLocationUpdates(cb)
        } catch (_: Exception) {}
        locationCallback = null
    }
}


