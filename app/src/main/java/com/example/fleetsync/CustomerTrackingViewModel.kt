package com.example.fleetsync

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class CustomerTrackingViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private var tripListener: ListenerRegistration? = null

    var currentTrip by mutableStateOf<TripModel?>(null)
        private set

    var isTripInvalid by mutableStateOf(false)
        private set

    var verificationError by mutableStateOf<String?>(null)
        private set

    var isVerifying by mutableStateOf(false)
        private set

    fun verifyShipment(shipmentId: String, passkey: String, onVerified: (String) -> Unit) {
        val cleanId = shipmentId.trim().lowercase()
        val cleanPass = passkey.trim()

        if (cleanId.isBlank() || cleanPass.isBlank()) {
            verificationError = "Please enter both ID and Passkey"
            return
        }

        isVerifying = true
        verificationError = null
        
        db.collection("orders")
            .whereEqualTo("shipmentId", cleanId)
            .get()
            .addOnSuccessListener { snap ->
                isVerifying = false
                val doc = snap.documents.firstOrNull()
                if (doc != null) {
                    val storedPasskey = doc.getString("trackingPasskey")
                    if (storedPasskey == cleanPass) {
                        val status = doc.getString("status") ?: "PENDING"
                        if (status.uppercase() == "DELIVERED") {
                            verificationError = "Shipment already delivered. Access revoked."
                        } else {
                            onVerified(cleanId)
                            observeTrip(cleanId)
                        }
                    } else {
                        verificationError = "Invalid Shipment ID or Passkey"
                    }
                } else {
                    verificationError = "Invalid Shipment ID or Passkey"
                }
            }
            .addOnFailureListener { e ->
                isVerifying = false
                verificationError = if (e.message?.contains("network", ignoreCase = true) == true) {
                    "Network Error. Please check your connection."
                } else {
                    "Error verifying shipment: ${e.message}"
                }
            }
    }

    fun observeTrip(shipmentId: String) {
        tripListener?.remove()
        // Query by the dynamic shipmentId field instead of document ID
        tripListener = db.collection("trips")
            .whereEqualTo("shipmentId", shipmentId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("CustomerTrackingVM", "Error listening to trip: ${error.message}")
                    return@addSnapshotListener
                }

                val doc = snapshot?.documents?.firstOrNull()
                if (doc != null && doc.exists()) {
                    val status = doc.getString("status") ?: ""
                    
                    if (status == "Delivered") {
                        currentTrip = null
                        isTripInvalid = true
                        tripListener?.remove()
                        
                        doc.reference.update("trackingPasskey", "")
                        
                        db.collection("orders")
                            .whereEqualTo("shipmentId", shipmentId)
                            .get()
                            .addOnSuccessListener { orderSnap ->
                                orderSnap.documents.forEach { it.reference.update("trackingPasskey", "") }
                            }
                    } else {
                        currentTrip = doc.toObject(TripModel::class.java)
                        isTripInvalid = false
                    }
                } else {
                    currentTrip = null
                    isTripInvalid = true
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        tripListener?.remove()
    }
}
