package com.example.slrry_10

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.slrry_10.repository.UserProfileStore
import com.example.slrry_10.ui.theme.Mint
import com.example.slrry_10.ui.theme.NeonAccent
import com.example.slrry_10.ui.theme.SLRRYTheme

class HeightActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SLRRYTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val onboardingViewModel: OnboardingViewModel = viewModel()
                    HeightScreen(
                        selectedHeight = onboardingViewModel.heightCm.value ?: 180,
                        step = 3,
                        totalSteps = onboardingViewModel.totalSteps,
                        onHeightChange = { onboardingViewModel.heightCm.value = it },
                        onBack = {
                            onboardingViewModel.currentStep.value = 2
                            finish()
                        },
                        onNext = {
                            val heightCm = onboardingViewModel.heightCm.value ?: 180
                            lifecycleScope.launch {
                                UserProfileStore.updateCurrentUserFields(
                                    mapOf(
                                        "heightCm" to heightCm,
                                        "updatedAt" to System.currentTimeMillis()
                                    )
                                )
                            }
                            onboardingViewModel.currentStep.value = 4
                            startActivity(
                                Intent(
                                    this@HeightActivity,
                                    ExperienceActivity::class.java
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
fun HeightScreen(
    selectedHeight: Int,
    step: Int,
    totalSteps: Int,
    onHeightChange: (Int) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val heightRange = (145..250).toList()
    
    // Calculate initial scroll position to center the selected height
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = heightRange.indexOf(selectedHeight).coerceAtLeast(0)
    )
    val scope = rememberCoroutineScope()
    LaunchedEffect(selectedHeight) {
        scope.launch {
            val idx = heightRange.indexOf(selectedHeight).coerceAtLeast(0)
            listState.animateScrollToItem(idx)
        }
    }

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
            text = "HOW TALL ARE YOU?",
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Select your height.",
            fontSize = 14.sp,
            color = Color(0xFF3F3F3F)
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        // Height Picker
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(heightRange) { height ->
                    HeightOption(
                        height = height,
                        selected = selectedHeight == height,
                        onClick = { onHeightChange(height) }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
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
private fun HeightOption(
    height: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clickable { onClick() }
            .then(
                if (selected) {
                    Modifier
                        .background(Color(0xFFF5F1EB))
                        .border(1.dp, Color.Black, RoundedCornerShape(0.dp))
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            // Draw lines above and below selected item
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.Black)
                    .align(Alignment.TopCenter)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.Black)
                    .align(Alignment.BottomCenter)
            )
        }
        Text(
            text = "$height cm",
            fontSize = 18.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) Color.Black else Color(0xFF808080)
        )
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
            containerColor = Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFB8B8B8))
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
            contentColor = MaterialTheme.colorScheme.onPrimary
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
private fun HeightPreview() {
    SLRRYTheme {
        HeightScreen(
            selectedHeight = 180,
            step = 3,
            totalSteps = 7,
            onHeightChange = {},
            onBack = {},
            onNext = {}
        )
    }
}
