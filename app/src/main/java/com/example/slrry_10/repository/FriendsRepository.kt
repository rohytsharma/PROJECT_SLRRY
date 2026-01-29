package com.example.slrry_10.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class UserSummary(
    val uid: String,
    val displayName: String,
    val email: String = "",
    val photoUrl: String = ""
)

class FriendsRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseDatabase = FirebaseDatabase.getInstance()
) {
    private fun currentUid(): String? = auth.currentUser?.uid

    /**
     * Send a friend request to [friendUid]. The receiver will see it under /friendRequests/{theirUid}/{myUid}.
     */
    suspend fun sendFriendRequest(friendUid: String) {
        val uid = currentUid() ?: return
        if (friendUid.isBlank() || friendUid == uid) return
        val updates = mapOf<String, Any?>(
            "/friendRequests/$friendUid/$uid" to mapOf(
                "fromUid" to uid,
                "toUid" to friendUid,
                "createdAt" to System.currentTimeMillis()
            )
        )
        suspendCancellableCoroutine<Unit> { cont ->
            db.reference.updateChildren(updates)
                .addOnSuccessListener { cont.resume(Unit) }
                .addOnFailureListener { cont.resume(Unit) }
        }
    }

    suspend fun acceptFriendRequest(fromUid: String) {
        val uid = currentUid() ?: return
        if (fromUid.isBlank() || fromUid == uid) return
        val updates = mapOf<String, Any?>(
            "/friends/$uid/$fromUid" to true,
            "/friends/$fromUid/$uid" to true,
            "/friendRequests/$uid/$fromUid" to null
        )
        suspendCancellableCoroutine<Unit> { cont ->
            db.reference.updateChildren(updates)
                .addOnSuccessListener { cont.resume(Unit) }
                .addOnFailureListener { cont.resume(Unit) }
        }
    }

    suspend fun listFriends(): List<UserSummary> {
        val uid = currentUid() ?: return emptyList()
        val ref = db.reference.child("friends").child(uid)
        val friendUids = suspendCancellableCoroutine<List<String>> { cont ->
            ref.get()
                .addOnSuccessListener { snap ->
                    cont.resume(snap.children.mapNotNull { it.key })
                }
                .addOnFailureListener { cont.resume(emptyList()) }
        }
        if (friendUids.isEmpty()) return emptyList()

        val usersSnap = usersSnapshot() ?: return emptyList()

        val set = friendUids.toSet()
        return usersSnap.children.mapNotNull { u ->
            val id = u.key ?: return@mapNotNull null
            if (id !in set) return@mapNotNull null
            snapshotToUserSummary(id, u)
        }.sortedBy { it.displayName.lowercase() }
    }

    /**
     * Fetch a small page of users for the "Add friend" dialog.
     * (RTDB doesn't support full text search; this is a pragmatic "show all" for small datasets.)
     */
    suspend fun listAllUsers(limit: Int = 50): List<UserSummary> {
        val uid = currentUid()
        // IMPORTANT: don't rely on displayNameLower being present; just read the first page.
        val snap = suspendCancellableCoroutine<DataSnapshot?> { cont ->
            db.reference.child("users")
                .limitToFirst(limit)
                .get()
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resume(null) }
        } ?: return emptyList()

        return snap.children.mapNotNull { u ->
            val id = u.key ?: return@mapNotNull null
            if (uid != null && id == uid) return@mapNotNull null
            snapshotToUserSummary(id, u)
        }.sortedBy { it.displayName.lowercase() }
    }

    suspend fun searchUsersByNamePrefix(query: String, limit: Int = 20): List<UserSummary> {
        val uid = currentUid()
        val q = query.trim().lowercase()
        if (q.isBlank()) return emptyList()

        val ref = db.reference.child("users")
        // First try name prefix; if nothing, fall back to email prefix (so searching "rohit" finds rohit@gmail.com).
        val byName = suspendCancellableCoroutine<List<UserSummary>> { cont ->
            ref.orderByChild("displayNameLower")
                .startAt(q)
                .endAt(q + "\uf8ff")
                .limitToFirst(limit)
                .get()
                .addOnSuccessListener { snap ->
                    val out = snap.children.mapNotNull { u ->
                        val id = u.key ?: return@mapNotNull null
                        if (uid != null && id == uid) return@mapNotNull null
                        snapshotToUserSummary(id, u)
                    }
                    cont.resume(out)
                }
                .addOnFailureListener { cont.resume(emptyList()) }
        }
        if (byName.isNotEmpty()) return byName

        val byEmail = suspendCancellableCoroutine<List<UserSummary>> { cont ->
            ref.orderByChild("emailLower")
                .startAt(q)
                .endAt(q + "\uf8ff")
                .limitToFirst(limit)
                .get()
                .addOnSuccessListener { snap ->
                    val out = snap.children.mapNotNull { u ->
                        val id = u.key ?: return@mapNotNull null
                        if (uid != null && id == uid) return@mapNotNull null
                        snapshotToUserSummary(id, u)
                    }
                    cont.resume(out)
                }
                .addOnFailureListener { cont.resume(emptyList()) }
        }
        return byEmail
    }

    private fun snapshotToUserSummary(uid: String, snap: DataSnapshot): UserSummary {
        val name = snap.child("displayName").getValue(String::class.java)
            ?: snap.child("name").getValue(String::class.java)
            ?: "Runner"
        val email = snap.child("email").getValue(String::class.java) ?: ""
        val photoUrl = snap.child("photoUrl").getValue(String::class.java) ?: ""
        return UserSummary(uid = uid, displayName = name, email = email, photoUrl = photoUrl)
    }

    suspend fun listIncomingFriendRequests(): List<UserSummary> {
        val uid = currentUid() ?: return emptyList()
        val reqRef = db.reference.child("friendRequests").child(uid)
        val fromUids = suspendCancellableCoroutine<List<String>> { cont ->
            reqRef.get()
                .addOnSuccessListener { snap -> cont.resume(snap.children.mapNotNull { it.key }) }
                .addOnFailureListener { cont.resume(emptyList()) }
        }
        if (fromUids.isEmpty()) return emptyList()
        val usersSnap = usersSnapshot() ?: return emptyList()
        val set = fromUids.toSet()
        return usersSnap.children.mapNotNull { u ->
            val id = u.key ?: return@mapNotNull null
            if (id !in set) return@mapNotNull null
            snapshotToUserSummary(id, u)
        }.sortedBy { it.displayName.lowercase() }
    }

    private suspend fun usersSnapshot(): DataSnapshot? {
        return suspendCancellableCoroutine { cont ->
            db.reference.child("users").get()
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resume(null) }
        }
    }
}

