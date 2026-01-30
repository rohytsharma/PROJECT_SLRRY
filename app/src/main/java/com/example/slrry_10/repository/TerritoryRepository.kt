package com.example.slrry_10.repository

import com.example.slrry_10.model.AreaModel
import com.example.slrry_10.model.LocationModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.PI

/**
 * Non-overlapping territory system for "take over" gameplay.
 *
 * We represent the world as a fixed lat/lon grid of cells.
 * Each cell has exactly ONE owner at any time. Capturing a polygon assigns all covered cells to the capturer,
 * thereby removing them from other users (no overlap).
 *
 * RTDB schema:
 * /territory/cells/{cellId} = { ownerUid: string, latIdx: int, lonIdx: int, updatedAt: long }
 */
class TerritoryRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseDatabase = FirebaseDatabase.getInstance()
) {
    private fun uid(): String? = auth.currentUser?.uid

    // ~33m at equator. Small enough to look good; still lightweight for RTDB.
    private val stepDeg: Double = 0.0003

    private fun cellId(latIdx: Int, lonIdx: Int): String = "${latIdx}_${lonIdx}"

    private fun latIdx(lat: Double): Int = floor(lat / stepDeg).toInt()
    private fun lonIdx(lon: Double): Int = floor(lon / stepDeg).toInt()

    suspend fun capturePolygon(polygon: List<LocationModel>) {
        val ownerUid = uid() ?: return
        if (polygon.size < 3) return

        val lats = polygon.map { it.latitude }
        val lons = polygon.map { it.longitude }
        val minLat = lats.minOrNull() ?: return
        val maxLat = lats.maxOrNull() ?: return
        val minLon = lons.minOrNull() ?: return
        val maxLon = lons.maxOrNull() ?: return

        val minLatIdx = latIdx(minLat)
        val maxLatIdx = latIdx(maxLat)
        val minLonIdx = lonIdx(minLon)
        val maxLonIdx = lonIdx(maxLon)

        val updates = mutableMapOf<String, Any?>()
        val now = System.currentTimeMillis()

        // Iterate cells in bounding box, keep only those whose center is inside the polygon.
        for (la in minLatIdx..maxLatIdx) {
            val centerLat = (la + 0.5) * stepDeg
            for (lo in minLonIdx..maxLonIdx) {
                val centerLon = (lo + 0.5) * stepDeg
                if (!pointInPolygon(centerLat, centerLon, polygon)) continue

                val id = cellId(la, lo)
                updates["/territory/cells/$id/ownerUid"] = ownerUid
                updates["/territory/cells/$id/latIdx"] = la
                updates["/territory/cells/$id/lonIdx"] = lo
                updates["/territory/cells/$id/updatedAt"] = now
            }
        }

        if (updates.isEmpty()) return
        suspendCancellableCoroutine<Unit> { cont ->
            db.reference.updateChildren(updates)
                .addOnSuccessListener { cont.resume(Unit) }
                .addOnFailureListener { cont.resume(Unit) }
        }
    }

    suspend fun listAllCells(): List<TerritoryCell> {
        val ref = db.reference.child("territory").child("cells")
        val snap = suspendCancellableCoroutine<DataSnapshot?> { cont ->
            ref.get()
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resume(null) }
        } ?: return emptyList()

        return snap.children.mapNotNull { c ->
            val ownerUid = c.child("ownerUid").getValue(String::class.java) ?: return@mapNotNull null
            val latIdx = c.child("latIdx").getValue(Int::class.java) ?: return@mapNotNull null
            val lonIdx = c.child("lonIdx").getValue(Int::class.java) ?: return@mapNotNull null
            TerritoryCell(ownerUid = ownerUid, latIdx = latIdx, lonIdx = lonIdx, stepDeg = stepDeg)
        }
    }

    suspend fun listAreasByOwner(): Map<String, List<AreaModel>> {
        val cells = listAllCells()
        if (cells.isEmpty()) return emptyMap()
        return cells.groupBy { it.ownerUid }
            .mapValues { (_, list) -> list.map { it.toAreaModel() } }
    }

    suspend fun getAreasForUser(userUid: String): List<AreaModel> {
        if (userUid.isBlank()) return emptyList()
        return listAllCells()
            .asSequence()
            .filter { it.ownerUid == userUid }
            .map { it.toAreaModel() }
            .toList()
    }

    suspend fun getTotalAreaForUser(userUid: String): Double {
        if (userUid.isBlank()) return 0.0
        return getAreasForUser(userUid).sumOf { it.area }
    }

    data class TerritoryCell(
        val ownerUid: String,
        val latIdx: Int,
        val lonIdx: Int,
        val stepDeg: Double
    ) {
        fun toAreaModel(): AreaModel {
            val lat0 = latIdx * stepDeg
            val lon0 = lonIdx * stepDeg
            val lat1 = (latIdx + 1) * stepDeg
            val lon1 = (lonIdx + 1) * stepDeg

            val poly = listOf(
                LocationModel(latitude = lat0, longitude = lon0),
                LocationModel(latitude = lat0, longitude = lon1),
                LocationModel(latitude = lat1, longitude = lon1),
                LocationModel(latitude = lat1, longitude = lon0)
            )

            val centerLat = (lat0 + lat1) * 0.5
            val metersPerDegLat = 111_320.0
            val metersPerDegLon = 111_320.0 * cos(centerLat * PI / 180.0).coerceAtLeast(0.2)
            val cellArea = abs(stepDeg * metersPerDegLat) * abs(stepDeg * metersPerDegLon)
            return AreaModel(polygon = poly, area = cellArea)
        }
    }

    // Ray casting point-in-polygon (lat,lon).
    private fun pointInPolygon(lat: Double, lon: Double, poly: List<LocationModel>): Boolean {
        var inside = false
        var j = poly.size - 1
        for (i in poly.indices) {
            val yi = poly[i].latitude
            val xi = poly[i].longitude
            val yj = poly[j].latitude
            val xj = poly[j].longitude
            val intersect = ((yi > lat) != (yj > lat)) &&
                (lon < (xj - xi) * (lat - yi) / ((yj - yi).takeIf { it != 0.0 } ?: 1e-12) + xi)
            if (intersect) inside = !inside
            j = i
        }
        return inside
    }
}

