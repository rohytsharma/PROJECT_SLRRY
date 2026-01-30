package com.example.slrry_10

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.DisposableEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.slrry_10.model.LocationModel
import com.example.slrry_10.model.RunScreenState
import com.example.slrry_10.repository.LocationRepositoryImpl
import com.example.slrry_10.repository.FirebaseUserRepoImpl
import com.example.slrry_10.ui.*
import com.example.slrry_10.ui.theme.SLRRY_10Theme
import com.example.slrry_10.viewmodel.StartRunViewModel
import com.google.android.gms.location.*
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.OnMapReadyCallback
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.android.style.sources.RasterSource
import org.maplibre.android.style.layers.RasterLayer
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import org.maplibre.geojson.Polygon
import java.util.*

class StartRunActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
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
            
            // Optimized location request - less frequent updates to prevent UI blocking
            locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
                .setMinUpdateIntervalMillis(1000)
                .setMaxUpdateDelayMillis(5000)
                .build()
            
            requestLocationPermission()
            
            enableEdgeToEdge()
            setContent {
                SLRRY_10Theme {
                    StartRunScreen(
                        fusedLocationClient = fusedLocationClient,
                        locationRequest = locationRequest,
                        hasLocationPermission = hasLocationPermission,
                        onPermissionGranted = { hasLocationPermission = true },
                        introTitle = intent.getStringExtra(EXTRA_INTRO_TITLE),
                        introMessage = intent.getStringExtra(EXTRA_INTRO_MESSAGE)
                    )
                }
            }
        } catch (e: Exception) {
            // If initialization fails, show error screen
            e.printStackTrace()
            setContent {
                SLRRY_10Theme {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        androidx.compose.material3.Text(
                            text = "Error initializing app. Please restart.",
                            color = androidx.compose.ui.graphics.Color.Red
                        )
                    }
                }
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

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        // Location updates will be handled in the composable
    }

    override fun onPause() {
        super.onPause()
        // Location updates will be stopped in the composable
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
    }
}

const val EXTRA_INTRO_TITLE = "slrry_intro_title"
const val EXTRA_INTRO_MESSAGE = "slrry_intro_message"

