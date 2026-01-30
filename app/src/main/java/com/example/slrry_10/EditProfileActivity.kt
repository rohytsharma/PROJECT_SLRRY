package com.example.slrry_10

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.slrry_10.repository.UserProfileStore
import com.example.slrry_10.ui.theme.Mint
import com.example.slrry_10.ui.theme.SLRRY_10Theme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class EditProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SLRRY_10Theme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    EditProfileScreen(onBack = { finish() })
                }
            }
        }
    }
}

private data class ProfileEditState(
    val displayName: String = "",
    val age: Int? = null,
    val bio: String = "",
    val weeklyDistanceKmGoal: Int? = null,
    val weeklyActiveHoursGoal: Int? = null
)

@Composable
private fun EditProfileScreen(
    onBack: () -> Unit
) {
    val background = Color(0xFFF5F1EB)

    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var nameText by remember { mutableStateOf("") }
    var ageText by remember { mutableStateOf("") }
    var bioText by remember { mutableStateOf("") }
    var weeklyKmText by remember { mutableStateOf("") }
    var weeklyHrsText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        isLoading = true
        error = null
        val s = fetchProfileEditState()
        nameText = s.displayName
        ageText = s.age?.toString().orEmpty()
        bioText = s.bio
        weeklyKmText = s.weeklyDistanceKmGoal?.toString().orEmpty()
        weeklyHrsText = s.weeklyActiveHoursGoal?.toString().orEmpty()
        isLoading = false
    }

    fun parseIntOrNull(v: String): Int? = v.trim().toIntOrNull()

    val canSave = nameText.trim().isNotBlank() && !isSaving && !isLoading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF111416))
            }
            Text("Edit profile", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111416))
            Spacer(modifier = Modifier.height(1.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Text("Loading…", color = Color(0xFF6E757A))
            return@Column
        }

        error?.let { msg ->
            Text(msg, color = Color(0xFFB00020), fontSize = 12.sp)
            Spacer(modifier = Modifier.height(10.dp))
        }

        SectionTitle("Profile")
        Spacer(modifier = Modifier.height(8.dp))
        InputField(
            label = "Full name",
            value = nameText,
            onChange = { nameText = it },
            keyboardType = KeyboardType.Text,
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))
        InputField(
            label = "Age",
            value = ageText,
            onChange = { ageText = it.filter { ch -> ch.isDigit() }.take(3) },
            keyboardType = KeyboardType.Number,
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))
        InputField(
            label = "Bio",
            value = bioText,
            onChange = { bioText = it.take(180) },
            keyboardType = KeyboardType.Text,
            singleLine = false,
            minLines = 3
        )

        Spacer(modifier = Modifier.height(18.dp))
        SectionTitle("Goals")
        Spacer(modifier = Modifier.height(8.dp))
        InputField(
            label = "Weekly distance goal (km)",
            value = weeklyKmText,
            onChange = { weeklyKmText = it.filter { ch -> ch.isDigit() }.take(4) },
            keyboardType = KeyboardType.Number,
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))
        InputField(
            label = "Weekly active time goal (hrs)",
            value = weeklyHrsText,
            onChange = { weeklyHrsText = it.filter { ch -> ch.isDigit() }.take(3) },
            keyboardType = KeyboardType.Number,
            singleLine = true
        )

        Spacer(modifier = Modifier.height(22.dp))

        Button(
            onClick = {
                isSaving = true
                error = null
                val name = nameText.trim()
                val age = parseIntOrNull(ageText)?.coerceIn(1, 120)
                val bio = bioText.trim()
                val kmGoal = parseIntOrNull(weeklyKmText)?.coerceIn(1, 9999)
                val hrsGoal = parseIntOrNull(weeklyHrsText)?.coerceIn(1, 999)
            },
            enabled = canSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Mint, contentColor = Color.White)
        ) {
            Text(if (isSaving) "Saving…" else "Save", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(18.dp))
    }

    // Save handler (kept outside button onClick so we can use suspend cleanly)
    LaunchedEffect(isSaving) {
        if (!isSaving) return@LaunchedEffect
        val name = nameText.trim()
        val age = parseIntOrNull(ageText)?.coerceIn(1, 120)
        val bio = bioText.trim()
        val kmGoal = parseIntOrNull(weeklyKmText)?.coerceIn(1, 9999)
        val hrsGoal = parseIntOrNull(weeklyHrsText)?.coerceIn(1, 999)

        val ok = saveProfileEdits(
            displayName = name,
            age = age,
            bio = bio,
            weeklyDistanceKmGoal = kmGoal,
            weeklyActiveHoursGoal = hrsGoal
        )
        isSaving = false
        if (ok) onBack() else error = "Couldn’t save right now. Please check your connection and try again."
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color(0xFF111416))
}

@Composable
private fun InputField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    keyboardType: KeyboardType,
    singleLine: Boolean,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = singleLine,
        minLines = minLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF3D3D3D),
            unfocusedBorderColor = Color(0xFF3D3D3D),
            cursorColor = Color(0xFF111416),
            focusedLabelColor = Color(0xFF111416),
            unfocusedLabelColor = Color(0xFF6E757A)
        ),
        textStyle = MaterialTheme.typography.bodyMedium
    )
}

private suspend fun fetchProfileEditState(): ProfileEditState {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return ProfileEditState()
    val ref = FirebaseDatabase.getInstance().reference.child("users").child(uid)

    val userSnap = suspendCancellableCoroutine<com.google.firebase.database.DataSnapshot?> { cont ->
        ref.get()
            .addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener { cont.resume(null) }
    }

    val displayName = userSnap?.child("displayName")?.getValue(String::class.java)
        ?: userSnap?.child("name")?.getValue(String::class.java)
        ?: FirebaseAuth.getInstance().currentUser?.displayName
        ?: "Runner"
    val age = userSnap?.child("age")?.getValue(Int::class.java)
    val bio = userSnap?.child("bio")?.getValue(String::class.java) ?: ""
    val km = userSnap?.child("goals")?.child("weeklyDistanceKm")?.getValue(Int::class.java)
    val hrs = userSnap?.child("goals")?.child("weeklyActiveHours")?.getValue(Int::class.java)

    return ProfileEditState(
        displayName = displayName,
        age = age,
        bio = bio,
        weeklyDistanceKmGoal = km,
        weeklyActiveHoursGoal = hrs
    )
}

private suspend fun saveProfileEdits(
    displayName: String,
    age: Int?,
    bio: String,
    weeklyDistanceKmGoal: Int?,
    weeklyActiveHoursGoal: Int?
): Boolean {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return false
    val db = FirebaseDatabase.getInstance()

    // Save core profile fields under /users/{uid}
    UserProfileStore.updateCurrentUserFields(
        mapOf(
            "name" to displayName,
            "displayName" to displayName,
            "age" to age,
            "bio" to bio,
            "updatedAt" to System.currentTimeMillis()
        )
    )

    // Save goals under /users/{uid}/goals
    val goalUpdates = mutableMapOf<String, Any?>(
        "updatedAt" to System.currentTimeMillis()
    )
    if (weeklyDistanceKmGoal != null) goalUpdates["weeklyDistanceKm"] = weeklyDistanceKmGoal
    if (weeklyActiveHoursGoal != null) goalUpdates["weeklyActiveHours"] = weeklyActiveHoursGoal

    return suspendCancellableCoroutine { cont ->
        db.reference.child("users").child(uid).child("goals").updateChildren(goalUpdates)
            .addOnSuccessListener { cont.resume(true) }
            .addOnFailureListener { cont.resume(false) }
    }
}

