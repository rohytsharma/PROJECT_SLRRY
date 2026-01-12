package com.example.slrry_10

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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.example.slrry_10.repository.LocationRepositoryImpl
import com.example.slrry_10.repository.UserRepoImpl
import com.example.slrry_10.ui.MapViewComponent
import com.example.slrry_10.ui.MapsHubScreen
import com.example.slrry_10.ui.theme.SLRRY_10Theme
import com.example.slrry_10.viewmodel.StartRunViewModel
import org.maplibre.android.maps.MapLibreMap

class MapsHubActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SLRRY_10Theme {
                val vm: StartRunViewModel = viewModel(
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return StartRunViewModel(UserRepoImpl(), LocationRepositoryImpl()) as T
                        }
                    }
                )
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
}


