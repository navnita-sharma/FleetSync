package com.example.fleetsync

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class NotificationsViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    // ─── Live state ───────────────────────────────────────────────────────────
    var notifications by mutableStateOf<List<NotificationModel>>(emptyList())
        private set
    var isLoading by mutableStateOf(true)
        private set
    var unreadCount by mutableStateOf(0)
        private set

    private var listener: ListenerRegistration? = null

    // ─────────────────────────────────────────────────────────────────────────
    init {
        startListener()
    }

    private fun startListener() {
        val uid = auth.currentUser?.uid ?: return

        Log.d("NotifVM", "Starting notification listener for uid=$uid")

        listener = db.collection("notifications")
            .whereEqualTo("targetUid", uid)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    Log.e("NotifVM", "Listener error: ${err.message}")
                    isLoading = false
                    return@addSnapshotListener
                }

                val list = mutableListOf<NotificationModel>()
                snap?.documents?.forEach { doc ->
                    try {
                        list.add(
                            NotificationModel(
                                notifId    = doc.id,
                                targetUid  = doc.getString("targetUid")  ?: "",
                                targetRole = doc.getString("targetRole") ?: "",
                                title      = doc.getString("title")      ?: "",
                                body       = doc.getString("body")       ?: "",
                                type       = doc.getString("type")       ?: "INFO",
                                tripId     = doc.getString("tripId")     ?: "",
                                isRead     = doc.getBoolean("isRead")    ?: false,
                                timestamp  = doc.getLong("timestamp")    ?: 0L
                            )
                        )
                    } catch (e: Exception) {
                        Log.w("NotifVM", "Failed to parse notif ${doc.id}: ${e.message}")
                    }
                }

                // Sort in memory by timestamp descending
                notifications = list.sortedByDescending { it.timestamp }
                unreadCount   = notifications.count { !it.isRead }
                isLoading     = false
                Log.d("NotifVM", "Notifications updated: ${list.size} total")
            }
    }

    // ─── Actions ──────────────────────────────────────────────────────────────
    fun markRead(notifId: String) {
        db.collection("notifications").document(notifId)
            .update("isRead", true)
            .addOnFailureListener { e -> Log.e("NotifVM", "markRead failed: ${e.message}") }
    }

    fun markAllRead() {
        val uid = auth.currentUser?.uid ?: return
        // Batch update all unread notifications for this user
        db.collection("notifications")
            .whereEqualTo("targetUid", uid)
            .whereEqualTo("isRead", false)
            .get()
            .addOnSuccessListener { snap ->
                val batch = db.batch()
                snap.documents.forEach { doc ->
                    batch.update(doc.reference, "isRead", true)
                }
                batch.commit()
                    .addOnSuccessListener { Log.d("NotifVM", "All notifications marked read") }
                    .addOnFailureListener { e -> Log.e("NotifVM", "markAllRead batch failed: ${e.message}") }
            }
    }

    // ─────────────────────────────────────────────────────────────────────────
    override fun onCleared() {
        super.onCleared()
        listener?.remove()
        Log.d("NotifVM", "Listener removed")
    }
}
