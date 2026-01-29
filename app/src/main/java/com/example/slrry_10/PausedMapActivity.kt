package com.example.slrry_10

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.slrry_10.repository.LocationRepositoryImpl
import com.example.slrry_10.repository.FirebaseUserRepoImpl
import com.example.slrry_10.ui.*
import com.example.slrry_10.ui.theme.SLRRY_10Theme
import com.example.slrry_10.viewmodel.StartRunViewModel
import com.example.slrry_10.viewmodel.StartRunUiState
import org.maplibre.android.MapLibre

class PausedMapActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Required for MapLibre MapView to render tiles
        MapLibre.getInstance(this)
        enableEdgeToEdge()
        setContent {
            SLRRY_10Theme {
                PausedMapScreen(
                    onResumeClick = {
                        val intent = Intent(this@PausedMapActivity, RunningActivity::class.java)
                        startActivity(intent)
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        finish()
                    },
                    onContinueClick = {
                        val intent = Intent(this@PausedMapActivity, RunningActivity::class.java)
                        startActivity(intent)
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        finish()
                    },
                    onFinishClick = {
                        // In the new single-activity flow, this screen isn't used.
                        // Keep a safe fallback here.
                        finish()
                    },
                    onBackClick = {
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun PausedMapScreen(
    onResumeClick: () -> Unit,
    onContinueClick: () -> Unit,
    onFinishClick: () -> Unit,
    onBackClick: () -> Unit,
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

    PausedMapScreenBody(
        uiState = uiState,
        onResumeClick = onResumeClick,
        onContinueClick = onContinueClick,
        onFinish = {
            viewModel.finishRun()
            onFinishClick()
        },
        onBackClick = onBackClick
    )
}

@Composable
fun PausedMapScreenBody(
    uiState: StartRunUiState,
    onResumeClick: () -> Unit,
    onContinueClick: () -> Unit,
    onFinish: () -> Unit,
    onBackClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Map - fills entire screen
        MapViewComponent(
            mapView = null,
            mapLibreMap = null,
            uiState = uiState,
            onMapReady = { },
            showMap = true
        )

        // Top Status Bar
        TopStatusBar(
            hasLocationPermission = true,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(2f)
        )

        // Bottom Section: Metrics and Buttons
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .zIndex(2f)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Metrics Row
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

            // Buttons Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Resume Button (Green circular with >>)
                FloatingActionButton(
                    onClick = onResumeClick,
                    modifier = Modifier.size(64.dp),
                    containerColor = Color(0xFF4CAF50),
                    shape = CircleShape
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Resume",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Continue Button (White with green border)
                Button(
                    onClick = onContinueClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF4CAF50)),
                        width = 2.dp
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = "CONTINUE",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                // Finish Button (Black circular with long press)
                FloatingActionButton(
                    onClick = onFinish,
                    modifier = Modifier.size(56.dp),
                    containerColor = Color.Black,
                    shape = CircleShape
                ) {
                    Text(
                        text = "FINISH",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Paused Map Screen", showSystemUi = true, device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=portrait")
@Composable
fun PausedMapScreenPreview() {
    SLRRY_10Theme {
        PausedMapScreenBody(
            uiState = StartRunUiState(
                currentSession = com.example.slrry_10.model.RunSession(
                    id = "preview",
                    startTime = System.currentTimeMillis() - 300000,
                    isActive = false,
                    distance = 5000.0,
                    duration = 300,
                    averagePace = "5'00''"
                ),
                capturedAreas = listOf(
                    com.example.slrry_10.model.AreaModel(
                        polygon = emptyList(),
                        area = 100.0
                    )
                )
            ),
            onResumeClick = {},
            onContinueClick = {},
            onFinish = {},
            onBackClick = {}
        )
    }
}



