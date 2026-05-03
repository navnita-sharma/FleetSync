package com.example.fleetsync

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import kotlin.math.roundToInt

/**
 * Lightweight repository that calls the Google Maps Directions API over HTTP
 * to compute ETA and estimate toll count along the route.
 *
 * No external libraries required — uses OkHttp (already in deps) + org.json (built into Android).
 */
object DirectionsRepository {

    private val client = OkHttpClient()
    private const val BASE_URL = "https://maps.googleapis.com/maps/api/directions/json"

    data class RouteResult(
        val etaMinutes: Int,
        val distanceKm: Double,
        val estimatedTolls: Int,
        val formattedEta: String,        // e.g. "2h 35m"
        val routeWaypoints: List<String> // major waypoints / toll names
    )

    /**
     * Performs a synchronous HTTP call — MUST be called from a background thread / coroutine.
     * Returns null on any failure (network, key invalid, etc.).
     */
    fun calculateRoute(origin: String, destination: String, apiKey: String): RouteResult? {
        return try {
            val url = "$BASE_URL?origin=${encode(origin)}&destination=${encode(destination)}" +
                      "&mode=driving&departure_time=now&key=$apiKey"

            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return null

            parseDirectionsResponse(body)
        } catch (e: Exception) {
            Log.e("DirectionsRepo", "Failed to fetch directions: ${e.message}")
            null
        }
    }

    private fun parseDirectionsResponse(json: String): RouteResult? {
        return try {
            val root   = JSONObject(json)
            val status = root.getString("status")
            if (status != "OK") {
                Log.w("DirectionsRepo", "Directions API status: $status")
                return null
            }

            val route = root.getJSONArray("routes").getJSONObject(0)
            val leg   = route.getJSONArray("legs").getJSONObject(0)

            val durationSec = leg.getJSONObject("duration").getInt("value")
            val distanceM   = leg.getJSONObject("distance").getInt("value")

            val etaMinutes  = (durationSec / 60.0).roundToInt()
            val distanceKm  = distanceM / 1000.0

            // Estimate tolls: Indian national highways average ~1 toll per 60-80 km
            val estimatedTolls = maxOf(0, (distanceKm / 70.0).roundToInt())

            // Extract via waypoint names if available
            val steps = leg.getJSONArray("steps")
            val waypoints = mutableListOf<String>()
            if (steps.length() > 2) {
                // Use intermediate step html_instructions as rough waypoint names
                for (i in 1 until minOf(steps.length() - 1, estimatedTolls + 1)) {
                    val html = steps.getJSONObject(i).optString("html_instructions", "")
                    val clean = html.replace(Regex("<[^>]*>"), "").take(30)
                    if (clean.isNotBlank()) waypoints.add("Toll Plaza ${i}: $clean")
                }
            }
            // If no waypoints extracted, generate generic names
            if (waypoints.isEmpty()) {
                for (i in 1..estimatedTolls) waypoints.add("Toll Plaza $i")
            }

            val hours   = etaMinutes / 60
            val minutes = etaMinutes % 60
            val formatted = when {
                hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
                hours > 0                -> "${hours}h"
                else                     -> "${minutes}m"
            }

            RouteResult(
                etaMinutes     = etaMinutes,
                distanceKm     = distanceKm,
                estimatedTolls = estimatedTolls,
                formattedEta   = formatted,
                routeWaypoints = waypoints
            )
        } catch (e: Exception) {
            Log.e("DirectionsRepo", "Parse error: ${e.message}")
            null
        }
    }

    private fun encode(text: String) =
        java.net.URLEncoder.encode(text.trim(), "UTF-8")
}
