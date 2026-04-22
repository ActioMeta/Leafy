package com.actiometa.leafy.domain.usecase

import com.actiometa.leafy.domain.model.WeatherForecast
import com.actiometa.leafy.domain.repository.LocationRepository
import com.actiometa.leafy.domain.repository.LocationResult
import com.actiometa.leafy.domain.repository.SettingsRepository
import com.actiometa.leafy.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetWeatherAlertUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val locationRepository: LocationRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(): Result<WeatherForecast?> = runCatching {
        val isEnabled = settingsRepository.isWeatherAlertsEnabled.first()
        if (!isEnabled) return@runCatching null

        val location = locationRepository.getUserLocation()
        
        when (location) {
            is LocationResult.CityName -> {
                weatherRepository.getForecastByCity(location.name).getOrThrow()
            }
            is LocationResult.Coordinates -> {
                weatherRepository.getForecastByCoordinates(location.lat, location.lon).getOrThrow()
            }
            else -> {
                // Return null if location is not configured or permission is denied
                null
            }
        }
    }
}