@Composable
fun StartRunScreen(
    fusedLocationClient: FusedLocationProviderClient,
    locationRequest: LocationRequest,
    hasLocationPermission: Boolean,
    onPermissionGranted: () -> Unit,
    introTitle: String? = null,
    introMessage: String? = null,
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
    val isSimulatingLocation = rememberUpdatedState(uiState.isSimulatingLocation)
    var showIntro by rememberSaveable { mutableStateOf(true) }
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var locationCallback by remember { mutableStateOf<LocationCallback?>(null) }

    if (showIntro && !introTitle.isNullOrBlank() && !introMessage.isNullOrBlank()) {
        AlertDialog(
            onDismissRequest = { showIntro = false },
            title = { Text(introTitle!!) },
            text = { Text(introMessage!!) },
            confirmButton = {
                TextButton(onClick = { showIntro = false }) { Text("Let’s go") }
            }
        )
    }
    
    // Initialize with error handling
    LaunchedEffect(Unit) {
        try {
            // Small delay to ensure activity is fully initialized
            kotlinx.coroutines.delay(100)
        } catch (e: Exception) {
            // Ignore errors
        }
    }

    // Request location updates (also before tracking so map centers on device location)
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            var lastCameraUpdate = 0L
            val cameraUpdateInterval = 2000L // Update camera every 2 seconds max
            
            val callback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    if (isSimulatingLocation.value) return
                    result.lastLocation?.let { location ->
                        // Update location in ViewModel (non-blocking)
                        val locationModel = LocationModel(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            altitude = location.altitude,
                            accuracy = location.accuracy,
                            timestamp = location.time
                        )
                        viewModel.updateLocation(locationModel)
                        
                        // Throttle map camera updates to prevent UI blocking
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastCameraUpdate > cameraUpdateInterval) {
                            lastCameraUpdate = currentTime
                            mapLibreMap?.let { map ->
                                // Use coroutine scope to prevent blocking
                                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                                    try {
                                        val cameraPosition = CameraPosition.Builder()
                                            .target(LatLng(location.latitude, location.longitude))
                                            .zoom(16.0)
                                            .build()
                                        map.animateCamera(
                                            org.maplibre.android.camera.CameraUpdateFactory.newCameraPosition(cameraPosition),
                                            300
                                        )
                                    } catch (e: Exception) {
                                        // Silently handle errors to prevent crashes
                                    }
                                }
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

    // Get initial location - deferred to prevent blocking
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission && uiState.currentLocation == null) {
            // Add delay to prevent blocking during activity initialization
            kotlinx.coroutines.delay(300)
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        val locationModel = LocationModel(
                            latitude = it.latitude,
                            longitude = it.longitude,
                            altitude = it.altitude,
                            accuracy = it.accuracy
                        )
                        viewModel.updateLocation(locationModel)
                    }
                }.addOnFailureListener {
                    // Silently handle failures
                }
            } catch (e: SecurityException) {
                // Silently handle security exceptions
            }
        }
    }

    // Update map with path - debounced to prevent UI blocking
    LaunchedEffect(uiState.runPath.size) {
        if (uiState.runPath.isNotEmpty() && mapLibreMap != null) {
            // Debounce updates - only update every 1000ms to reduce load
            kotlinx.coroutines.delay(if (uiState.isSimulatingLocation) 120 else 1000)
            if (mapLibreMap != null && uiState.runPath.isNotEmpty()) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    try {
                        updatePathOnMap(mapLibreMap!!, uiState.runPath)
                    } catch (e: Exception) {
                        // Handle errors silently
                    }
                }
            }
        }
    }

    // Update map with route - optimized
    LaunchedEffect(uiState.selectedRoute) {
        uiState.selectedRoute?.let { route ->
            if (mapLibreMap != null) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    try {
                        updateRouteOnMap(mapLibreMap!!, route)
                    } catch (e: Exception) {
                        // Handle errors silently
                    }
                }
            }
        }
    }

    // Update map with captured areas - optimized
    LaunchedEffect(uiState.capturedAreas.size) {
        if (uiState.capturedAreas.isNotEmpty() && mapLibreMap != null) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                try {
                    updateAreasOnMap(mapLibreMap!!, uiState.capturedAreas)
                } catch (e: Exception) {
                    // Handle errors silently
                }
            }
        }
    }

    // Update map with current area being captured - optimized
    LaunchedEffect(uiState.currentAreaPolygon.size) {
        if (uiState.isCapturingArea && uiState.currentAreaPolygon.isNotEmpty() && mapLibreMap != null) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                try {
                    updateCurrentAreaOnMap(mapLibreMap!!, uiState.currentAreaPolygon)
                } catch (e: Exception) {
                    // Handle errors silently
                }
            }
        }
    }

    // Initialize map view
    LaunchedEffect(Unit) {
        // Map will be initialized in MapViewComponent
    }

    // Keep ONE MapView alive across all pages (no reset)
    Box(modifier = Modifier.fillMaxSize()) {
        MapViewComponent(
            mapView = mapView,
            mapLibreMap = mapLibreMap,
            uiState = uiState,
            onMapReady = { map -> mapLibreMap = map },
            showMap = true,
            enableUserMarkerDrag = (uiState.screenState == RunScreenState.RUNNING && uiState.isTracking),
            onSimulatedLocation = { loc -> viewModel.updateLocation(loc) },
            onSimulatedDragState = { sim -> viewModel.setSimulatingLocation(sim) }
        )

        // Screen Switcher (UI overlays only; map persists)
    when (uiState.screenState) {
        RunScreenState.READY_TO_START -> {
            ReadyToStartScreen(
                uiState = uiState,
                viewModel = viewModel,
                fusedLocationClient = fusedLocationClient,
                locationRequest = locationRequest,
                hasLocationPermission = hasLocationPermission,
                mapLibreMap = mapLibreMap,
                mapView = mapView,
                    onMapReady = { map -> mapLibreMap = map },
                    showMap = false
            )
        }
        RunScreenState.RUNNING -> {
            RunningScreen(
                uiState = uiState,
                viewModel = viewModel,
                mapLibreMap = mapLibreMap,
                mapView = mapView,
                    onMapReady = { map -> mapLibreMap = map },
                    showMap = false
            )
        }
        RunScreenState.PAUSED_WITH_OVERLAY -> {
            PausedWithOverlayScreen(
                uiState = uiState,
                viewModel = viewModel
            )
        }
        RunScreenState.PAUSED_WITH_MAP -> {
            PausedWithMapScreen(
                uiState = uiState,
                viewModel = viewModel,
                mapLibreMap = mapLibreMap,
                mapView = mapView,
                    onMapReady = { map -> mapLibreMap = map },
                    showMap = false
            )
        }
        RunScreenState.SUMMARY -> {
            RunSummaryScreen(
                uiState = uiState,
                viewModel = viewModel,
                mapLibreMap = mapLibreMap,
                mapView = mapView,
                    onMapReady = { map -> mapLibreMap = map },
                    showMap = false
            )
            }
        RunScreenState.MAPS -> {
            com.example.slrry_10.ui.MapsHubScreen(
                uiState = uiState,
                viewModel = viewModel,
                mapLibreMap = mapLibreMap
            )
        }
        }
    }
}

