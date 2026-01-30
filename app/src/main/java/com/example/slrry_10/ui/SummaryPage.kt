package com.example.slrry_10.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.slrry_10.viewmodel.StartRunUiState

@Composable
fun SummaryPage(
    uiState: StartRunUiState,
    modifier: Modifier = Modifier,
    title: String = "Monday Morning Run",
    onContinue: () -> Unit = {}
) {
    val accent = Color(0xFFB8FF3A)
    val darkText = Color(0xFF111416)
    val muted = Color(0xFF7E868C)

    val distanceKm = (uiState.currentSession?.distance ?: 0.0) / 1000.0
    val durationText = formatDuration(uiState.currentSession?.duration ?: 0L)
    val avgPace = uiState.currentSession?.averagePace ?: "0'00''"
    val stepLength = "0.00 m"
    val calories = "0 kcal"
    val capturedAreaSqM = uiState.currentSession?.capturedAreas?.sumOf { it.area } ?: 0.0
    val area = when {
        capturedAreaSqM <= 0.0 -> "0 m²"
        capturedAreaSqM < 1.0 -> "<1 m²"
        else -> String.format("%.0f m²", capturedAreaSqM)
    }
    val elevation = "0 ft"

    Box(modifier = modifier.fillMaxSize()) {
        // Top bar (transparent over map)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { }) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = darkText)
            }
            IconButton(onClick = { }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = darkText)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 72.dp)
        ) {
            Text(
                text = title,
                fontSize = 26.sp,
                fontWeight = FontWeight.SemiBold,
                color = darkText
            )
            HorizontalDivider(
                modifier = Modifier.padding(top = 10.dp),
                color = darkText.copy(alpha = 0.15f)
            )
        }

        // Green metrics pill (like your screenshot)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 240.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = accent),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PillMetric(value = String.format("%.2f", distanceKm), unit = "km", label = "Distance", color = darkText)
                VerticalSeparator(darkText)
                PillMetric(value = durationText, unit = "", label = "Duration", color = darkText)
                VerticalSeparator(darkText)
                PillMetric(value = avgPace, unit = "", label = "Avg Pace", color = darkText)
            }
        }

        // Detail cards
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 360.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DetailCard(title = "Step Length", subtitle = "Distance between steps", value = stepLength, muted = muted, text = darkText)
            DetailCard(title = "Calories", subtitle = "Total energy burned", value = calories, muted = muted, text = darkText)
            DetailCard(title = "Your Area", subtitle = "Total area you captured", value = area, muted = muted, text = darkText)
            DetailCard(title = "Elevation Gain", subtitle = "Total height that you climb", value = elevation, muted = muted, text = darkText)
        }

        // Continue -> Maps screen
        Box(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = darkText)
            ) {
                Text("CONTINUE", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun PillMetric(value: String, unit: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color)
            if (unit.isNotBlank()) {
                Spacer(Modifier.width(4.dp))
                Text(unit, fontSize = 13.sp, color = color.copy(alpha = 0.8f))
            }
        }
        Text(label, fontSize = 12.sp, color = color.copy(alpha = 0.85f))
    }
}

@Composable
private fun VerticalSeparator(color: Color) {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(36.dp)
            .background(color.copy(alpha = 0.2f))
    )
}

@Composable
private fun DetailCard(title: String, subtitle: String, value: String, muted: Color, text: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = text)
                Text(subtitle, fontSize = 12.sp, color = muted)
            }
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = text)
        }
    }
}

