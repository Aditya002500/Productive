package com.example.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng

/**
 * Live GPS distance tracking for a single Running/Cycling session. Accumulates a running
 * total via [Location.distanceTo] (already geodesic) and keeps every sample so the route can
 * be drawn on a map. Total/path live on the instance (not [start]'s stack) so [pause] + [start]
 * again resumes instead of losing progress; only [stop] clears them for good.
 */
class LocationTracker(context: Context) {
    private val client = LocationServices.getFusedLocationProviderClient(context)
    private var lastLocation: Location? = null
    private var callback: LocationCallback? = null
    private var totalDistance = 0f
    private val path = mutableListOf<LatLng>()

    @SuppressLint("MissingPermission") // caller must have already requested ACCESS_FINE_LOCATION
    fun start(onUpdate: (totalDistanceMeters: Float, path: List<LatLng>) -> Unit) {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L).build()
        val newCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                lastLocation?.let { totalDistance += it.distanceTo(location) }
                lastLocation = location
                path.add(LatLng(location.latitude, location.longitude))
                onUpdate(totalDistance, path.toList())
            }
        }
        callback = newCallback
        client.requestLocationUpdates(request, newCallback, null)
    }

    /** Stops GPS updates but keeps the accumulated total/path so [start] can resume. */
    fun pause() {
        callback?.let { client.removeLocationUpdates(it) }
        callback = null
    }

    /** Ends the session for good — clears accumulated total/path. */
    fun stop() {
        pause()
        lastLocation = null
        totalDistance = 0f
        path.clear()
    }

    @SuppressLint("MissingPermission") // caller must have already requested ACCESS_FINE_LOCATION
    fun getLastLocation(onResult: (Location?) -> Unit) {
        client.lastLocation
            .addOnSuccessListener { onResult(it) }
            .addOnFailureListener { onResult(null) }
    }
}
