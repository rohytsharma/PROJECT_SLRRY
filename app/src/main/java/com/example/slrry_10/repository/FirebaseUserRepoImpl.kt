package com.example.slrry_10.repository

import com.example.slrry_10.model.LocationModel
import com.example.slrry_10.model.RunSession
import com.example.slrry_10.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class FirebaseUserRepoImpl(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseDatabase = FirebaseDatabase.getInstance()
) : UserRepo {

    private fun uidOrNull(): String? = auth.currentUser?.uid

    override suspend fun getUser(): UserModel? {
        val uid = uidOrNull() ?: return null
        val userRef = db.reference.child("users").child(uid)
        return suspendCancellableCoroutine { cont ->
            userRef.get()
                .addOnSuccessListener { snap ->
                    cont.resume(parseUser(uid, snap))
                }
                .addOnFailureListener { e ->
                    // If we can't read the user node, fall back to auth identity.
                    val u = auth.currentUser
                    cont.resume(
                        if (u == null) null else UserModel(id = uid, name = u.displayName ?: "", email = u.email ?: "")
                    )
                }
        }
    }

    override suspend fun saveRunSession(session: RunSession) {
        val uid = uidOrNull() ?: return
        val runsRef = db.reference.child("runs").child(uid).child(session.id.ifBlank { System.currentTimeMillis().toString() })
        val payload = sessionToMap(session)
        suspendCancellableCoroutine<Unit> { cont ->
            runsRef.setValue(payload)
                .addOnSuccessListener { cont.resume(Unit) }
                .addOnFailureListener { cont.resume(Unit) } // ignore failure in UI thread; errors are shown elsewhere
        }
    }

    override suspend fun getRunSessions(): List<RunSession> {
        val uid = uidOrNull() ?: return emptyList()
        val runsRef = db.reference.child("runs").child(uid)
        return suspendCancellableCoroutine { cont ->
            runsRef.get()
                .addOnSuccessListener { snap ->
                    val list = snap.children.mapNotNull { child ->
                        parseRunSession(child.key ?: return@mapNotNull null, child)
                    }.sortedByDescending { it.startTime }
                    cont.resume(list)
                }
                .addOnFailureListener {
                    cont.resume(emptyList())
                }
        }
    }

    private fun parseUser(uid: String, snap: DataSnapshot): UserModel {
        val name = snap.child("displayName").getValue(String::class.java)
            ?: snap.child("name").getValue(String::class.java)
            ?: ""
        val email = snap.child("email").getValue(String::class.java) ?: auth.currentUser?.email.orEmpty()
        return UserModel(id = uid, name = name, email = email)
    }

    private fun sessionToMap(session: RunSession): Map<String, Any?> {
        val pathList = session.path.map { loc ->
            mapOf(
                "lat" to loc.latitude,
                "lon" to loc.longitude,
                "ts" to loc.timestamp
            )
        }
        return mapOf(
            "id" to session.id,
            "startTime" to session.startTime,
            "endTime" to session.endTime,
            "distance" to session.distance,
            "duration" to session.duration,
            "averagePace" to session.averagePace,
            "isActive" to session.isActive,
            "path" to pathList
        )
    }

    private fun parseRunSession(id: String, snap: DataSnapshot): RunSession? {
        val startTime = snap.child("startTime").getValue(Long::class.java) ?: 0L
        val endTime = snap.child("endTime").getValue(Long::class.java)
        val distance = snap.child("distance").getValue(Double::class.java) ?: 0.0
        val duration = snap.child("duration").getValue(Long::class.java) ?: 0L
        val averagePace = snap.child("averagePace").getValue(String::class.java) ?: "0'00''"

        val path = snap.child("path").children.mapNotNull { c ->
            val lat = c.child("lat").getValue(Double::class.java) ?: return@mapNotNull null
            val lon = c.child("lon").getValue(Double::class.java) ?: return@mapNotNull null
            val ts = c.child("ts").getValue(Long::class.java) ?: System.currentTimeMillis()
            LocationModel(latitude = lat, longitude = lon, timestamp = ts)
        }

        return RunSession(
            id = id,
            startTime = startTime,
            endTime = endTime,
            path = path,
            distance = distance,
            duration = duration,
            averagePace = averagePace,
            isActive = false
        )
    }
}