// Keep the old implementation for reference but it won't be used
@Composable
fun OldStartRunScreen(
    fusedLocationClient: FusedLocationProviderClient,
    locationRequest: LocationRequest,
    hasLocationPermission: Boolean,
    onPermissionGranted: () -> Unit,
    viewModel: StartRunViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var locationCallback by remember { mutableStateOf<LocationCallback?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        // MapLibre Map
        DisposableEffect(Unit) {
            onDispose {
                mapView?.onDestroy()
            }
        }
        
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    mapView = this
                    onCreate(null)
                    onStart()
                    onResume()
                    getMapAsync(object : OnMapReadyCallback {
                        override fun onMapReady(map: MapLibreMap) {
                            mapLibreMap = map
                            
                            // Create custom style with OpenStreetMap raster tiles
                            val styleJson = """
                            {
                              "version": 8,
                              "sources": {
                                "osm": {
                                  "type": "raster",
                                  "tiles": ["https://tile.openstreetmap.org/{z}/{x}/{y}.png"],
                                  "tileSize": 256,
                                  "attribution": "© OpenStreetMap contributors"
                                }
                              },
                              "layers": [
                                {
                                  "id": "osm-layer",
                                  "type": "raster",
                                  "source": "osm",
                                  "minzoom": 0,
                                  "maxzoom": 19
                                }
                              ]
                            }
                            """.trimIndent()
                            
                            map.setStyle(Style.Builder().fromJson(styleJson)) { style ->
                                // Center on current location
                                uiState.currentLocation?.let { loc ->
                                    val cameraPosition = CameraPosition.Builder()
                                        .target(LatLng(loc.latitude, loc.longitude))
                                        .zoom(15.0)
                                        .build()
                                    map.animateCamera(
                                        CameraUpdateFactory.newCameraPosition(cameraPosition)
                                    )
                                }
                                
                                // Add click listener for area capture
                                map.addOnMapClickListener(object : org.maplibre.android.maps.MapLibreMap.OnMapClickListener {
                                    override fun onMapClick(point: LatLng): Boolean {
                                    if (uiState.isCapturingArea) {
                                        val location = LocationModel(
                                            latitude = point.latitude,
                                            longitude = point.longitude
                                        )
                                        viewModel.addPointToArea(location)
                                    }
                                        return true
                                    }
                                })
                            }
                        }
                    })
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .zIndex(0f) // Map behind other UI elements
        )

        // Top Status Bar - Above map
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter)
                .zIndex(2f), // Above search bar and map
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Temperature indicator
            StatusPill(
                icon = Icons.Default.WbSunny,
                text = "32°C",
                color = Color(0xFF90EE90)
            )

            // Progress bar (GPS signal indicator)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (hasLocationPermission) Color(0xFF4CAF50) else Color.Gray)
            )

            // GPS indicator
            StatusPill(
                icon = Icons.Default.LocationOn,
                text = "GPS",
                color = if (hasLocationPermission) Color(0xFF90EE90) else Color.Gray
            )
        }

        // Search Bar - Make sure it's above the map
        var searchQuery by remember { mutableStateOf("") }
        var showSearchResults by remember { mutableStateOf(false) }
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 60.dp)
                .align(Alignment.TopCenter)
                .zIndex(1f) // Ensure search bar is above map
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it
                    if (it.isNotBlank()) {
                        viewModel.searchLocation(it)
                        showSearchResults = true
                    } else {
                        showSearchResults = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search location...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { 
                            searchQuery = ""
                            showSearchResults = false
                        }) {
                            Icon(Icons.Default.Close, null)
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
            
            if (showSearchResults && uiState.searchResults.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column {
                        uiState.searchResults.take(5).forEach { result ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        searchQuery = result.displayName
                                        showSearchResults = false
                                        val location = LocationModel(
                                            latitude = result.latitude,
                                            longitude = result.longitude
                                        )
                                        viewModel.updateLocation(location)
                                        mapLibreMap?.let { map ->
                                            val cameraPosition = CameraPosition.Builder()
                                                .target(LatLng(result.latitude, result.longitude))
                                                .zoom(15.0)
                                                .build()
                                            map.animateCamera(
                                                CameraUpdateFactory.newCameraPosition(cameraPosition)
                                            )
                                        }
                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Place, null, modifier = Modifier.padding(end = 8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(result.displayName, fontWeight = FontWeight.Medium)
                                    result.address?.let {
                                        Text(it, fontSize = 12.sp, color = Color.Gray)
                                    }
                                }
                            }
                            if (result != uiState.searchResults.last()) {
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }

        // Bottom Metrics and Action Button - Above map
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .zIndex(2f), // Above map
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Activity Metrics
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricCard(
                    value = uiState.currentSession?.averagePace ?: "0'00''",
                    label = "Avg Pace",
                    icon = Icons.AutoMirrored.Filled.DirectionsRun
                )
                MetricCard(
                    value = formatDuration(uiState.currentSession?.duration ?: 0L),
                    label = "Duration",
                    icon = Icons.Default.AccessTime
                )
                MetricCard(
                    value = String.format("%.2fm²", uiState.capturedAreas.sumOf { it.area }),
                    label = "area captured",
                    icon = Icons.Default.Castle
                )
            }

            // Action Buttons Row
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Area Capture Button
                if (!uiState.isTracking) {
                    FloatingActionButton(
                        onClick = {
                            if (uiState.isCapturingArea) {
                                viewModel.finishAreaCapture()
                            } else {
                                viewModel.startAreaCapture()
                            }
                        },
                        modifier = Modifier.size(56.dp),
                        containerColor = if (uiState.isCapturingArea) Color(0xFFFF9800) else Color(0xFF2196F3)
                    ) {
                        Icon(
                            imageVector = if (uiState.isCapturingArea) Icons.Default.Check else Icons.Default.Polyline,
                            contentDescription = "Area Capture",
                            tint = Color.White
                        )
                    }
                }

                // Main Action Button with Long Press (3 seconds)
                var pressStartTime by remember { mutableStateOf(0L) }
                var isPressing by remember { mutableStateOf(false) }
                val vibrator = remember {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                        vibratorManager.defaultVibrator
                    } else {
                        @Suppress("DEPRECATION")
                        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    }
                }
                
                // Handle long press with vibration
                LaunchedEffect(isPressing) {
                    if (isPressing && !uiState.isTracking) {
                        kotlinx.coroutines.delay(3000) // Wait 3 seconds
                        if (isPressing && !uiState.isTracking) {
                            // Vibrate to indicate run start
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
                            } else {
                                @Suppress("DEPRECATION")
                                vibrator.vibrate(200)
                            }
                            // Start tracking - this will start time, duration, and area capture tracking
                            viewModel.startTracking()
                            isPressing = false
                        }
                    }
                }
                
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(if (uiState.isTracking) Color(0xFFF44336) else Color(0xFF4CAF50))
                        .pointerInput(uiState.isTracking) {
                            if (!uiState.isTracking) {
                                detectTapGestures(
                                    onPress = {
                                        isPressing = true
                                        pressStartTime = System.currentTimeMillis()
                                        tryAwaitRelease()
                                        isPressing = false
                                    }
                                )
                            } else {
                                detectTapGestures(
                                    onTap = {
                                        viewModel.stopTracking()
                                    }
                                )
                            }
                        }
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (uiState.isTracking) Icons.Default.Stop else Icons.AutoMirrored.Filled.DirectionsRun,
                            contentDescription = if (uiState.isTracking) "Stop" else "Start Run",
                            modifier = Modifier.size(40.dp),
                            tint = Color.Black
                        )
                    }
                }

                // Route Button
                if (!uiState.isTracking && uiState.currentLocation != null) {
                    FloatingActionButton(
                        onClick = {
                            // Show route dialog or use last search result
                            if (uiState.searchResults.isNotEmpty()) {
                                val destination = uiState.searchResults.first()
                                val destLocation = LocationModel(
                                    latitude = destination.latitude,
                                    longitude = destination.longitude
                                )
                                viewModel.getRoute(uiState.currentLocation!!, destLocation)
                            }
                        },
                        modifier = Modifier.size(56.dp),
                        containerColor = Color(0xFF9C27B0)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Route,
                            contentDescription = "Route",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

fun updatePathOnMap(map: MapLibreMap, path: List<LocationModel>) {
    if (path.isEmpty()) return
    
    // Perform heavy operations on background thread
    kotlinx.coroutines.CoroutineScope(Dispatchers.Default).launch {
        try {
            val points = path.map { 
                org.maplibre.geojson.Point.fromLngLat(it.longitude, it.latitude) 
            }
            val lineString = LineString.fromLngLats(points)
            val feature = Feature.fromGeometry(lineString)
            
            // Update UI on main thread
            withContext(Dispatchers.Main) {
                map.getStyle { style ->
                    try {
                        var source = style.getSourceAs<GeoJsonSource>("path-source")
                        if (source == null) {
                            source = GeoJsonSource("path-source")
                            style.addSource(source)
                            
                            val lineLayer = LineLayer("path-layer", "path-source")
                            lineLayer.setProperties(
                                PropertyFactory.lineColor(Color(0xFF4CAF50).hashCode()),
                                PropertyFactory.lineWidth(5f),
                                PropertyFactory.lineCap("round"),
                                PropertyFactory.lineJoin("round")
                            )
                            style.addLayer(lineLayer)
                        }
                        source.setGeoJson(FeatureCollection.fromFeatures(listOf(feature)))
                    } catch (e: Exception) {
                        // Handle errors silently
                    }
                }
            }
        } catch (e: Exception) {
            // Handle errors silently
        }
    }
}

fun updateRouteOnMap(map: MapLibreMap, route: com.example.slrry_10.model.RouteModel) {
    if (route.coordinates.isEmpty()) return
    
    kotlinx.coroutines.CoroutineScope(Dispatchers.Default).launch {
        try {
            val points = route.coordinates.map { 
                org.maplibre.geojson.Point.fromLngLat(it.longitude, it.latitude) 
            }
            val lineString = LineString.fromLngLats(points)
            val feature = Feature.fromGeometry(lineString)
            
            withContext(Dispatchers.Main) {
                map.getStyle { style ->
                    try {
                        var source = style.getSourceAs<GeoJsonSource>("route-source")
                        if (source == null) {
                            source = GeoJsonSource("route-source")
                            style.addSource(source)
                            
                            val lineLayer = LineLayer("route-layer", "route-source")
                            lineLayer.setProperties(
                                PropertyFactory.lineColor(Color(0xFF2196F3).hashCode()),
                                PropertyFactory.lineWidth(6f),
                                PropertyFactory.lineDasharray(arrayOf(2f, 2f))
                            )
                            style.addLayer(lineLayer)
                        }
                        source.setGeoJson(FeatureCollection.fromFeatures(listOf(feature)))
                    } catch (e: Exception) {
                        // Handle errors silently
                    }
                }
            }
        } catch (e: Exception) {
            // Handle errors silently
        }
    }
}

fun updateAreasOnMap(map: MapLibreMap, areas: List<com.example.slrry_10.model.AreaModel>) {
    kotlinx.coroutines.CoroutineScope(Dispatchers.Default).launch {
        try {
            val features = areas.mapIndexedNotNull { index, area ->
                if (area.polygon.size < 3) return@mapIndexedNotNull null
                
                val points = area.polygon.map { Point.fromLngLat(it.longitude, it.latitude) }
                val polygon = org.maplibre.geojson.Polygon.fromLngLats(listOf(points))
                Feature.fromGeometry(polygon) to index
            }
            
            withContext(Dispatchers.Main) {
                map.getStyle { style ->
                    try {
                        features.forEach { (feature, index) ->
                            val sourceId = "area-source-$index"
                            var source = style.getSourceAs<GeoJsonSource>(sourceId)
                            if (source == null) {
                                source = GeoJsonSource(sourceId)
                                style.addSource(source)
                                
                                val fillLayer = org.maplibre.android.style.layers.FillLayer("area-fill-$index", sourceId)
                                fillLayer.setProperties(
                                    PropertyFactory.fillColor(Color(0x33FF9800).hashCode()),
                                    PropertyFactory.fillOpacity(0.5f)
                                )
                                style.addLayer(fillLayer)
                                
                                val lineLayer = LineLayer("area-line-$index", sourceId)
                                lineLayer.setProperties(
                                    PropertyFactory.lineColor(Color(0xFFFF9800).hashCode()),
                                    PropertyFactory.lineWidth(3f)
                                )
                                style.addLayer(lineLayer)
                            }
                            source.setGeoJson(FeatureCollection.fromFeatures(listOf(feature)))
                        }
                    } catch (e: Exception) {
                        // Handle errors silently
                    }
                }
            }
        } catch (e: Exception) {
            // Handle errors silently
        }
    }
}

fun updateCurrentAreaOnMap(map: MapLibreMap, polygon: List<LocationModel>) {
    if (polygon.isEmpty()) return
    
    kotlinx.coroutines.CoroutineScope(Dispatchers.Default).launch {
        try {
            val points = polygon.map { 
                org.maplibre.geojson.Point.fromLngLat(it.longitude, it.latitude) 
            }
            
            withContext(Dispatchers.Main) {
                map.getStyle { style ->
                    try {
                        // Remove existing layers and sources
                        try {
                            style.removeLayer("current-area-line-layer")
                            style.removeLayer("current-area-fill-layer")
                            style.removeSource("current-area-source")
                        } catch (e: Exception) {
                            // Layers don't exist yet, ignore
                        }
                        
                        if (polygon.size >= 3) {
                            // Show filled polygon
                            val polygonGeoJson = org.maplibre.geojson.Polygon.fromLngLats(listOf(points))
                            val polygonFeature = Feature.fromGeometry(polygonGeoJson)
                            
                            val source = GeoJsonSource("current-area-source")
                            style.addSource(source)
                            
                            val fillLayer = FillLayer("current-area-fill-layer", "current-area-source")
                            fillLayer.setProperties(
                                PropertyFactory.fillColor(Color(0x33FF9800).hashCode()),
                                PropertyFactory.fillOpacity(0.4f)
                            )
                            style.addLayer(fillLayer)
                            
                            val lineLayer = LineLayer("current-area-line-layer", "current-area-source")
                            lineLayer.setProperties(
                                PropertyFactory.lineColor(Color(0xFFFF9800).hashCode()),
                                PropertyFactory.lineWidth(4f),
                                PropertyFactory.lineDasharray(arrayOf(5f, 5f))
                            )
                            style.addLayer(lineLayer)
                            
                            source.setGeoJson(FeatureCollection.fromFeatures(listOf(polygonFeature)))
                        } else {
                            // Show line only
                            val lineString = LineString.fromLngLats(points)
                            val lineFeature = Feature.fromGeometry(lineString)
                            
                            val source = GeoJsonSource("current-area-source")
                            style.addSource(source)
                            
                            val lineLayer = LineLayer("current-area-line-layer", "current-area-source")
                            lineLayer.setProperties(
                                PropertyFactory.lineColor(Color(0xFFFF9800).hashCode()),
                                PropertyFactory.lineWidth(4f),
                                PropertyFactory.lineDasharray(arrayOf(5f, 5f))
                            )
                            style.addLayer(lineLayer)
                            
                            source.setGeoJson(FeatureCollection.fromFeatures(listOf(lineFeature)))
                        }
                    } catch (e: Exception) {
                        // Handle errors silently
                    }
                }
            }
        } catch (e: Exception) {
            // Handle errors silently
        }
    }
}

@Composable
fun StatusPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = Color.Black
        )
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}

