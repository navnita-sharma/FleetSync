package com.example.fleetsync

data class NotificationModel(
    val notifId    : String  = "",
    /** uid of the user who should receive this notification */
    val targetUid  : String  = "",
    /** "Driver" or "Fleet Owner" — for role-based filtering */
    val targetRole : String  = "",
    val title      : String  = "",
    val body       : String  = "",
    /** "INFO" | "SUCCESS" | "WARNING" | "ALERT" */
    val type       : String  = "INFO",
    /** Related trip, if any */
    val tripId     : String  = "",
    val isRead     : Boolean = false,
    val timestamp  : Long    = 0L
)
