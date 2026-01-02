package com.example.slrry_10

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.slrry_10.repository.LocationRepositoryImpl
import com.example.slrry_10.repository.UserRepoImpl
import com.example.slrry_10.ui.*
import com.example.slrry_10.ui.theme.SLRRY_10Theme
import com.example.slrry_10.viewmodel.StartRunViewModel
import com.google.android.gms.location.*
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap

class ReadyToStartActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    // Compose-observable state: permission changes will recompose and start/stop GPS updates.
    private var hasLocationPermission by mutableStateOf(false)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            // Required for MapLibre MapView to render tiles
            MapLibre.getInstance(this)
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

            locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
                .setMinUpdateIntervalMillis(1000)
                .setMaxUpdateDelayMillis(5000)
                .build()
            
            requestLocationPermission()
            
            enableEdgeToEdge()
            setContent {
                SLRRY_10Theme {
                    ReadyToStartScreen(
                        fusedLocationClient = fusedLocationClient,
                        locationRequest = locationRequest,
                        hasLocationPermission = hasLocationPermission,
                        onPermissionGranted = { hasLocationPermission = true },
                        onStartRun = {
                            // Navigate to RunningActivity with smooth transition
                            val intent = Intent(this@ReadyToStartActivity, RunningActivity::class.java)
                            startActivity(intent)
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                            finish()
                        }
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun requestLocationPermission() {
        hasLocationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

        if (!hasLocationPermission) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
}

@Composable
fun ReadyToStartScreen(
    fusedLocationClient: FusedLocationProviderClient,
    locationRequest: LocationRequest,
    hasLocationPermission: Boolean,
    onPermissionGranted: () -> Unit,
    onStartRun: () -> Unit,
    viewModel: StartRunViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return StartRunViewModel(
                    UserRepoImpl(),
                    LocationRepositoryImpl()
                ) as T
            }
        }
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var isPressing by remember { mutableStateOf(false) }
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var locationCallback by remember { mutableStateOf<LocationCallback?>(null) }
    
    val vibrator = remember {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    // Handle long press (3 seconds) to start run
    LaunchedEffect(isPressing) {
        if (isPressing && !uiState.isTracking) {
            kotlinx.coroutines.delay(3000) // 3 seconds
            if (isPressing && !uiState.isTracking) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(200)
                }
                viewModel.startTracking()
                onStartRun()
                isPressing = false
            }
        }
    }

    // Get initial location
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission && uiState.currentLocation == null) {
            kotlinx.coroutines.delay(300)
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        val locationModel = com.example.slrry_10.model.LocationModel(
                            latitude = it.latitude,
                            longitude = it.longitude,
                            altitude = it.altitude,
                            accuracy = it.accuracy
                        )
                        viewModel.updateLocation(locationModel)
                    }
                }
            } catch (e: SecurityException) {
                // Silently handle
            }
        }
    }

    // Keep updating device location on the ready screen too (so map centers & dot moves)
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            val callback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.lastLocation?.let { location ->
                        val locationModel = com.example.slrry_10.model.LocationModel(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            altitude = location.altitude,
                            accuracy = location.accuracy,
                            timestamp = location.time
                        )
                        viewModel.updateLocation(locationModel)
                    }
                }
            }
            locationCallback = callback
            try {
                fusedLocationClient.requestLocationUpdates(locationRequest, callback, context.mainLooper)
            } catch (_: SecurityException) {
                // Ignore
            }
        } else {
            locationCallback?.let { cb ->
                try {
                    fusedLocationClient.removeLocationUpdates(cb)
                } catch (_: Exception) {}
            }
            locationCallback = null
        }
    }

    // Center map on current location when available
    LaunchedEffect(uiState.currentLocation, mapLibreMap) {
        val loc = uiState.currentLocation
        val map = mapLibreMap
        if (loc != null && map != null) {
            try {
                val cameraPosition = CameraPosition.Builder()
                    .target(LatLng(loc.latitude, loc.longitude))
                    .zoom(16.0)
                    .build()
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 400)
            } catch (_: Exception) {
                // Ignore camera errors
            }
        }
    }

    ReadyToStartScreenBody(
        uiState = uiState,
        hasLocationPermission = hasLocationPermission,
        onMapReady = { map -> mapLibreMap = map },
        onStartPress = { isPressing = true },
        onStartRelease = { isPressing = false }
    )
}

@Composable
fun ReadyToStartScreenBody(
    uiState: com.example.slrry_10.viewmodel.StartRunUiState,
    hasLocationPermission: Boolean,
    onMapReady: (MapLibreMap) -> Unit,
    onStartPress: () -> Unit,
    onStartRelease: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Map - fills entire screen
        MapViewComponent(
            mapView = null,
            mapLibreMap = null,
            uiState = uiState,
            onMapReady = onMapReady,
            showMap = true
        )

        // Top Status Bar - aligned to top
        TopStatusBar(
            hasLocationPermission = hasLocationPermission,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(2f)
        )

        // Bottom Metrics and Start Button - aligned to bottom
        BottomMetricsAndButton(
            uiState = uiState,
            onStartPress = onStartPress,
            onStartRelease = onStartRelease,
            showStartButton = true,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(2f)
        )
    }
}

@Preview(showBackground = true, name = "Ready To Start Screen", showSystemUi = true, device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=portrait")
@Composable
fun ReadyToStartScreenPreview() {
    SLRRY_10Theme {
        ReadyToStartScreenBody(
            uiState = com.example.slrry_10.viewmodel.StartRunUiState(
                isTracking = false,
                currentLocation = null,
                currentSession = null,
                capturedAreas = emptyList(),
                searchResults = emptyList()
            ),
            hasLocationPermission = true,
            onMapReady = { },
            onStartPress = {},
            onStartRelease = {}
        )
    }
}
