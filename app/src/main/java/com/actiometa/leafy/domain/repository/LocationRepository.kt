package com.actiometa.leafy.domain.repository

sealed class LocationResult {
  data class Coordinates(val lat: Double, val lon: Double) : LocationResult()
  data class CityName(val name: String) : LocationResult()
  object NotConfigured : LocationResult()
  object PermissionDenied : LocationResult()
  object Error : LocationResult()
}

interface LocationRepository {
  suspend fun getUserLocation(): LocationResult
}
