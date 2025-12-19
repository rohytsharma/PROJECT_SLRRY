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
import com.example.slrry_10.repository.UserRepoImpl
import com.example.slrry_10.ui.*
import com.example.slrry_10.ui.theme.SLRRY_10Theme
import com.example.slrry_10.viewmodel.StartRunViewModel
import com.example.slrry_10.viewmodel.StartRunUiState
class PausedOverlayActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SLRRY_10Theme {
                PausedOverlayScreen(
                    onContinueClick = {
                        val intent = Intent(this@PausedOverlayActivity, RunningActivity::class.java)
                        startActivity(intent)
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        finish()
                    },
                    onMapClick = {
                        val intent = Intent(this@PausedOverlayActivity, PausedMapActivity::class.java)
                        startActivity(intent)
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    },
                    onFinishClick = {
                        // In the new single-activity flow, this screen isn't used.
                        // Keep a safe fallback here.
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun PausedOverlayScreen(
    onContinueClick: () -> Unit,
    onMapClick: () -> Unit,
    onFinishClick: () -> Unit,
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

    PausedOverlayScreenBody(
        uiState = uiState,
        onContinueClick = onContinueClick,
        onMapClick = onMapClick,
        onFinish = {
            viewModel.finishRun()
            onFinishClick()
        }
    )
}

@Composable
fun PausedOverlayScreenBody(
    uiState: StartRunUiState,
    onContinueClick: () -> Unit,
    onMapClick: () -> Unit,
    onFinish: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // Top Status Bar
        TopStatusBar(
            hasLocationPermission = true,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(2f)
        )

        // Center: Large Distance Display
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .zIndex(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = String.format("%.2f", (uiState.currentSession?.distance ?: 0.0) / 1000.0).replace(".", ","),
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "Distance (Km)",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }

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
                    label = "area captured",
                    icon = Icons.Default.Castle
                )
            }

            // Three Circular Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Map Button (White with green border)
                FloatingActionButton(
                    onClick = onMapClick,
                    modifier = Modifier.size(64.dp),
                    containerColor = Color.White,
                    shape = CircleShape
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(2.dp, Color(0xFF4CAF50), CircleShape)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = "Map",
                            tint = Color.Black,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Continue Button (Green)
                FloatingActionButton(
                    onClick = onContinueClick,
                    modifier = Modifier.size(64.dp),
                    containerColor = Color(0xFF4CAF50),
                    shape = CircleShape
                ) {
                    Text(
                        text = "CONTINUE",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }

                // Finish Button (Black with long press)
                FloatingActionButton(
                    onClick = onFinish,
                    modifier = Modifier.size(64.dp),
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

@Preview(showBackground = true, name = "Paused Overlay Screen", showSystemUi = true, device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=portrait")
@Composable
fun PausedOverlayScreenPreview() {
    SLRRY_10Theme {
        PausedOverlayScreenBody(
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
            onContinueClick = {},
            onMapClick = {},
            onFinish = {}
        )
    }
}




