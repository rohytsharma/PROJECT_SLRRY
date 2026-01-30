package com.example.slrry_10.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.tooling.preview.Preview
import com.example.slrry_10.viewmodel.StartRunUiState
import com.example.slrry_10.ui.theme.SLRRY_10Theme

fun formatRunningDuration(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, secs)
    } else {
        String.format("%02d:%02d", minutes, secs)
    }
}

@Composable
fun RunningMetrics(
    uiState: StartRunUiState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avg Pace with icon
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Icon in green square
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF4CAF50)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = uiState.currentSession?.averagePace ?: "0'00''",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Text(
                text = "Avg Pace",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        
        // Duration with icon
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Icon in green square
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF4CAF50)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = formatRunningDuration(uiState.currentSession?.duration ?: 0L),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Text(
                text = "Duration",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        
        // Area Captured with icon
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Icon in green square
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF4CAF50)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Castle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = String.format("%.2fm²", uiState.capturedAreas.sumOf { it.area }),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Text(
                text = "area captured",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun RunningActionButtons(
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Resume/Start Button (Green circular with white >> arrows)
        FloatingActionButton(
            onClick = onResumeClick,
            modifier = Modifier.size(72.dp),
            containerColor = Color(0xFF4CAF50),
            shape = CircleShape
        ) {
            // Double chevron right (>>) icon in white
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Resume",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Pause Button (White with light green border)
        Button(
            onClick = onPauseClick,
            modifier = Modifier
                .weight(1f)
                .height(64.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF90EE90)),
                width = 2.dp
            ),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(32.dp)
        ) {
            Text(
                text = "PAUSE",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
fun RunningBottomButtons(
    uiState: StartRunUiState,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Metrics section
        RunningMetrics(uiState = uiState)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Action Buttons
        RunningActionButtons(
            onPauseClick = onPauseClick,
            onResumeClick = onResumeClick
        )
    }
}

@Preview(showBackground = true, name = "Running Buttons", showSystemUi = false)
@Composable
fun RunningBottomButtonsPreview() {
    SLRRY_10Theme {
        RunningBottomButtons(
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
                )
            ),
            onPauseClick = {},
            onResumeClick = {}
        )
    }
}
