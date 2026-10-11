package com.example.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.example.data.model.DistrictLocation
import com.example.data.model.PRESET_DISTRICTS
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Model representing active geographic coordinates in AgriTwin.
 */
data class UserLocation(
    val latitude: Double = 31.4187,
    val longitude: Double = 73.0791,
    val displayName: String = "Faisalabad, Pakistan",
    val isGpsFixed: Boolean = false,
    val accuracyMeters: Float? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    val formattedCoordinates: String
        get() = String.format(Locale.US, "%.4f°N, %.4f°E", latitude, longitude)
}

/**
 * AppLocationManager coordinates GPS retrieval via FusedLocationProviderClient
 * and provides state across all NASA POWER telemetry features with fallback to manual input.
 */
object AppLocationManager {

    private val _currentLocation = MutableStateFlow(
        UserLocation(
            latitude = 31.4187,
            longitude = 73.0791,
            displayName = "Faisalabad Agricultural Belt",
            isGpsFixed = false
        )
    )
    val currentLocation: StateFlow<UserLocation> = _currentLocation.asStateFlow()

    private val _isLoadingLocation = MutableStateFlow(false)
    val isLoadingLocation: StateFlow<Boolean> = _isLoadingLocation.asStateFlow()

    private val _locationError = MutableStateFlow<String?>(null)
    val locationError: StateFlow<String?> = _locationError.asStateFlow()

    private val _isPermissionDenied = MutableStateFlow(false)
    val isPermissionDenied: StateFlow<Boolean> = _isPermissionDenied.asStateFlow()

    /**
     * Check whether runtime location permissions are granted.
     */
    fun hasLocationPermission(context: Context): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocation || coarseLocation
    }

    /**
     * Sets manual coordinates (e.g. from user input dialog or district picker).
     */
    fun setManualCoordinates(
        latitude: Double,
        longitude: Double,
        name: String = "Custom Field"
    ): Boolean {
        if (latitude !in -90.0..90.0 || longitude !in -180.0..180.0) {
            _locationError.value = "Coordinates must be within -90..90 lat and -180..180 lon"
            return false
        }

        _currentLocation.value = UserLocation(
            latitude = latitude,
            longitude = longitude,
            displayName = name,
            isGpsFixed = false,
            timestamp = System.currentTimeMillis()
        )
        _locationError.value = null
        _isPermissionDenied.value = false
        return true
    }

    /**
     * Sets coordinates from one of the preset NASA agricultural districts.
     */
    fun setFromPresetDistrict(district: DistrictLocation) {
        setManualCoordinates(
            latitude = district.latitude,
            longitude = district.longitude,
            name = "${district.name}, ${district.country}"
        )
    }

    /**
     * Mark permission as denied to prompt manual input dialog.
     */
    fun onPermissionDenied() {
        _isPermissionDenied.value = true
        _locationError.value = "Location permission denied. Please enter farm coordinates manually."
        _isLoadingLocation.value = false
    }

    fun dismissPermissionNotice() {
        _isPermissionDenied.value = false
        _locationError.value = null
    }

    /**
     * Fetch the current device location via FusedLocationProviderClient.
     * Uses High Accuracy GPS with cancellation token and falls back to lastLocation.
     */
    @SuppressLint("MissingPermission")
    suspend fun fetchDeviceLocation(context: Context): Result<UserLocation> {
        if (!hasLocationPermission(context)) {
            onPermissionDenied()
            return Result.failure(SecurityException("Location permission not granted"))
        }

        _isLoadingLocation.value = true
        _locationError.value = null
        _isPermissionDenied.value = false

        return try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            val cts = CancellationTokenSource()

            // Request fresh high-accuracy location fix
            var location: Location? = try {
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cts.token
                ).await()
            } catch (e: Exception) {
                null
            }

            // Fallback to last known cached location if fresh fix timed out
            if (location == null) {
                location = fusedLocationClient.lastLocation.await()
            }

            if (location != null) {
                val newLoc = UserLocation(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    displayName = String.format(Locale.US, "Device GPS Fix (%.4f, %.4f)", location.latitude, location.longitude),
                    isGpsFixed = true,
                    accuracyMeters = if (location.hasAccuracy()) location.accuracy else null,
                    timestamp = System.currentTimeMillis()
                )
                _currentLocation.value = newLoc
                _locationError.value = null
                _isPermissionDenied.value = false
                _isLoadingLocation.value = false
                Result.success(newLoc)
            } else {
                val errMsg = "Device GPS location unavailable. Falling back to manual coordinate input."
                _locationError.value = errMsg
                _isLoadingLocation.value = false
                Result.failure(IllegalStateException(errMsg))
            }
        } catch (e: Exception) {
            val errMsg = e.localizedMessage ?: "Failed to acquire GPS fix."
            _locationError.value = errMsg
            _isLoadingLocation.value = false
            Result.failure(e)
        }
    }
}

/**
 * Await extension for Play Services Task without external dependency.
 */
private suspend fun <T> Task<T>.await(): T? = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result ->
        continuation.resume(result)
    }
    addOnFailureListener { exception ->
        continuation.resumeWithException(exception)
    }
    addOnCanceledListener {
        continuation.cancel()
    }
}
