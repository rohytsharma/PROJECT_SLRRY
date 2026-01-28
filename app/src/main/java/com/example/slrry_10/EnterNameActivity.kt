package com.example.slrry_10

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.slrry_10.ui.theme.SLRRYTheme
import com.example.slrry_10.ui.theme.Mint
import com.example.slrry_10.ui.theme.NeonAccent

class EnterNameActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SLRRYTheme {
                val onboardingViewModel: OnboardingViewModel = viewModel()
                Surface(modifier = Modifier.fillMaxSize()) {
                    EnterNameScreen(
                        fullName = onboardingViewModel.fullName.value,
                        step = onboardingViewModel.currentStep.value,
                        totalSteps = onboardingViewModel.totalSteps,
                        onNameChange = { onboardingViewModel.fullName.value = it },
                        onNext = {
                            // Advance to the next logical step and navigate to the gender screen.
                            onboardingViewModel.currentStep.value = 2
                            startActivity(
                                Intent(
                                    this@EnterNameActivity,
                                    GenderActivity::class.java
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
fun EnterNameScreen(
    fullName: String,
    step: Int,
    totalSteps: Int,
    onNameChange: (String) -> Unit,
    onNext: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F1EB))
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Logo()
        Spacer(modifier = Modifier.height(36.dp))
        ProgressBar(step = step, totalSteps = totalSteps)
        Spacer(modifier = Modifier.height(28.dp))
    Text(
            text = "WHAT'S YOUR FULL NAME?",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(18.dp))
        OutlinedTextField(
            value = fullName,
            onValueChange = onNameChange,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            textStyle = TextStyle(fontSize = 16.sp),
            shape = RoundedCornerShape(6.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF3D3D3D),
                unfocusedBorderColor = Color(0xFF3D3D3D),
                cursorColor = Color.Black
            )
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Mint,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            enabled = fullName.isNotBlank()
        ) {
            Text(text = "Next", fontSize = 16.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EnterNamePreview() {
    SLRRYTheme {
        EnterNameScreen(
            fullName = "Alex Runner",
            step = 1,
            totalSteps = 7,
            onNameChange = {},
            onNext = {}
        )
    }
}

@Composable
private fun Logo() {
    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
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
private fun ProgressBar(step: Int, totalSteps: Int = 3) {
    val fraction = (step.toFloat() / totalSteps.toFloat()).coerceIn(0f, 1f)
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$step/$totalSteps",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
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