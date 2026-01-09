package com.example.slrry

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.slrry.ui.theme.SLRRYTheme
import com.example.slrry.ui.theme.FieldGrey
import com.example.slrry.ui.theme.Mint
import com.example.slrry.ui.theme.NeonAccent
import com.example.slrry.R

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SLRRYTheme {
                val onboardingViewModel: OnboardingViewModel = viewModel()
                Surface(modifier = Modifier.fillMaxSize()) {
                    SignUpScreen(
                        username = onboardingViewModel.username.value,
                        password = onboardingViewModel.password.value,
                        confirm = onboardingViewModel.confirmPassword.value,
                        onUsernameChange = { onboardingViewModel.username.value = it },
                        onPasswordChange = { onboardingViewModel.password.value = it },
                        onConfirmChange = { onboardingViewModel.confirmPassword.value = it },
                        onNext = {
                            // basic match check; keep logic thin here
                            if (onboardingViewModel.password.value == onboardingViewModel.confirmPassword.value &&
                                onboardingViewModel.password.value.isNotBlank()
                            ) {
                                // First step completed, move to name entry (step 1 of progress flow)
                                onboardingViewModel.currentStep.value = 1
                                startActivity(
                                    Intent(
                                        this@MainActivity,
                                        EnterNameActivity::class.java
                                    )
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SignUpScreen(
    username: String,
    password: String,
    confirm: String,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmChange: (String) -> Unit,
    onNext: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Logo()
        Spacer(modifier = Modifier.height(32.dp))
        LabeledInput(
            label = "USERNAME",
            value = username,
            onValueChange = onUsernameChange
        )
        Spacer(modifier = Modifier.height(18.dp))
        LabeledInput(
            label = "PASSWORD",
            value = password,
            onValueChange = onPasswordChange,
            isPassword = true
        )
        Spacer(modifier = Modifier.height(18.dp))
        LabeledInput(
            label = "CONFIRM PASSWORD",
            value = confirm,
            onValueChange = onConfirmChange,
            isPassword = true
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Mint,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(text = "Next", fontSize = 16.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpPreview() {
    SLRRYTheme {
        SignUpScreen(
            username = "user",
            password = "password123!",
            confirm = "password123!",
            onUsernameChange = {},
            onPasswordChange = {},
            onConfirmChange = {},
            onNext = {}
        )
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
                .width(130.dp)
                .height(6.dp)
                .background(
                    brush = Brush.linearGradient(
                        listOf(NeonAccent, NeonAccent.copy(alpha = 0.65f))
                    ),
                    shape = RoundedCornerShape(50)
                )
        )
    }
}

@Composable
private fun LabeledInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isPassword: Boolean = false
) {
    var passwordVisible by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontWeight = FontWeight.Black,
            fontSize = 22.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(6.dp),
            textStyle = TextStyle(fontSize = 16.sp),
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = {
                if (isPassword) {
                    val icon = if (passwordVisible) R.drawable.baseline_visibility_24 else R.drawable.baseline_visibility_off_24
                    val contentDesc = if (passwordVisible) "Hide password" else "Show password"
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            painter = painterResource(id = icon),
                            contentDescription = contentDesc
                        )
                    }
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = FieldGrey,
                unfocusedContainerColor = FieldGrey,
                disabledContainerColor = FieldGrey,
                focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                disabledIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                cursorColor = androidx.compose.ui.graphics.Color.Black
            )
        )
    }
}