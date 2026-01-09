package com.example.slrry_10.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.slrry_10.model.AreaModel
import com.example.slrry_10.model.LocationModel
import com.example.slrry_10.model.RouteModel
import com.example.slrry_10.model.RunSession
import com.example.slrry_10.model.RunScreenState
import com.example.slrry_10.model.SearchResult
import com.example.slrry_10.repository.LocationRepository
import com.example.slrry_10.repository.UserRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.cos

enum class MapsTab { WORLD, PERSONAL, FRIENDS }

data class ZoneOwner(
    val id: String,
    val displayName: String,
    val colorArgb: Int,
    val areas: List<AreaModel>
)

data class StartRunUiState(
    val currentLocation: LocationModel? = null,
    val isTracking: Boolean = false,
    val isPaused: Boolean = false,
    val runPath: List<LocationModel> = emptyList(),
    val currentSession: RunSession? = null,
    val searchResults: List<SearchResult> = emptyList(),
    val selectedRoute: RouteModel? = null,
    val capturedAreas: List<AreaModel> = emptyList(),
    val isCapturingArea: Boolean = false,
    val currentAreaPolygon: List<LocationModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val screenState: RunScreenState = RunScreenState.READY_TO_START,
    val showMapInPaused: Boolean = false,
    // Maps screen state
    val mapsTab: MapsTab = MapsTab.PERSONAL,
    val worldOwners: List<ZoneOwner> = emptyList(),
    val friendsOwners: List<ZoneOwner> = emptyList()
)

