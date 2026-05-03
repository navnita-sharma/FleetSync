package com.example.fleetsync

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrdersViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _orders = mutableStateOf<List<Order>>(emptyList())
    val orders: State<List<Order>> = _orders

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    // Vehicles owned by this fleet owner
    private val _vehicles = mutableStateOf<List<VehicleModel>>(emptyList())
    val vehicles: State<List<VehicleModel>> = _vehicles

    // Drivers associated with this fleet
    private val _drivers = mutableStateOf<List<UserModel>>(emptyList())
    val drivers: State<List<UserModel>> = _drivers

    private val _isSaving = mutableStateOf(false)
    val isSaving: State<Boolean> = _isSaving

    init {
        fetchOrders()
        fetchVehicles()
        fetchDrivers()
    }

    private fun fetchOrders() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("orders")
            .whereEqualTo("ownerUid", uid)
            .addSnapshotListener { snapshot, _ ->
                _orders.value = snapshot?.toObjects(Order::class.java) ?: emptyList()
                _isLoading.value = false
            }
    }

    private fun fetchVehicles() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("vehicles")
            .whereEqualTo("ownerUid", uid)
            .addSnapshotListener { snapshot, _ ->
                _vehicles.value = snapshot?.toObjects(VehicleModel::class.java) ?: emptyList()
            }
    }

    private fun fetchDrivers() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users")
            .whereEqualTo("role", "Driver")
            .whereEqualTo("fleetOwnerUid", uid)
            .addSnapshotListener { snapshot, _ ->
                _drivers.value = snapshot?.toObjects(UserModel::class.java) ?: emptyList()
            }
    }

    fun createOrder(
        vehicleNumber: String,
        driverName: String,
        source: String,
        destination: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: run {
            onError("User not logged in")
            return
        }

        if (vehicleNumber.isBlank() || driverName.isBlank() || source.isBlank() || destination.isBlank()) {
            onError("Please fill all fields")
            return
        }

        _isSaving.value = true

        val dateStr = SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date())
        val docRef = db.collection("orders").document()
        // Simulate dynamic calculation for Time and Tolls
        val distanceFactor = (source.length + destination.length) % 10
        val simEstimatedTime = "${2 + distanceFactor}h ${15 + (distanceFactor * 5)}m"
        val simTollCount = (distanceFactor / 2) + 1

        val newOrder = hashMapOf(
            "id" to "#ORD-${docRef.id.take(4).uppercase()}",
            "vehicle" to vehicleNumber,
            "driver" to driverName,
            "from" to source,
            "to" to destination,
            "status" to "PENDING",
            "eta" to simEstimatedTime,
            "estimatedTime" to simEstimatedTime,
            "tollCount" to simTollCount,
            "date" to dateStr,
            "ownerUid" to uid,
            "createdAt" to System.currentTimeMillis()
        )

        docRef.set(newOrder)
            .addOnSuccessListener {
                _isSaving.value = false
                onSuccess()
            }
            .addOnFailureListener { e ->
                _isSaving.value = false
                onError(e.message ?: "Failed to create order")
            }
    }
}
