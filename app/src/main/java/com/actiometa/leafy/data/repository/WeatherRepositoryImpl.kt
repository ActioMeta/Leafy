package com.actiometa.leafy.data.repository

import com.actiometa.leafy.data.remote.api.WeatherApi
import com.actiometa.leafy.domain.model.WeatherForecast
import com.actiometa.leafy.domain.repository.SettingsRepository
import com.actiometa.leafy.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val weatherApi: WeatherApi,
    private val settingsRepository: SettingsRepository
) : WeatherRepository {

    override suspend fun validateOpenWeatherKey(apiKey: String): Result<Boolean> = runCatching {
        weatherApi.getCurrentWeather("London", apiKey)
        true
    }

    override suspend fun getForecastByCity(city: String): Result<WeatherForecast> = runCatching {
        val apiKey = settingsRepository.openWeatherApiKey.first() 
            ?: throw Exception("OpenWeather API Key not found")
            
        val response = weatherApi.getCurrentWeather(city, apiKey)
        val rainProb = (response.pop ?: 0.0) * 100
        
        WeatherForecast(
            cityName = response.name,
            condition = response.weather.firstOrNull()?.main ?: "Unknown",
            temperature = response.main.temp,
            rainProbability = rainProb.toInt(),
            shouldPauseWatering = rainProb > 50 || response.weather.any { it.main == "Rain" }
        )
    }

    override suspend fun getForecastByCoordinates(lat: Double, lon: Double): Result<WeatherForecast> = runCatching {
        val apiKey = settingsRepository.openWeatherApiKey.first()
            ?: throw Exception("OpenWeather API Key not found")
            
        val response = weatherApi.getWeatherByCoordinates(lat, lon, apiKey)
        val rainProb = (response.pop ?: 0.0) * 100
        
        WeatherForecast(
            cityName = response.name,
            condition = response.weather.firstOrNull()?.main ?: "Unknown",
            temperature = response.main.temp,
            rainProbability = rainProb.toInt(),
            shouldPauseWatering = rainProb > 50 || response.weather.any { it.main == "Rain" }
        )
    }
}