@Composable
fun MetricCard(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Color.Gray
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

fun formatDuration(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, secs)
    } else {
        String.format("%02d:%02d", minutes, secs)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun StartRunScreenPreview() {
    SLRRY_10Theme {
        // Preview of the UI components without the map
        Box(modifier = Modifier.fillMaxSize()) {
            // Simulated map background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFE0E0E0))
            )
            
            // Top Status Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusPill(
                    icon = Icons.Default.WbSunny,
                    text = "32°C",
                    color = Color(0xFF90EE90)
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF4CAF50))
                )

                StatusPill(
                    icon = Icons.Default.LocationOn,
                    text = "GPS",
                    color = Color(0xFF90EE90)
                )
            }

            // Search Bar
            var searchQuery by remember { mutableStateOf("") }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 60.dp)
                    .align(Alignment.TopCenter)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search location...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }

            // Bottom Metrics and Action Button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Activity Metrics
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MetricCard(
                        value = "5'30''",
                        label = "Avg Pace",
                        icon = Icons.AutoMirrored.Filled.DirectionsRun
                    )
                    MetricCard(
                        value = "12:34",
                        label = "Duration",
                        icon = Icons.Default.AccessTime
                    )
                    MetricCard(
                        value = "125.50m²",
                        label = "area captured",
                        icon = Icons.Default.Castle
                    )
                }

                // Action Button
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50))
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                            contentDescription = "Start Run",
                            modifier = Modifier.size(40.dp),
                            tint = Color.Black
                        )
                    }
                }
            }
        }
    }
}
