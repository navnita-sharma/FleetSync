package com.example.fleetsync

data class UserModel(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "",
    // Company-based linking (new architecture)
    val companyId: String = "",        // For Fleet Owners: same as their uid. For Drivers: their fleet's companyId.
    // Legacy field kept for backward compatibility
    val fleetOwnerUid: String = "",
    val phoneVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)