package com.example.slrry_10

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.slrry_10.ui.theme.Mint
import com.example.slrry_10.ui.theme.NeonAccent
import com.example.slrry_10.ui.theme.SLRRYTheme

class DistanceGoalActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SLRRYTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val onboardingViewModel: OnboardingViewModel = viewModel()
                    DistanceGoalScreen(
                        distanceKm = onboardingViewModel.weeklyDistanceKm.value ?: 20,
                        step = 5,
                        totalSteps = onboardingViewModel.totalSteps,
                        onDistanceChange = { onboardingViewModel.weeklyDistanceKm.value = it },
                        onBack = {
                            onboardingViewModel.currentStep.value = 4
                            finish()
                        },
                        onNext = {
                            onboardingViewModel.currentStep.value = 6
                            startActivity(
                                Intent(
                                    this@DistanceGoalActivity,
                                    RunningGoalActivity::class.java
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DistanceGoalScreen(
    distanceKm: Int,
    step: Int,
    totalSteps: Int,
    onDistanceChange: (Int) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val minDistance = 0f
    val maxDistance = 100f
    val snappedValue = remember(distanceKm, step) {
        ((distanceKm + step / 2) / step) * step
    }.coerceIn(minDistance.toInt(), maxDistance.toInt())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F1EB))
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Logo()
        Spacer(modifier = Modifier.height(28.dp))
        Progress(step = step, totalSteps = totalSteps)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "WEEKLY DISTANCE GOAL?",
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Select an option.",
            fontSize = 14.sp,
            color = Color(0xFF3F3F3F),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(48.dp))
        
        // Distance Slider
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val sliderValue = snappedValue.toFloat()
            Slider(
                value = sliderValue,
                onValueChange = { onDistanceChange(it.toInt()) },
                onValueChangeFinished = { onDistanceChange(snappedValue) },
                valueRange = minDistance..maxDistance,
                steps = ((maxDistance - minDistance) / step).toInt() - 1, // snap to 'step' increments
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = Mint,
                    activeTrackColor = Mint,
                    inactiveTrackColor = Color(0xFFD8D8D8)
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
    Text(
                text = "${snappedValue}km",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedNavButton(
                text = "Back",
                icon = { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") },
                modifier = Modifier.weight(1f),
                onClick = onBack
            )
            FilledNavButton(
                text = "Next",
                icon = { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next") },
                modifier = Modifier.weight(1f),
                onClick = onNext
            )
        }
    }
}

@Composable
private fun Logo() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "SLRRY",
            style = TextStyle(
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                fontStyle = FontStyle.Italic
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .width(140.dp)
                .height(6.dp)
                .background(
                    brush = Brush.linearGradient(
                        listOf(NeonAccent, NeonAccent.copy(alpha = 0.6f))
                    ),
                    shape = RoundedCornerShape(50)
                )
        )
    }
}

@Composable
private fun Progress(step: Int, totalSteps: Int) {
    val fraction = (step.toFloat() / totalSteps.toFloat()).coerceIn(0f, 1f)
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "$step/$totalSteps", fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(Color(0xFFEEEAE4), RoundedCornerShape(50))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(6.dp)
                    .background(Color(0xFF2F2F2F), RoundedCornerShape(50))
            )
        }
    }
}

@Composable
private fun OutlinedNavButton(
    text: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(6.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color.Black,
            containerColor = Color(0xFFF5F1EB)
        ),
        border = BorderStroke(1.dp, Color.Black)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            icon()
            Text(text = text, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun FilledNavButton(
    text: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(6.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Mint,
            contentColor = Color.Black
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = text, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            icon()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DistanceGoalPreview() {
    SLRRYTheme {
        DistanceGoalScreen(
            distanceKm = 20,
            step = 5,
            totalSteps = 7,
            onDistanceChange = {},
            onBack = {},
            onNext = {}
        )
    }
}
