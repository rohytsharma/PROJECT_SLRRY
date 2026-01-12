package com.example.slrry_10.ui

import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.DisposableEffect
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.example.slrry_10.model.LocationModel
import com.example.slrry_10.model.RunScreenState
import com.example.slrry_10.viewmodel.StartRunViewModel
import com.example.slrry_10.viewmodel.StartRunUiState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.OnMapReadyCallback
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.RasterLayer
import org.maplibre.android.style.sources.RasterSource
import org.maplibre.android.style.sources.TileSet
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.sin
import org.json.JSONObject
import org.maplibre.android.offline.OfflineManager
import org.maplibre.android.offline.OfflineRegion
import org.maplibre.android.offline.OfflineRegionError
import org.maplibre.android.offline.OfflineRegionStatus
import org.maplibre.android.offline.OfflineTilePyramidRegionDefinition
import kotlinx.coroutines.delay

// Screen 1: Ready to Start
@Composable
fun ReadyToStartScreen(
    uiState: com.example.slrry_10.viewmodel.StartRunUiState,
    viewModel: StartRunViewModel,
    fusedLocationClient: FusedLocationProviderClient,
    locationRequest: LocationRequest,
    hasLocationPermission: Boolean,
    mapLibreMap: MapLibreMap?,
    mapView: MapView?,
    onMapReady: (MapLibreMap) -> Unit,
    showMap: Boolean = true
) {
    val context = LocalContext.current
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

    LaunchedEffect(isPressing) {
        if (isPressing && !uiState.isTracking) {
            kotlinx.coroutines.delay(2000) // 2 seconds
            if (isPressing && !uiState.isTracking) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(200)
                }
                viewModel.startTracking()
                isPressing = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Map
        MapViewComponent(
            mapView = mapView,
            mapLibreMap = mapLibreMap,
            uiState = uiState,
            onMapReady = onMapReady,
            showMap = showMap
        )

        // Top Status Bar
        TopStatusBar(
            hasLocationPermission = hasLocationPermission,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(2f)
        )

        // Bottom Metrics
        BottomMetricsAndButton(
            uiState = uiState,
            onStartPress = { isPressing = true },
            onStartRelease = { isPressing = false },
            showStartButton = true,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .zIndex(2f)
        )
    }
}

// Screen 2: Running
@Composable
fun RunningScreen(
    uiState: com.example.slrry_10.viewmodel.StartRunUiState,
    viewModel: StartRunViewModel,
    mapLibreMap: MapLibreMap?,
    mapView: MapView?,
    onMapReady: (MapLibreMap) -> Unit = {},
    showMap: Boolean = true
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Map
        MapViewComponent(
            mapView = mapView,
            mapLibreMap = mapLibreMap,
            uiState = uiState,
            onMapReady = onMapReady,
            showMap = showMap
        )

        // Top Status Bar
        TopStatusBar(
            hasLocationPermission = true,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(2f)
        )

        // Bottom Metrics and Buttons
        BottomMetricsAndButton(
            uiState = uiState,
            onPauseClick = { viewModel.pauseTracking() },
            onResumeClick = { viewModel.resumeTracking() },
            showPauseButton = true,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .zIndex(2f)
        )
    }
}

