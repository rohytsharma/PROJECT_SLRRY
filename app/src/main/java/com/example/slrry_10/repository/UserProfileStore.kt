package com.example.slrry_10.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object UserProfileStore {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseDatabase = FirebaseDatabase.getInstance()

    suspend fun updateCurrentUserFields(fields: Map<String, Any?>) {
        val uid = auth.currentUser?.uid ?: return
        val normalized = fields.toMutableMap()
        val displayName = (fields["displayName"] as? String)
            ?: (fields["name"] as? String)
        if (!displayName.isNullOrBlank()) {
            normalized["displayNameLower"] = displayName.trim().lowercase()
        }
        val email = (fields["email"] as? String)
        if (!email.isNullOrBlank()) {
            normalized["emailLower"] = email.trim().lowercase()
        }
        suspendCancellableCoroutine<Unit> { cont ->
            db.reference.child("users").child(uid).updateChildren(normalized)
                .addOnSuccessListener { cont.resume(Unit) }
                .addOnFailureListener { cont.resume(Unit) }
        }
    }
}

