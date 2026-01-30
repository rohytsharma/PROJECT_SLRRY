package com.example.slrry_10.repository

import com.example.slrry_10.model.AreaModel
import com.example.slrry_10.model.LocationModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos

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
                        val computed = if (areaVal > 0.0) areaVal else approxAreaSqMeters(poly)
                        AreaModel(polygon = poly, area = computed)
                    }
                    cont.resume(areas)
                }
                .addOnFailureListener { cont.resume(emptyList()) }
        }
    }

    /**
     * Approx polygon area in m² using an equirectangular projection (good enough for small shapes).
     * Returns 0 when polygon is invalid.
     */
    private fun approxAreaSqMeters(poly: List<LocationModel>): Double {
        if (poly.size < 3) return 0.0
        val lat0 = poly.map { it.latitude }.average()
        val cosLat = cos(lat0 * PI / 180.0).coerceAtLeast(0.2)
        val metersPerDegLat = 111_320.0
        val metersPerDegLon = 111_320.0 * cosLat

        // Shoelace formula on projected coordinates.
        var sum = 0.0
        for (i in poly.indices) {
            val j = (i + 1) % poly.size
            val xi = poly[i].longitude * metersPerDegLon
            val yi = poly[i].latitude * metersPerDegLat
            val xj = poly[j].longitude * metersPerDegLon
            val yj = poly[j].latitude * metersPerDegLat
            sum += (xi * yj) - (xj * yi)
        }
        return abs(sum) * 0.5
    }
}

