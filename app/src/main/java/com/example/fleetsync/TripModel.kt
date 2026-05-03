package com.example.fleetsync

data class TripModel(
    val tripId             : String = "",
    val origin             : String = "",
    val destination        : String = "",
    val assignedDriverUid  : String = "",
    val assignedDriverName : String = "",
    val vehicleNumber      : String = "",
    val companyId          : String = "",
    val status             : String = "Pending",   // "Pending" | "In Transit" | "Delivered"
    val timestamp          : Long   = System.currentTimeMillis(),

    // ─── Route / Toll data ────────────────────────────────────────────────────
    val routeTolls         : List<String> = emptyList(),   // toll plaza names along the route
    val currentTollIndex   : Int    = -1,  // -1 = at source; 0..N = at tollN; N+1 = destination
    val totalTolls         : Int    = 0,
    val passedTolls        : Int    = 0,
    val distanceKm         : Double = 0.0,

    // ─── ETA ──────────────────────────────────────────────────────────────────
    val etaMinutes         : Int    = 0,
    val eta                : String = "",  // formatted, e.g. "2h 35m"

    // ─── Secure tracking link ─────────────────────────────────────────────────
    val trackingPasskey    : String = "",
    val trackingLink       : String = "",
    val expiresAt          : Long   = 0L   // Unix ms; 0 = never set
)

data class TollEvent(
    val eventId     : String = "",
    val vehicleId   : String = "",   // Firestore vehicle document ID
    val tripId      : String = "",
    val name        : String = "",   // toll plaza name
    val time        : Long   = 0L,
    val status      : String = "Scheduled",  // "Scheduled" | "Passed"
    val estimatedFee: Double = 0.0
)
