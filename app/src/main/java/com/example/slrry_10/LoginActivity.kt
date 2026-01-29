package com.example.slrry_10

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.slrry_10.auth.AuthServiceLocator
import com.example.slrry_10.ui.theme.SLRRY_10Theme
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException

private val AccentGreen = Color(0xFFB5FF00)
private val FieldBg = Color(0xFFF6F6F6)
private val PageBg = Color.White

private enum class LoginRoute {
    LOGIN,
    FORGOT_EMAIL
}

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SLRRY_10Theme {
                var route by remember { mutableStateOf(LoginRoute.LOGIN) }
                val authManager = remember { AuthServiceLocator.authManager }

                var email by remember { mutableStateOf("") }
                var password by remember { mutableStateOf("") }
                var isLoading by remember { mutableStateOf(false) }
                var errorMessage by remember { mutableStateOf<String?>(null) }
                var infoMessage by remember { mutableStateOf<String?>(null) }

                when (route) {
                    LoginRoute.LOGIN -> LoginScreen(
                        onBack = { finish() },
                        email = email,
                        password = password,
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        infoMessage = infoMessage,
                        onEmailChange = { email = it },
                        onPasswordChange = { password = it },
                        onLogin = { enteredEmail, enteredPassword ->
                            val e = enteredEmail.trim()
                            val p = enteredPassword
                            if (e.isBlank() || p.isBlank()) {
                                errorMessage = "Please enter email and password."
                                return@LoginScreen
                            }

                            errorMessage = null
                            infoMessage = null
                            isLoading = true
                            authManager.loginWithEmail(e, p) { result ->
                                result
                                    .onSuccess { user ->
                                        authManager.ensureUserDoc(user, displayName = null) { _ -> }
                                        startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                                        finish()
                                    }
                                    .onFailure { ex ->
                                        errorMessage = when (ex) {
                                            is FirebaseNetworkException ->
                                                "Network error. Check your internet connection and try again."
                                            is FirebaseAuthException -> when (ex.errorCode) {
                                                "ERROR_WRONG_PASSWORD" -> "Wrong password."
                                                "ERROR_USER_NOT_FOUND" -> "No account found for this email."
                                                "ERROR_INVALID_EMAIL" -> "Invalid email address."
                                                else -> ex.message ?: "Login failed."
                                            }
                                            else -> ex.message ?: "Login failed."
                                        }
                                    }
                                isLoading = false
                            }
                        },
                        onForgotPassword = { route = LoginRoute.FORGOT_EMAIL }
                    )

                    LoginRoute.FORGOT_EMAIL -> ForgotPasswordEmailScreen(
                        onBack = { route = LoginRoute.LOGIN },
                        onSubmitEmail = { enteredEmail ->
                            val e = enteredEmail.trim()
                            if (e.isBlank()) return@ForgotPasswordEmailScreen

                            errorMessage = null
                            infoMessage = null
                            isLoading = true
                            authManager.sendPasswordResetEmail(e) { result ->
                                result
                                    .onSuccess {
                                        infoMessage = "Reset link sent to $e."
                                        route = LoginRoute.LOGIN
                                    }
                                    .onFailure { ex ->
                                        errorMessage = ex.message ?: "Failed to send reset email."
                                    }
                                isLoading = false
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ForgotPasswordEmailScreen(
    onBack: () -> Unit,
    onSubmitEmail: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    val canSubmit = email.trim().isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBg)
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Reset\npassword",
                fontSize = 35.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(AccentGreen, CircleShape)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Enter your email and we’ll send a password reset link.",
            color = Color.Black,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Check your inbox and follow the link to reset your password.",
            color = Color.Gray,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                if (showError) showError = false
            },
            placeholder = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                unfocusedContainerColor = FieldBg,
                focusedContainerColor = FieldBg
            )
        )

        if (showError) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Please enter your email.",
                color = Color(0xFFD32F2F),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        Button(
            onClick = {
                val trimmed = email.trim()
                if (trimmed.isBlank()) {
                    showError = true
                } else {
                    onSubmitEmail(trimmed)
                }
            },
            enabled = canSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
        ) {
            Text(
                text = "Submit",
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ForgotPasswordCodeScreen(
    email: String,
    onBack: () -> Unit,
    onSubmitCode: (String) -> Unit,
    onResendCode: (() -> Unit)? = null
) {
    var code by remember { mutableStateOf("") }
    val canSubmit = code.trim().isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBg)
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Enter\ncode",
                fontSize = 35.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(AccentGreen, CircleShape)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (email.isBlank()) {
                "Enter the verification code sent to your email."
            } else {
                "Enter the verification code sent to $email."
            },
            color = Color.Black,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Change email",
            modifier = Modifier
                .align(Alignment.Start)
                .clickable { onBack() },
            color = Color.Black,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(18.dp))

        OutlinedTextField(
            value = code,
            onValueChange = { input ->
                code = input.filter { it.isDigit() }.take(6)
            },
            placeholder = { Text("Verification code") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                unfocusedContainerColor = FieldBg,
                focusedContainerColor = FieldBg
            )
        )

        Spacer(modifier = Modifier.height(22.dp))

        if (onResendCode != null) {
            Text(
                text = "Resend code",
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable { onResendCode() },
                color = Color.Black,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        Button(
            onClick = { onSubmitCode(code.trim()) },
            enabled = canSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
        ) {
            Text(
                text = "Submit",
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun LoginScreen(
    onBack: () -> Unit = {},
    email: String,
    password: String,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    infoMessage: String? = null,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: (String, String) -> Unit = { _, _ -> },
    onForgotPassword: () -> Unit = {}
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBg)
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {

        // TOP: Welcome Text + Back Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "Welcome\nrunners!",
                fontSize = 35.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(AccentGreen, CircleShape)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // USERNAME FIELD
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            placeholder = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                unfocusedContainerColor = FieldBg,
                focusedContainerColor = FieldBg
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // PASSWORD FIELD
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            placeholder = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility
                        else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle password visibility"
                    )
                }
            },
            visualTransformation =
                if (passwordVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                unfocusedContainerColor = FieldBg,
                focusedContainerColor = FieldBg
            )
        )

        Spacer(modifier = Modifier.height(40.dp))

        if (infoMessage != null) {
            Text(
                text = infoMessage,
                color = Color(0xFF2E7D32),
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = Color(0xFFD32F2F),
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Text(
            text = "Forgot password?",
            modifier = Modifier
                .align(Alignment.End)
                .clickable { onForgotPassword() },
            color = Color.Black,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(14.dp))

        // LOGIN BUTTON
        Button(
            onClick = { onLogin(email, password) },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentGreen
            )
        ) {
            Text(
                text = if (isLoading) "Logging in..." else "Log In",
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    SLRRY_10Theme {
        LoginScreen(
            email = "test@example.com",
            password = "password",
            onEmailChange = {},
            onPasswordChange = {}
        )
    }
}
