package com.example.fleetsync

data class VehicleModel(
    val id             : String = "",
    val ownerUid       : String = "",
    val vehicleNumber  : String = "",
    val chassisNumber  : String = "",
    val vehicleType    : String = "",
    val model          : String = "",
    val fuelType       : String = "",
    val capacity       : String = "",
    val insuranceExpiry: String = "",
    val pucExpiry      : String = "",
    val latitude       : Double = 19.0760,
    val longitude      : Double = 72.8777,
    val createdAt      : Long   = System.currentTimeMillis(),

    // ─── Toll summary (aggregated from tollEvents) ────────────────────────────
    val totalTollsPaid : Int    = 0,
    val lastTripId     : String = "",
    val lastTripEta    : String = ""
)
