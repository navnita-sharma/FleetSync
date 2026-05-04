package com.example.fleetsync

import java.util.UUID
import kotlin.random.Random

object SecureTrackingUtils {
    
    /**
     * Generates a unique, non-guessable Tracking ID using UUID.
     */
    fun generateTrackingId(): String {
        return UUID.randomUUID().toString()
    }

    /**
     * Generates a 6-digit numeric passkey.
     */
    fun generatePasskey(): String {
        return (100000..999999).random().toString()
    }

    /**
     * Bundles the tracking ID into a shareable URL.
     */
    fun generateShareableLink(trackingId: String): String {
        return "https://fleetsync.app/track/$trackingId"
    }
}
