package com.example.slrry_10.repository

import com.example.slrry_10.model.AreaModel
import com.example.slrry_10.model.LocationModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class CapturedAreasRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseDatabase = FirebaseDatabase.getInstance()
) {
    private fun uid(): String? = auth.currentUser?.uid

    suspend fun addArea(area: AreaModel) {
        val uid = uid() ?: return
        val id = System.currentTimeMillis().toString()
        val ref = db.reference.child("users").child(uid).child("capturedAreas").child(id)
        val payload = mapOf(
            "area" to area.area,
            "polygon" to area.polygon.map { p -> mapOf("lat" to p.latitude, "lon" to p.longitude, "ts" to p.timestamp) }
        )
        suspendCancellableCoroutine<Unit> { cont ->
            ref.setValue(payload)
                .addOnSuccessListener { cont.resume(Unit) }
                .addOnFailureListener { cont.resume(Unit) }
        }
    }

    suspend fun getAreasForUser(userUid: String): List<AreaModel> {
        if (userUid.isBlank()) return emptyList()
        val ref = db.reference.child("users").child(userUid).child("capturedAreas")
        return suspendCancellableCoroutine { cont ->
            ref.get()
                .addOnSuccessListener { snap ->
                    val areas = snap.children.mapNotNull { child ->
                        val areaVal = child.child("area").getValue(Double::class.java) ?: 0.0
                        val poly = child.child("polygon").children.mapNotNull { p ->
                            val lat = p.child("lat").getValue(Double::class.java) ?: return@mapNotNull null
                            val lon = p.child("lon").getValue(Double::class.java) ?: return@mapNotNull null
                            val ts = p.child("ts").getValue(Long::class.java) ?: System.currentTimeMillis()
                            LocationModel(latitude = lat, longitude = lon, timestamp = ts)
                        }
                        AreaModel(polygon = poly, area = areaVal)
                    }
                    cont.resume(areas)
                }
                .addOnFailureListener { cont.resume(emptyList()) }
        }
    }
}