// Screen 3: Paused with Overlay (Distance shown, map hidden)
@Composable
fun PausedWithOverlayScreen(
    uiState: com.example.slrry_10.viewmodel.StartRunUiState,
    viewModel: StartRunViewModel
) {
    val distanceKm = ((uiState.currentSession?.distance ?: 0.0) / 1000.0)
    Box(modifier = Modifier.fillMaxSize()) {
        // White background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        )

        // Distance Overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = String.format("%.2f", distanceKm),
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "Distance (Km)",
                fontSize = 18.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // View Map Button
            Button(
                onClick = { viewModel.showMapInPaused() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text("View Map", color = Color.White, fontSize = 16.sp)
            }
        }

        // Bottom Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Continue Button
            Button(
                onClick = { viewModel.resumeTracking() },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF4CAF50)),
                    width = 2.dp
                )
            ) {
                Text("CONTINUE", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
            }

            // Finish Button (click)
            Button(
                onClick = { viewModel.finishRun() },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Color.White),
                    width = 2.dp
                )
            ) {
                Text("FINISH", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Screen 4: Paused with Map
@Composable
fun PausedWithMapScreen(
    uiState: com.example.slrry_10.viewmodel.StartRunUiState,
    viewModel: StartRunViewModel,
    mapLibreMap: MapLibreMap?,
    mapView: MapView?,
    onMapReady: (MapLibreMap) -> Unit = {},
    showMap: Boolean = true
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Map
        MapViewComponent(
            mapView = mapView,
            mapLibreMap = mapLibreMap,
            uiState = uiState,
            onMapReady = onMapReady,
            showMap = showMap
        )

        // Top Status Bar
        TopStatusBar(
            hasLocationPermission = true,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(2f)
        )

        // Bottom Controls (resume / continue / finish)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(24.dp)
                .navigationBarsPadding()
                .zIndex(2f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricCard(
                    value = uiState.currentSession?.averagePace ?: "0'00''",
                    label = "Avg Pace",
                    icon = Icons.Default.DirectionsRun
                )
                MetricCard(
                    value = formatDuration(uiState.currentSession?.duration ?: 0L),
                    label = "Duration",
                    icon = Icons.Default.AccessTime
                )
                MetricCard(
                    value = String.format("%.2fm²", uiState.capturedAreas.sumOf { it.area }),
                    label = "area",
                    icon = Icons.Default.Castle
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
                FloatingActionButton(
                    onClick = { viewModel.resumeTracking() },
                    modifier = Modifier.size(64.dp),
                    containerColor = Color(0xFF4CAF50),
                    shape = CircleShape
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Resume", tint = Color.White, modifier = Modifier.size(24.dp))
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                }

                Button(
                    onClick = { viewModel.hideMapInPaused() },
            modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF4CAF50)),
                        width = 2.dp
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text("CONTINUE", color = Color.Black, fontWeight = FontWeight.Bold)
        }

                FloatingActionButton(
                    onClick = { viewModel.finishRun() },
                    modifier = Modifier.size(56.dp),
                    containerColor = Color.Black,
                    shape = CircleShape
                ) {
                    Text("FINISH", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }
            }
        }
    }
}

// Screen 5: Summary
@Composable
fun RunSummaryScreen(
    uiState: com.example.slrry_10.viewmodel.StartRunUiState,
    viewModel: StartRunViewModel,
    mapLibreMap: MapLibreMap?,
    mapView: MapView?,
    onMapReady: (MapLibreMap) -> Unit = {},
    showMap: Boolean = true
) {
    val activity = LocalContext.current as? Activity
    // Map is hosted at the StartRunActivity root; this composable is only the overlay UI.
    SummaryPage(
        uiState = uiState,
        // After run details -> Continue should return to Dashboard (finish this activity).
        onContinue = { activity?.finish() }
    )
}

@Composable
fun SummaryMetric(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(label, color = Color.White, fontSize = 12.sp)
    }
}

@Composable
fun DetailMetric(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 16.sp)
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

