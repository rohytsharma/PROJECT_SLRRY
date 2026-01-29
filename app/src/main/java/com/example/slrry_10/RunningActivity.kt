package com.example.slrry_10

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Box
import androidx.core.content.ContextCompat
import com.example.slrry_10.viewmodel.StartRunUiState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.slrry_10.repository.LocationRepositoryImpl
import com.example.slrry_10.repository.FirebaseUserRepoImpl
import com.example.slrry_10.ui.*
import com.example.slrry_10.ui.RunningBottomButtons
import com.example.slrry_10.ui.theme.SLRRY_10Theme
import com.example.slrry_10.viewmodel.StartRunViewModel
import com.google.android.gms.location.*
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap

class RunningActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    // Compose-observable state: permission changes will recompose and start/stop GPS updates.
    private var hasLocationPermission by mutableStateOf(false)
    private var locationCallback: LocationCallback? = null

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
                    RunningScreen(
                        fusedLocationClient = fusedLocationClient,
                        locationRequest = locationRequest,
                        hasLocationPermission = hasLocationPermission,
                        onPermissionGranted = { hasLocationPermission = true }
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationCallback?.let {
            try {
                fusedLocationClient.removeLocationUpdates(it)
            } catch (e: Exception) {
                // Ignore
            }
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
fun RunningScreen(
    fusedLocationClient: FusedLocationProviderClient,
    locationRequest: LocationRequest,
    hasLocationPermission: Boolean,
    onPermissionGranted: () -> Unit,
    viewModel: StartRunViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return StartRunViewModel(
                    FirebaseUserRepoImpl(),
                    LocationRepositoryImpl()
                ) as T
            }
        }
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var locationCallback by remember { mutableStateOf<LocationCallback?>(null) }
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    val mapRef by rememberUpdatedState(mapLibreMap)

    // Start tracking when screen loads
    LaunchedEffect(Unit) {
        viewModel.startTracking()
    }

    // Request location updates
    LaunchedEffect(hasLocationPermission, uiState.isTracking) {
        if (hasLocationPermission && uiState.isTracking) {
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

                        // Follow user location on the map (running app feel)
                        mapRef?.let { map ->
                            try {
                                val cameraPosition = CameraPosition.Builder()
                                    .target(LatLng(location.latitude, location.longitude))
                                    .zoom(16.0)
                                    .build()
                                map.animateCamera(
                                    CameraUpdateFactory.newCameraPosition(cameraPosition),
                                    400
                                )
                            } catch (_: Exception) {
                                // Ignore camera errors
                            }
                        }
                    }
                }
            }
            locationCallback = callback
            
            try {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    callback,
                    context.mainLooper
                )
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        } else {
            locationCallback?.let { callback ->
                try {
                    fusedLocationClient.removeLocationUpdates(callback)
                } catch (e: Exception) {
                    // Handle cleanup errors
                }
            }
            locationCallback = null
        }
    }

    RunningScreenBody(
        uiState = uiState,
        onMapReady = { map -> mapLibreMap = map },
        onPauseClick = {
            viewModel.pauseTracking()
            // Navigate to PausedOverlayActivity
            val intent = Intent(context, PausedOverlayActivity::class.java)
            context.startActivity(intent)
            (context as? ComponentActivity)?.overridePendingTransition(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
        },
        onResumeClick = { viewModel.resumeTracking() }
    )
}

@Composable
fun RunningScreenBody(
    uiState: com.example.slrry_10.viewmodel.StartRunUiState,
    onMapReady: (MapLibreMap) -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit
) {
    val context = LocalContext.current
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Map - fills the entire screen
        MapViewComponent(
            mapView = null,
            mapLibreMap = null,
            uiState = uiState,
            onMapReady = onMapReady,
            showMap = true
        )

        // Top Status Bar - aligned to top
        TopStatusBar(
            hasLocationPermission = true,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(2f)
        )

        // Bottom section - Metrics and Buttons aligned to bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .zIndex(2f)
        ) {
            // Metrics section - positioned below map, above buttons
            RunningMetrics(
                uiState = uiState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            )
            
            // Action Buttons - at the very bottom
            RunningActionButtons(
                onPauseClick = {
                    onPauseClick()
                    // Navigate to PausedOverlayActivity
                    val intent = Intent(context, PausedOverlayActivity::class.java)
                    context.startActivity(intent)
                    (context as? ComponentActivity)?.overridePendingTransition(
                        android.R.anim.fade_in,
                        android.R.anim.fade_out
                    )
                },
                onResumeClick = onResumeClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "Running Screen", showSystemUi = true, device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=portrait")
@Composable
fun RunningScreenPreview() {
    SLRRY_10Theme {
        RunningScreenBody(
            uiState = StartRunUiState(
                isTracking = true,
                currentSession = com.example.slrry_10.model.RunSession(
                    id = "preview",
                    startTime = System.currentTimeMillis() - 300000,
                    isActive = true,
                    distance = 2500.0,
                    duration = 300,
                    averagePace = "5'00''"
                ),
                capturedAreas = listOf(
                    com.example.slrry_10.model.AreaModel(
                        polygon = emptyList(),
                        area = 100.0
                    )
                ),
                currentLocation = null,
                searchResults = emptyList()
            ),
            onMapReady = { },
            onPauseClick = {},
            onResumeClick = {}
        )
    }
}
