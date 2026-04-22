package com.actiometa.leafy.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.actiometa.leafy.domain.repository.LocationRepository
import com.actiometa.leafy.domain.repository.LocationResult
import com.actiometa.leafy.domain.repository.SettingsRepository
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class LocationRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient,
    private val settingsRepository: SettingsRepository
) : LocationRepository {

    override suspend fun getUserLocation(): LocationResult {
        // 1. Check if user has a manual city set
        val savedCity = settingsRepository.userCityLocation.first()
        if (!savedCity.isNullOrBlank()) {
            return LocationResult.CityName(savedCity)
        }

        // 2. Check GPS permissions
        val hasFineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFineLocation && !hasCoarseLocation) {
            return LocationResult.PermissionDenied
        }

        // 3. Try to get last location
        return try {
            val location = fusedLocationClient.lastLocation.await()
            if (location != null) {
                LocationResult.Coordinates(location.latitude, location.longitude)
            } else {
                LocationResult.Error
            }
        } catch (e: Exception) {
            LocationResult.Error
        }
    }
}
