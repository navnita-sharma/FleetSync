package com.example.fleetsync

data class CompanyModel(
    val companyId: String = "",       // Same as ownerUid for Fleet Owners
    val companyName: String = "",
    val ownerUid: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