// Shared Components
@Composable
fun MapViewComponent(
    mapView: MapView?,
    mapLibreMap: MapLibreMap?,
    uiState: StartRunUiState,
    onMapReady: (MapLibreMap) -> Unit,
    showMap: Boolean
) {
    if (!showMap) return

    // MapLibre's GL surface cannot render inside Android Studio Compose Preview.
    // Show a lightweight placeholder there so the UI isn't a black box.
    if (LocalInspectionMode.current) {
        OfflineMapPlaceholder(modifier = Modifier.fillMaxSize())
        return
    }
    
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val internalMap = remember { mutableStateOf<MapLibreMap?>(null) }
    var isMapLoaded by remember { mutableStateOf(false) }
    var showFallback by remember { mutableStateOf(false) }
    var offlineDownloadProgress by remember { mutableStateOf<Int?>(null) }
    var hasCenteredOnUser by remember { mutableStateOf(false) }
    var lastFollowUpdateMs by remember { mutableStateOf(0L) }
    
    // Use remember to persist map view across recompositions
    val currentMapView = remember { 
        mutableStateOf<MapView?>(null)
    }
    val isMapInitialized = remember { mutableStateOf(false) }
    
    // Initialize map view synchronously but defer heavy operations
    // MapView must be created on main thread, but we'll defer style loading
    
    // Manage lifecycle - with error handling
    DisposableEffect(lifecycleOwner) {
        val lifecycle = lifecycleOwner.lifecycle
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            try {
                currentMapView.value?.let { view ->
                    when (event) {
                        androidx.lifecycle.Lifecycle.Event.ON_CREATE -> {
                            try {
                                view.onCreate(null)
                            } catch (e: Exception) {
                                // Ignore onCreate errors
                            }
                        }
                        androidx.lifecycle.Lifecycle.Event.ON_START -> {
                            try {
                                view.onStart()
                            } catch (e: Exception) {
                                // Ignore onStart errors
                            }
                        }
                        androidx.lifecycle.Lifecycle.Event.ON_RESUME -> {
                            try {
                                view.onResume()
                            } catch (e: Exception) {
                                // Ignore onResume errors
                            }
                        }
                        androidx.lifecycle.Lifecycle.Event.ON_PAUSE -> {
                            try {
                                view.onPause()
                            } catch (e: Exception) {
                                // Ignore onPause errors
                            }
                        }
                        androidx.lifecycle.Lifecycle.Event.ON_STOP -> {
                            try {
                                view.onStop()
                            } catch (e: Exception) {
                                // Ignore onStop errors
                            }
                        }
                        androidx.lifecycle.Lifecycle.Event.ON_DESTROY -> {
                            try {
                                view.onDestroy()
                            } catch (e: Exception) {
                                // Ignore onDestroy errors
                            }
                        }
                        else -> {}
                    }
                }
            } catch (e: Exception) {
                // Ignore lifecycle errors
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            try {
                lifecycle.removeObserver(observer)
                currentMapView.value?.let { view ->
                    try {
                        view.onPause()
                        view.onStop()
                        view.onDestroy()
                    } catch (e: Exception) {
                        // Ignore cleanup errors
                    }
                }
            } catch (e: Exception) {
                // Ignore dispose errors
            }
        }
    }
    
    AndroidView(
        factory = { ctx ->
            try {
                if (currentMapView.value == null) {
                    // Create map view with proper error handling
                    val newMapView = MapView(ctx)
                    try {
                        // If tiles fail to load, avoid a pure black background
                        newMapView.setBackgroundColor(android.graphics.Color.parseColor("#E6E6E6"))
                    } catch (_: Exception) {}
                    currentMapView.value = newMapView
                    isMapInitialized.value = true

                    // IMPORTANT: ensure MapView lifecycle is started immediately.
                    // In Compose, the MapView can be created after the Activity is already RESUMED,
                    // which means it might never receive ON_CREATE/ON_START/ON_RESUME events.
                    try {
                        newMapView.onCreate(null)
                        newMapView.onStart()
                        newMapView.onResume()
                    } catch (_: Exception) {
                        // Ignore lifecycle init errors
                    }
                    
                    // Set up map callback - this is async and won't block
                    newMapView.getMapAsync(object : org.maplibre.android.maps.OnMapReadyCallback {
                        override fun onMapReady(map: MapLibreMap) {
                            try {
                                internalMap.value = map

                                // Use the same raster OSM style everywhere (matches MapsHub and avoids
                                // vector style/glyph/sprite dependencies).
                                map.setStyle(Style.Builder().fromUri(OFFLINE_STYLE_URI)) {
                                    // Style loaded (not necessarily all tiles yet, but map should render)
                                    isMapLoaded = true
                                    showFallback = false

                                    // If we already have a location, start centered on it (not zoomed out).
                                    val loc = uiState.currentLocation
                                    if (loc != null) {
                                        try {
                                            val camera = CameraPosition.Builder()
                                                .target(LatLng(loc.latitude, loc.longitude))
                                                .zoom(16.5)
                                                .build()
                                            map.moveCamera(CameraUpdateFactory.newCameraPosition(camera))
                                            hasCenteredOnUser = true
                                        } catch (_: Exception) {}
                                    }

                                    // Show current location marker if available.
                                    try {
                                        updateUserLocationOnMap(map, loc)
                                    } catch (_: Exception) {}
                                }
                                onMapReady(map)
                            } catch (e: Exception) {
                                // Handle initialization errors - don't crash
                                e.printStackTrace()
                            }
                        }
                    })
                    newMapView
                } else {
                    currentMapView.value!!
                }
            } catch (e: Exception) {
                // If MapView creation fails, return a placeholder view
                android.view.View(ctx).apply {
                    setBackgroundColor(android.graphics.Color.parseColor("#E0E0E0"))
                }
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .zIndex(0f),
        update = { view ->
            // View updates handled by lifecycle observer
        }
    )

    // If tiles/style fail to render, show a lightweight placeholder instead of a black screen.
    if (showFallback) {
        OfflineMapPlaceholder(modifier = Modifier.fillMaxSize())
    }

    // If the style/tiles haven't loaded after a few seconds, show placeholder instead of black.
    LaunchedEffect(Unit) {
        delay(4500)
        if (!isMapLoaded) showFallback = true
    }

    // Keep current location marker updated
    LaunchedEffect(uiState.currentLocation, uiState.isTracking) {
        val map = internalMap.value ?: return@LaunchedEffect
        try {
            updateUserLocationOnMap(map, uiState.currentLocation)
        } catch (_: Exception) {
            // Ignore marker errors
        }

        val loc = uiState.currentLocation

        // Center tightly when we first get a real GPS fix.
        if (!hasCenteredOnUser && loc != null) {
            try {
                val camera = CameraPosition.Builder()
                    .target(LatLng(loc.latitude, loc.longitude))
                    .zoom(16.5)
                    .build()
                map.animateCamera(CameraUpdateFactory.newCameraPosition(camera), 400)
                hasCenteredOnUser = true
            } catch (_: Exception) {
                // Ignore camera errors
            }
        }

        // While tracking, keep the camera following the user (throttled so it stays smooth).
        if (uiState.isTracking && loc != null) {
            val now = System.currentTimeMillis()
            if (now - lastFollowUpdateMs >= 1200L) {
                lastFollowUpdateMs = now
                try {
                    val camera = CameraPosition.Builder()
                        .target(LatLng(loc.latitude, loc.longitude))
                        .zoom(16.5)
                        .build()
                    map.animateCamera(CameraUpdateFactory.newCameraPosition(camera), 300)
                } catch (_: Exception) {
                    // Ignore camera errors
                }
            }
        }

        // Start offline download once we have a fix on location (one-time)
        if (loc != null) {
            maybeStartOfflineDownload(
                context = context,
                center = LatLng(loc.latitude, loc.longitude),
                onProgress = { pct -> offlineDownloadProgress = pct }
            )
        }
    }

    // Note: offline download runs silently; no UI overlays (matches reference designs).
}

private fun maybeStartOfflineDownload(
    context: Context,
    center: LatLng,
    onProgress: (Int?) -> Unit
) {
    val prefs = context.getSharedPreferences(OFFLINE_PREFS, MODE_PRIVATE)
    if (prefs.getBoolean(OFFLINE_DOWNLOADED, false)) return

    // Download a small region around the current GPS (so it works offline afterwards)
    val delta = 0.08 // ~8-9km radius
    val bounds = LatLngBounds.from(
        center.latitude + delta,
        center.longitude + delta,
        center.latitude - delta,
        center.longitude - delta
    )

    val definition = OfflineTilePyramidRegionDefinition(
        OFFLINE_STYLE_URI,
        bounds,
        12.0, // min zoom
        16.0, // max zoom
        context.resources.displayMetrics.density
    )

    val metadata = JSONObject()
        .put("name", "SLRRY Offline Area")
        .put("createdAt", System.currentTimeMillis())
        .toString()
        .toByteArray(Charsets.UTF_8)

    try {
        val offlineManager = OfflineManager.getInstance(context)
        offlineManager.createOfflineRegion(
            definition,
            metadata,
            object : OfflineManager.CreateOfflineRegionCallback {
                override fun onCreate(offlineRegion: OfflineRegion) {
                    offlineRegion.setObserver(object : OfflineRegion.OfflineRegionObserver {
                        override fun onStatusChanged(status: OfflineRegionStatus) {
                            if (status.isComplete) {
                                prefs.edit().putBoolean(OFFLINE_DOWNLOADED, true).apply()
                                onProgress(null)
                                offlineRegion.setDownloadState(OfflineRegion.STATE_INACTIVE)
                                return
                            }

                            val required = status.requiredResourceCount
                            val completed = status.completedResourceCount
                            if (required > 0L) {
                                val pct = ((completed * 100) / required).toInt().coerceIn(0, 100)
                                onProgress(pct)
                            }
                        }

                        override fun onError(error: OfflineRegionError) {
                            onProgress(null)
                        }

                        override fun mapboxTileCountLimitExceeded(limit: Long) {
                            onProgress(null)
                        }
                    })

                    offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE)
                }

                override fun onError(error: String) {
                    onProgress(null)
                }
            }
        )
    } catch (_: Throwable) {
        onProgress(null)
    }
}

@Composable
fun OfflineMapPlaceholder(
    modifier: Modifier = Modifier
) {
    // A lightweight fake-map background (grid + a few “roads”) to avoid a black screen
    Box(modifier = modifier.background(Color(0xFF101214))) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Subtle grid
            val gridColor = androidx.compose.ui.graphics.Color(0xFF1B1F22)
            val step = (minOf(w, h) / 8f).coerceAtLeast(80f)
            var x = 0f
            while (x <= w) {
                drawLine(gridColor, Offset(x, 0f), Offset(x, h), strokeWidth = 1.5f)
                x += step
            }
            var y = 0f
            while (y <= h) {
                drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1.5f)
                y += step
            }

            // A few curved “roads”
            val roadColor = androidx.compose.ui.graphics.Color(0xFF2C3338)
            fun road(seed: Float, thickness: Float) {
                val path = Path()
                path.moveTo(0f, h * (0.2f + 0.6f * seed))
                var px = 0f
                while (px <= w) {
                    val t = px / w
                    val py = h * (0.2f + 0.6f * seed) + sin((t * 6.28f) + seed * 10f) * (h * 0.05f)
                    path.lineTo(px, py)
                    px += 30f
                }
                drawPath(path, roadColor, style = Stroke(width = thickness, cap = StrokeCap.Round))
            }
            road(0.15f, 10f)
            road(0.45f, 14f)
            road(0.75f, 12f)

            // Center “you are here” dot
            val center = Offset(w / 2f, h / 2f)
            drawCircle(color = androidx.compose.ui.graphics.Color.White, radius = 18f, center = center)
            drawCircle(color = androidx.compose.ui.graphics.Color(0xFF4CAF50), radius = 14f, center = center)
        }
    }
}

