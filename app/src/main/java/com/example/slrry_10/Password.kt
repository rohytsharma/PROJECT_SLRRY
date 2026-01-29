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
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import com.example.slrry_10.auth.AuthServiceLocator
import com.example.slrry_10.auth.canProceedFromPassword
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.example.slrry_10.ui.theme.FieldGrey
import com.example.slrry_10.ui.theme.Mint
import com.example.slrry_10.ui.theme.NeonAccent
import com.example.slrry_10.ui.theme.SLRRYTheme

class PasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SLRRYTheme {
                val onboardingViewModel: OnboardingViewModel = viewModel()
                val authManager = remember { AuthServiceLocator.authManager }
                var isLoading by remember { mutableStateOf(false) }
                var errorMessage by remember { mutableStateOf<String?>(null) }
                var infoMessage by remember { mutableStateOf<String?>(null) }
                Surface(modifier = Modifier.fillMaxSize()) {
                    SignUpScreen(
                        username = onboardingViewModel.username.value,
                        password = onboardingViewModel.password.value,
                        confirm = onboardingViewModel.confirmPassword.value,
                        onUsernameChange = { onboardingViewModel.username.value = it },
                        onPasswordChange = { onboardingViewModel.password.value = it },
                        onConfirmChange = { onboardingViewModel.confirmPassword.value = it },
                        isLoading = isLoading,
                        onNext = {
                            val email = onboardingViewModel.username.value.trim()
                            val password = onboardingViewModel.password.value
                            val confirm = onboardingViewModel.confirmPassword.value

                            errorMessage = null
                            infoMessage = null
                            if (email.isBlank()) {
                                errorMessage = "Please enter your email."
                                return@SignUpScreen
                            }
                            if (!canProceedFromPassword(password, confirm)) {
                                errorMessage = "Passwords do not match or are blank."
                                return@SignUpScreen
                            }

                            isLoading = true
                            authManager.registerWithEmail(email, password) { result ->
                                result
                                    .onSuccess { user ->
                                        sendEmailVerification(user) { verifyRes ->
                                            verifyRes.onSuccess {
                                                infoMessage = "Verification link sent to $email. Please verify your email."
                                            }
                                        }
                                        authManager.ensureUserDoc(user, displayName = null) { _ -> }
                                        onboardingViewModel.currentStep.value = 1
                                        startActivity(Intent(this@PasswordActivity, EnterNameActivity::class.java))
                                    }
                                    .onFailure { e ->
                                        errorMessage = when (e) {
                                            is FirebaseNetworkException ->
                                                "Network error during Firebase verification (reCAPTCHA). " +
                                                    "If you're on an emulator, try a physical device OR add your app SHA-1 in Firebase and re-download google-services.json."
                                            is FirebaseAuthException -> when (e.errorCode) {
                                                "ERROR_EMAIL_ALREADY_IN_USE" -> "This email is already registered. Please log in."
                                                "ERROR_INVALID_EMAIL" -> "Invalid email address."
                                                "ERROR_WEAK_PASSWORD" -> "Password is too weak (min 6 characters)."
                                                else -> e.message ?: "Registration failed."
                                            }
                                            else -> e.message ?: "Registration failed."
                                        }
                                    }
                                isLoading = false
                            }
                        }
                    )

                    if (infoMessage != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Text(
                                text = infoMessage!!,
                                color = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    if (errorMessage != null) {
                        // Simple overlay message (keeps UI minimal)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Text(
                                text = errorMessage!!,
                                color = androidx.compose.ui.graphics.Color(0xFFB00020),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun sendEmailVerification(
    user: FirebaseUser,
    onResult: (Result<Unit>) -> Unit
) {
    user.sendEmailVerification()
        .addOnSuccessListener { onResult(Result.success(Unit)) }
        .addOnFailureListener { e -> onResult(Result.failure(e)) }
}

@Composable
fun SignUpScreen(
    username: String,
    password: String,
    confirm: String,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmChange: (String) -> Unit,
    isLoading: Boolean = false,
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
            label = "EMAIL",
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
            ),
            enabled = !isLoading
        ) {
            Text(text = if (isLoading) "Please wait..." else "Next", fontSize = 16.sp)
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
    var passwordVisible by rememberSaveable(isPassword) { mutableStateOf(false) }
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
                    val contentDesc = if (passwordVisible) "Hide password" else "Show password"
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            // Standard UX: show "eye" when hidden (action=show), show "eye-off" when visible (action=hide)
                            imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
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