class StartRunViewModel(
    private val userRepo: UserRepo,
    private val locationRepo: LocationRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(StartRunUiState())
    val uiState: StateFlow<StartRunUiState> = _uiState.asStateFlow()
    
    fun updateLocation(location: LocationModel) {
        // Use viewModelScope to ensure updates happen on correct thread
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            val currentState = _uiState.value
            val newPath = if (currentState.isTracking) {
                currentState.runPath + location
            } else {
                currentState.runPath
            }
            
            // Update state on main thread
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                _uiState.value = currentState.copy(
                    currentLocation = location,
                    runPath = newPath
                )
                
                if (currentState.isTracking) {
                    updateCurrentSession()
                }
            }
        }
    }
    
    fun startTracking() {
        val session = RunSession(
            id = System.currentTimeMillis().toString(),
            startTime = System.currentTimeMillis(),
            isActive = true
        )
        _uiState.value = _uiState.value.copy(
            isTracking = true,
            isPaused = false,
            currentSession = session,
            runPath = emptyList(),
            screenState = RunScreenState.RUNNING
        )
    }
    
    fun pauseTracking() {
        _uiState.value = _uiState.value.copy(
            isTracking = false,
            isPaused = true,
            screenState = RunScreenState.PAUSED_WITH_OVERLAY,
            showMapInPaused = false
        )
    }
    
    fun resumeTracking() {
        _uiState.value = _uiState.value.copy(
            isTracking = true,
            isPaused = false,
            screenState = RunScreenState.RUNNING
        )
    }
    
    fun showMapInPaused() {
        _uiState.value = _uiState.value.copy(
            screenState = RunScreenState.PAUSED_WITH_MAP,
            showMapInPaused = true
        )
    }
    
    fun hideMapInPaused() {
        _uiState.value = _uiState.value.copy(
            screenState = RunScreenState.PAUSED_WITH_OVERLAY,
            showMapInPaused = false
        )
    }
    
    fun finishRun() {
        val currentState = _uiState.value
        val session = currentState.currentSession?.copy(
            endTime = System.currentTimeMillis(),
            path = currentState.runPath,
            isActive = false
        )
        
        session?.let {
            viewModelScope.launch {
                userRepo.saveRunSession(it)
            }
        }
        
        _uiState.value = currentState.copy(
            isTracking = false,
            isPaused = false,
            currentSession = session,
            screenState = RunScreenState.SUMMARY
        )
    }

    fun openMaps() {
        // Ensure we have some data to show for WORLD / FRIENDS.
        val current = _uiState.value
        val ensured = ensureDemoOwners(current)
        _uiState.value = ensured.copy(
            screenState = RunScreenState.MAPS,
            mapsTab = MapsTab.PERSONAL
        )
    }

    fun backToSummaryFromMaps() {
        _uiState.value = _uiState.value.copy(screenState = RunScreenState.SUMMARY)
    }

    fun setMapsTab(tab: MapsTab) {
        _uiState.value = _uiState.value.copy(mapsTab = tab)
    }
    
    fun stopTracking() {
        val currentState = _uiState.value
        val session = currentState.currentSession?.copy(
            endTime = System.currentTimeMillis(),
            path = currentState.runPath,
            isActive = false
        )
        
        session?.let {
            viewModelScope.launch {
                userRepo.saveRunSession(it)
            }
        }
        
        _uiState.value = currentState.copy(
            isTracking = false,
            isPaused = false,
            currentSession = session,
            screenState = RunScreenState.READY_TO_START
        )
    }
    
    private fun updateCurrentSession() {
        // Throttle session updates to prevent UI blocking
        viewModelScope.launch(Dispatchers.Default) {
            val currentState = _uiState.value
            val session = currentState.currentSession ?: return@launch
            
            val path = currentState.runPath
            if (path.size < 2) return@launch
            
            // Calculate distance on background thread
            var distance = 0.0
            for (i in 1 until path.size) {
                distance += calculateDistance(path[i - 1], path[i])
            }
            
            // Calculate duration
            val duration = if (session.startTime > 0) {
                (System.currentTimeMillis() - session.startTime) / 1000
            } else 0L
            
            // Calculate average pace
            val pace = if (distance > 0 && duration > 0) {
                val paceSeconds = (duration / (distance / 1000)).toInt()
                val minutes = paceSeconds / 60
                val seconds = paceSeconds % 60
                "${minutes}'${seconds.toString().padStart(2, '0')}''"
            } else {
                "0'00''"
            }
            
            val updatedSession = session.copy(
                path = path,
                distance = distance,
                duration = duration,
                averagePace = pace
            )
            
            // Update UI on main thread
            withContext(Dispatchers.Main) {
                _uiState.value = currentState.copy(currentSession = updatedSession)
            }
        }
    }
    
    fun searchLocation(query: String) {
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(searchResults = emptyList())
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            locationRepo.searchLocation(query)
                .onSuccess { results ->
                    _uiState.value = _uiState.value.copy(
                        searchResults = results,
                        isLoading = false
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message,
                        isLoading = false
                    )
                }
        }
    }
    
    fun getRoute(start: LocationModel, end: LocationModel) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            locationRepo.getRoute(
                start.latitude, start.longitude,
                end.latitude, end.longitude
            )
                .onSuccess { route ->
                    _uiState.value = _uiState.value.copy(
                        selectedRoute = route,
                        isLoading = false
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message,
                        isLoading = false
                    )
                }
        }
    }
    
    fun startAreaCapture() {
        _uiState.value = _uiState.value.copy(
            isCapturingArea = true,
            currentAreaPolygon = emptyList()
        )
    }
    
    fun addPointToArea(location: LocationModel) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            currentAreaPolygon = currentState.currentAreaPolygon + location
        )
    }
    
    fun finishAreaCapture() {
        val currentState = _uiState.value
        if (currentState.currentAreaPolygon.size >= 3) {
            val area = locationRepo.calculateArea(currentState.currentAreaPolygon)
            val areaModel = AreaModel(
                polygon = currentState.currentAreaPolygon,
                area = area
            )
            
            _uiState.value = currentState.copy(
                isCapturingArea = false,
                capturedAreas = currentState.capturedAreas + areaModel,
                currentAreaPolygon = emptyList()
            )
        } else {
            _uiState.value = currentState.copy(
                isCapturingArea = false,
                currentAreaPolygon = emptyList(),
                error = "At least 3 points required for area capture"
            )
        }
    }
    
    fun clearRoute() {
        _uiState.value = _uiState.value.copy(selectedRoute = null)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    private fun calculateDistance(loc1: LocationModel, loc2: LocationModel): Double {
        val earthRadius = 6371000.0 // meters
        val dLat = Math.toRadians(loc2.latitude - loc1.latitude)
        val dLon = Math.toRadians(loc2.longitude - loc1.longitude)
        val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
                kotlin.math.cos(Math.toRadians(loc1.latitude)) *
                kotlin.math.cos(Math.toRadians(loc2.latitude)) *
                kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        return earthRadius * c
    }

    private fun ensureDemoOwners(state: StartRunUiState): StartRunUiState {
        // If we already have demo owners, keep them stable.
        if (state.worldOwners.isNotEmpty() || state.friendsOwners.isNotEmpty()) return state

        val loc = state.currentLocation ?: LocationModel(0.0, 0.0)

        // If the user has no captured areas yet, we still keep PERSONAL empty,
        // but WORLD/FRIENDS will show example zones so the screen isn't blank.
        val youAreas = state.capturedAreas

        fun squareArea(center: LocationModel, meters: Double): AreaModel {
            val lat = center.latitude
            val lon = center.longitude
            val latDelta = meters / 111_320.0
            val lonDelta = meters / (111_320.0 * cos(Math.toRadians(lat)).coerceAtLeast(0.2))
            val poly = listOf(
                LocationModel(lat + latDelta, lon - lonDelta),
                LocationModel(lat + latDelta, lon + lonDelta),
                LocationModel(lat - latDelta, lon + lonDelta),
                LocationModel(lat - latDelta, lon - lonDelta)
            )
            val area = try {
                locationRepo.calculateArea(poly)
            } catch (_: Throwable) {
                0.0
            }
            return AreaModel(polygon = poly, area = area)
        }

        val world = buildList {
            // You (green) if you have data
            if (youAreas.isNotEmpty()) {
                add(ZoneOwner(id = "you", displayName = "You", colorArgb = 0xFFB8FF3A.toInt(), areas = youAreas))
            }
            // Two "global" users
            add(
                ZoneOwner(
                    id = "world_1",
                    displayName = "World User 1",
                    colorArgb = 0xFFF04AA8.toInt(), // pink-ish
                    areas = listOf(squareArea(loc.copy(latitude = loc.latitude + 0.0015, longitude = loc.longitude + 0.0010), 120.0))
                )
            )
            add(
                ZoneOwner(
                    id = "world_2",
                    displayName = "World User 2",
                    colorArgb = 0xFF6BEA5B.toInt(), // green-ish
                    areas = listOf(squareArea(loc.copy(latitude = loc.latitude - 0.0018, longitude = loc.longitude - 0.0008), 140.0))
                )
            )
        }

        val friends = buildList {
            add(
                ZoneOwner(
                    id = "friend_1",
                    displayName = "Friend 1",
                    colorArgb = 0xFF7C5CFF.toInt(), // purple
                    areas = listOf(squareArea(loc.copy(latitude = loc.latitude + 0.0009, longitude = loc.longitude - 0.0014), 110.0))
                )
            )
            add(
                ZoneOwner(
                    id = "friend_2",
                    displayName = "Friend 2",
                    colorArgb = 0xFFFF8A3D.toInt(), // orange
                    areas = listOf(squareArea(loc.copy(latitude = loc.latitude - 0.0010, longitude = loc.longitude + 0.0016), 130.0))
                )
            )
        }

        return state.copy(worldOwners = world, friendsOwners = friends)
    }
}