@Composable
fun TopStatusBar(
    hasLocationPermission: Boolean,
    modifier: Modifier = Modifier
) {
    val pill = Color(0xFFDDF2B3)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatusPill(
            icon = Icons.Default.Settings,
            text = "32°C",
            color = pill
        )
        
        SignalBarsPill(color = pill)

        StatusPill(
            icon = Icons.Default.Wifi,
            text = "GPS",
            color = if (hasLocationPermission) pill else Color(0xFFE0E0E0)
        )
    }
}

@Composable
private fun SignalBarsPill(color: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(26.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFB8FF3A))
        )
        Box(
            modifier = Modifier
                .width(26.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFB8FF3A), RoundedCornerShape(4.dp))
        )
    }
}

@Composable
fun IconPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isHighlighted: Boolean
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isHighlighted) Color(0xFF4CAF50) else Color(0xFFE0E0E0)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isHighlighted) Color.White else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
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
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.Black)
    }
}

@Composable
fun SearchBarComponent(
    uiState: StartRunUiState,
    viewModel: StartRunViewModel?,
    mapLibreMap: MapLibreMap?,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showSearchResults by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 60.dp)
            .zIndex(1f)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                if (it.isNotBlank()) {
                    viewModel?.searchLocation(it)
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
                                    viewModel?.updateLocation(location)
                                    mapLibreMap?.let { map ->
                                        val cameraPosition = org.maplibre.android.camera.CameraPosition.Builder()
                                            .target(org.maplibre.android.geometry.LatLng(result.latitude, result.longitude))
                                            .zoom(15.0)
                                            .build()
                                        map.animateCamera(
                                            org.maplibre.android.camera.CameraUpdateFactory.newCameraPosition(cameraPosition)
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
}

@Composable
fun BottomMetricsAndButton(
    uiState: StartRunUiState,
    onStartPress: (() -> Unit)? = null,
    onStartRelease: (() -> Unit)? = null,
    onPauseClick: (() -> Unit)? = null,
    onResumeClick: (() -> Unit)? = null,
    onHideMapClick: (() -> Unit)? = null,
    showStartButton: Boolean = false,
    showPauseButton: Boolean = false,
    showContinueButton: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .zIndex(2f)
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
                value = uiState.currentSession?.averagePace ?: "0'00''",
                label = "Avg Pace",
                icon = Icons.Default.DirectionsRun
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

        // Action Buttons
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showStartButton && onStartPress != null) {
                // Big start button (green ring + runner)
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .border(width = 8.dp, color = Color(0xFFB8FF3A), shape = CircleShape)
                        .background(Color.Transparent)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    onStartPress()
                                    tryAwaitRelease()
                                    onStartRelease?.invoke()
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(74.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsRun,
                            contentDescription = "Start Run",
                            modifier = Modifier.size(42.dp),
                            tint = Color.Black
                        )
                    }
                }
            }

            if (showPauseButton) {
                FloatingActionButton(
                    onClick = { onResumeClick?.invoke() },
                    modifier = Modifier.size(72.dp),
                    containerColor = Color(0xFFB8FF3A),
                    shape = CircleShape
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Go", tint = Color.Black, modifier = Modifier.size(28.dp))
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Black, modifier = Modifier.size(28.dp))
                    }
                }

                OutlinedButton(
                    onClick = { onPauseClick?.invoke() },
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFB8FF3A)),
                        width = 3.dp
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                ) {
                    Text("PAUSE", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            if (showContinueButton) {
                FloatingActionButton(
                    onClick = { onResumeClick?.invoke() },
                    modifier = Modifier.size(56.dp),
                    containerColor = Color(0xFF4CAF50)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Continue", tint = Color.White)
                }
                
                if (onHideMapClick != null) {
                    FloatingActionButton(
                        onClick = { onHideMapClick() },
                        modifier = Modifier.size(56.dp),
                        containerColor = Color.White,
                        contentColor = Color(0xFF4CAF50)
                    ) {
                        Text("CONTINUE", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
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
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color(0xFF2B2F32))
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Text(label, fontSize = 12.sp, color = Color(0xFF6E757A))
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

private const val USER_LOCATION_SOURCE_ID = "user-location-source"
private const val USER_LOCATION_LAYER_ID = "user-location-layer"
private const val OFFLINE_STYLE_URI = "asset://offline_style.json"
private const val OFFLINE_PREFS = "slrry_offline_maps"
private const val OFFLINE_DOWNLOADED = "region_downloaded_v1"

private fun updateUserLocationOnMap(map: MapLibreMap, location: LocationModel?) {
    if (location == null) return
    map.getStyle { style ->
        try {
            val point = org.maplibre.geojson.Point.fromLngLat(location.longitude, location.latitude)
            val feature = org.maplibre.geojson.Feature.fromGeometry(point)

            var source =
                style.getSourceAs<org.maplibre.android.style.sources.GeoJsonSource>(USER_LOCATION_SOURCE_ID)
            if (source == null) {
                source = org.maplibre.android.style.sources.GeoJsonSource(USER_LOCATION_SOURCE_ID)
                style.addSource(source)

                val circleLayer =
                    org.maplibre.android.style.layers.CircleLayer(USER_LOCATION_LAYER_ID, USER_LOCATION_SOURCE_ID)
                circleLayer.setProperties(
                    org.maplibre.android.style.layers.PropertyFactory.circleRadius(8f),
                    org.maplibre.android.style.layers.PropertyFactory.circleColor(Color(0xFF4CAF50).hashCode()),
                    org.maplibre.android.style.layers.PropertyFactory.circleStrokeColor(Color.White.hashCode()),
                    org.maplibre.android.style.layers.PropertyFactory.circleStrokeWidth(2f)
                )
                style.addLayer(circleLayer)
            }

            source.setGeoJson(org.maplibre.geojson.FeatureCollection.fromFeatures(listOf(feature)))
        } catch (_: Exception) {
            // Ignore style update errors
        }
    }
}

