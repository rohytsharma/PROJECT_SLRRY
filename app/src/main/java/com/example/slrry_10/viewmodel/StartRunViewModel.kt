package com.example.slrry_10.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.slrry_10.model.AreaModel
import com.example.slrry_10.model.LocationModel
import com.example.slrry_10.model.RouteModel
import com.example.slrry_10.model.RunSession
import com.example.slrry_10.model.RunScreenState
import com.example.slrry_10.model.SearchResult
import com.example.slrry_10.repository.CapturedAreasRepository
import com.example.slrry_10.repository.FriendsRepository
import com.example.slrry_10.repository.LocationRepository
import com.example.slrry_10.repository.UserRepo
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.absoluteValue

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
    val isSimulatingLocation: Boolean = false,
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

    private var timerJob: Job? = null
    private var timerBaseMs: Long = 0L
    private var accumulatedDurationSec: Long = 0L

    private val friendsRepo = FriendsRepository()
    private val areasRepo = CapturedAreasRepository()
    private val auth = FirebaseAuth.getInstance()

    private var runStartLocation: LocationModel? = null
    private var hasCapturedThisRun: Boolean = false

    init {
        // Load user's captured areas for personal/friends maps.
        refreshMyCapturedAreas()
    }

    fun setSimulatingLocation(isSimulating: Boolean) {
        _uiState.value = _uiState.value.copy(isSimulatingLocation = isSimulating)
    }
    
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
                    maybeAutoCaptureClosedLoop(newPath, location)
                }
            }
        }
    }
    
    fun startTracking() {
        val now = System.currentTimeMillis()
        // Reset timer accounting for a fresh run.
        timerJob?.cancel()
        accumulatedDurationSec = 0L
        timerBaseMs = now

        val startLoc = _uiState.value.currentLocation
        runStartLocation = startLoc
        hasCapturedThisRun = false

        val session = RunSession(
            id = System.currentTimeMillis().toString(),
            startTime = now,
            isActive = true
        )
        _uiState.value = _uiState.value.copy(
            isTracking = true,
            isPaused = false,
            currentSession = session,
            runPath = startLoc?.let { listOf(it) } ?: emptyList(),
            screenState = RunScreenState.RUNNING
        )

        startTimer()
    }
    
    fun pauseTracking() {
        val now = System.currentTimeMillis()
        accumulatedDurationSec = computeDurationNow(now)
        timerJob?.cancel()
        timerJob = null
        _uiState.value = _uiState.value.copy(
            isTracking = false,
            isPaused = true,
            currentSession = _uiState.value.currentSession?.copy(duration = accumulatedDurationSec),
            screenState = RunScreenState.PAUSED_WITH_OVERLAY,
            showMapInPaused = false
        )
    }
    
    fun resumeTracking() {
        val now = System.currentTimeMillis()
        timerBaseMs = now
        startTimer()
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
        val now = System.currentTimeMillis()
        val duration = computeDurationNow(now)
        val distance = computeDistanceForPath(currentState.runPath)
        val pace = computeAveragePace(distance, duration)

        timerJob?.cancel()
        timerJob = null
        accumulatedDurationSec = duration

        val session = currentState.currentSession?.copy(
            endTime = now,
            path = currentState.runPath,
            distance = distance,
            duration = duration,
            averagePace = pace,
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

    private fun maybeAutoCaptureClosedLoop(path: List<LocationModel>, current: LocationModel) {
        if (hasCapturedThisRun) return
        val start = runStartLocation ?: return
        if (path.size < 12) return

        val distanceMeters = computeDistanceForPath(path)
        if (distanceMeters < 50.0) return

        val distToStart = calculateDistance(start, current)
        if (distToStart > 15.0) return

        // Close the polygon explicitly for stable area calcs.
        val polygon = buildList {
            addAll(path)
            if (isNotEmpty()) {
                val first = first()
                val last = last()
                if (first.latitude != last.latitude || first.longitude != last.longitude) add(first)
            }
        }

        val area = try { locationRepo.calculateArea(polygon) } catch (_: Throwable) { 0.0 }
        // Allow small loops on emulator; still ignore truly degenerate polygons.
        if (area <= 0.5) return

        val areaModel = AreaModel(polygon = polygon, area = area)
        hasCapturedThisRun = true

        val state = _uiState.value
        val updatedSession = state.currentSession?.copy(
            capturedAreas = (state.currentSession.capturedAreas + areaModel)
        )
        _uiState.value = state.copy(
            capturedAreas = state.capturedAreas + areaModel,
            currentSession = updatedSession
        )

        // Persist so it shows up in personal/friends/world maps + leaderboard.
        viewModelScope.launch(Dispatchers.IO) {
            areasRepo.addArea(areaModel)
        }
    }

    fun openMaps() {
        _uiState.value = _uiState.value.copy(
            screenState = RunScreenState.MAPS,
            mapsTab = MapsTab.PERSONAL
        )
        refreshWorldOwners()
        refreshFriendsOwners()
    }

    fun backToSummaryFromMaps() {
        _uiState.value = _uiState.value.copy(screenState = RunScreenState.SUMMARY)
    }

    fun setMapsTab(tab: MapsTab) {
        _uiState.value = _uiState.value.copy(mapsTab = tab)
        when (tab) {
            MapsTab.WORLD -> refreshWorldOwners()
            MapsTab.FRIENDS -> refreshFriendsOwners()
            MapsTab.PERSONAL -> Unit
        }
    }
    
    fun stopTracking() {
        val currentState = _uiState.value
        val now = System.currentTimeMillis()
        val duration = computeDurationNow(now)
        val distance = computeDistanceForPath(currentState.runPath)
        val pace = computeAveragePace(distance, duration)

        timerJob?.cancel()
        timerJob = null
        accumulatedDurationSec = duration

        val session = currentState.currentSession?.copy(
            endTime = now,
            path = currentState.runPath,
            distance = distance,
            duration = duration,
            averagePace = pace,
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
            
            // Duration comes from the run timer (so paused time isn't counted).
            val duration = currentState.currentSession?.duration ?: 0L
            val pace = computeAveragePace(distance, duration)
            
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
            // Persist for friends map + leaderboard.
            viewModelScope.launch {
                areasRepo.addArea(areaModel)
            }
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

    private fun refreshWorldOwners() {
        viewModelScope.launch {
            val users = friendsRepo.listAllUsers(limit = 200, includeSelf = true)
            if (users.isEmpty()) {
                _uiState.value = _uiState.value.copy(worldOwners = emptyList())
                return@launch
            }

            val owners = coroutineScope {
                users.map { u ->
                    async(Dispatchers.IO) {
                        val areas = areasRepo.getAreasForUser(u.uid)
                        ZoneOwner(
                            id = u.uid,
                            displayName = if (u.uid == auth.currentUser?.uid) "You" else u.displayName,
                            colorArgb = colorForUid(u.uid),
                            areas = areas
                        )
                    }
                }.awaitAll()
            }
                // show biggest capturers first; map rendering will still include all
                .sortedByDescending { it.areas.sumOf { a -> a.area } }

            _uiState.value = _uiState.value.copy(worldOwners = owners)
        }
    }

    private fun refreshMyCapturedAreas() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val areas = areasRepo.getAreasForUser(uid)
            _uiState.value = _uiState.value.copy(capturedAreas = areas)
        }
    }

    private fun refreshFriendsOwners() {
        viewModelScope.launch {
            val friends = friendsRepo.listFriends()
            if (friends.isEmpty()) {
                _uiState.value = _uiState.value.copy(friendsOwners = emptyList())
                return@launch
            }

            val owners = friends.map { f ->
                val areas = areasRepo.getAreasForUser(f.uid)
                ZoneOwner(
                    id = f.uid,
                    displayName = f.displayName,
                    colorArgb = colorForUid(f.uid),
                    areas = areas
                )
            }
            _uiState.value = _uiState.value.copy(friendsOwners = owners)
        }
    }

    private fun colorForUid(uid: String): Int {
        // Deterministic bright-ish palette by hashing uid.
        val h = uid.hashCode().absoluteValue
        val r = 80 + (h % 140)
        val g = 80 + ((h / 7) % 140)
        val b = 80 + ((h / 13) % 140)
        return (0xFF shl 24) or (r shl 16) or (g shl 8) or b
    }

    private fun startTimer() {
        // Only one timer at a time.
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val state = _uiState.value
                if (!state.isTracking) continue
                val now = System.currentTimeMillis()
                val duration = computeDurationNow(now)
                val session = state.currentSession ?: continue
                val pace = computeAveragePace(session.distance, duration)
                _uiState.value = state.copy(
                    currentSession = session.copy(
                        duration = duration,
                        averagePace = pace
                    )
                )
            }
        }
    }

    private fun computeDurationNow(nowMs: Long): Long {
        val elapsedSec = ((nowMs - timerBaseMs).coerceAtLeast(0L)) / 1000L
        return accumulatedDurationSec + elapsedSec
    }

    private fun computeDistanceForPath(path: List<LocationModel>): Double {
        if (path.size < 2) return 0.0
        var distance = 0.0
        for (i in 1 until path.size) {
            distance += calculateDistance(path[i - 1], path[i])
        }
        return distance
    }

    private fun computeAveragePace(distanceMeters: Double, durationSec: Long): String {
        if (distanceMeters <= 0.0 || durationSec <= 0L) return "0'00''"
        val paceSeconds = (durationSec / (distanceMeters / 1000.0)).toInt()
        val minutes = paceSeconds / 60
        val seconds = paceSeconds % 60
        return "${minutes}'${seconds.toString().padStart(2, '0')}''"
    }
}

