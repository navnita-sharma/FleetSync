package com.example.fleetsync

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class DriverDashboardViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    // ─── Driver identity ──────────────────────────────────────────────────────
    var driverName   by mutableStateOf("Loading…")
        private set
    var companyName  by mutableStateOf("")
        private set
    var driverStatus by mutableStateOf("Available")
        private set

    // ─── Owner uid (needed to write notifications to Fleet Owner) ─────────────
    private var ownerUid: String = ""

    // ─── Live trips ───────────────────────────────────────────────────────────
    var assignedTrips  by mutableStateOf<List<TripModel>>(emptyList())
        private set
    var isLoadingTrips by mutableStateOf(true)
        private set

    private var tripsListener: ListenerRegistration? = null

    // ─────────────────────────────────────────────────────────────────────────
    init { loadDriverProfile() }

    private fun loadDriverProfile() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                driverName = doc.getString("name") ?: "Driver"
                val compId = doc.getString("companyId") ?: ""
                Log.d("DriverVM", "Driver: $driverName  companyId=$compId")

                // Fetch company name + ownerUid for notifications
                if (compId.isNotBlank()) {
                    db.collection("companies").document(compId).get()
                        .addOnSuccessListener { compDoc ->
                            companyName = compDoc.getString("companyName") ?: ""
                            ownerUid    = compDoc.getString("ownerUid")    ?: ""
                            Log.d("DriverVM", "Company: $companyName  ownerUid=$ownerUid")
                        }
                }

                startTripsListener(uid)
            }
            .addOnFailureListener { e ->
                Log.e("DriverVM", "Profile load failed: ${e.message}")
                startTripsListener(uid)
            }
    }

    // ─── Real-time SnapshotListener ───────────────────────────────────────────
    private fun startTripsListener(driverUid: String) {
        Log.d("DriverVM", "Starting trips listener for uid=$driverUid")

        tripsListener = db.collection("trips")
            .whereEqualTo("assignedDriverUid", driverUid)
            .whereIn("status", listOf("Pending", "In Transit"))
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    Log.e("DriverVM", "Trips listener error: ${err.message}")
                    isLoadingTrips = false
                    return@addSnapshotListener
                }

                val trips = mutableListOf<TripModel>()
                snap?.documents?.forEach { doc ->
                    try {
                        trips.add(
                            TripModel(
                                tripId             = doc.getString("tripId")             ?: doc.id,
                                origin             = doc.getString("origin")             ?: "",
                                destination        = doc.getString("destination")        ?: "",
                                assignedDriverUid  = doc.getString("assignedDriverUid")  ?: "",
                                assignedDriverName = doc.getString("assignedDriverName") ?: "",
                                vehicleNumber      = doc.getString("vehicleNumber")      ?: "",
                                companyId          = doc.getString("companyId")          ?: "",
                                status             = doc.getString("status")             ?: "Pending",
                                timestamp          = doc.getLong("timestamp")            ?: 0L
                            )
                        )
                    } catch (e: Exception) {
                        Log.w("DriverVM", "Parse error ${doc.id}: ${e.message}")
                    }
                }

                assignedTrips  = trips.sortedByDescending { it.timestamp }
                isLoadingTrips = false
                driverStatus   = if (trips.any { it.status == "In Transit" }) "On Trip" else "Available"
                Log.d("DriverVM", "Trips: ${trips.size} active")
            }
    }

    // ─── Trip status updates ──────────────────────────────────────────────────
    fun markInTransit(tripId: String) {
        val trip = assignedTrips.find { it.tripId == tripId } ?: return
        
        // Generate Secure Tracking Details
        val passkey = (100000..999999).random().toString()
        val trackingLink = "https://fleetsync.app/track/$tripId"
        
        val updates = mapOf(
            "status" to "In Transit",
            "trackingPasskey" to passkey,
            "trackingLink" to trackingLink
        )

        db.collection("trips").document(tripId)
            .update(updates)
            .addOnSuccessListener {
                Log.d("DriverVM", "Trip $tripId → In Transit")
                // Notify the Fleet Owner
                sendOwnerNotification(
                    title  = "Trip Accepted ✅",
                    body   = "$driverName accepted the trip: ${trip.origin} → ${trip.destination}",
                    type   = "INFO",
                    tripId = tripId
                )
            }
            .addOnFailureListener { e -> Log.e("DriverVM", "markInTransit failed: ${e.message}") }
    }

    fun markDelivered(tripId: String) {
        val trip = assignedTrips.find { it.tripId == tripId } ?: return
        db.collection("trips").document(tripId)
            .update("status", "Delivered")
            .addOnSuccessListener {
                Log.d("DriverVM", "Trip $tripId → Delivered")
                // Notify the Fleet Owner
                sendOwnerNotification(
                    title  = "Order Delivered 🎉",
                    body   = "$driverName delivered the order to ${trip.destination}.",
                    type   = "SUCCESS",
                    tripId = tripId
                )
            }
            .addOnFailureListener { e -> Log.e("DriverVM", "markDelivered failed: ${e.message}") }
    }

    // ─── Internal notification writer ─────────────────────────────────────────
    private fun sendOwnerNotification(title: String, body: String, type: String, tripId: String) {
        val trip = assignedTrips.find { it.tripId == tripId }
        
        // Use trip.companyId as the targetUid for fleet owner notifications (since companyId == ownerUid)
        // Fallback to the loaded ownerUid if companyId is somehow missing
        val targetUid = trip?.companyId ?: ownerUid

        if (targetUid.isBlank()) {
            Log.w("DriverVM", "Cannot send notification: targetUid/ownerUid is blank")
            return
        }

        val docRef = db.collection("notifications").document()
        val notif = hashMapOf(
            "notifId"    to docRef.id,
            "targetUid"  to targetUid,
            "targetRole" to "Fleet Owner",
            "title"      to title,
            "body"       to body,
            "type"       to type,
            "tripId"     to tripId,
            "isRead"     to false,
            "timestamp"  to System.currentTimeMillis()
        )
        docRef.set(notif)
            .addOnSuccessListener { Log.d("DriverVM", "Notification sent to owner ($targetUid): $title") }
            .addOnFailureListener { e -> Log.e("DriverVM", "Notif write failed: ${e.message}") }
    }

    // ─────────────────────────────────────────────────────────────────────────
    override fun onCleared() {
        super.onCleared()
        tripsListener?.remove()
        Log.d("DriverVM", "SnapshotListener removed")
    }
}
