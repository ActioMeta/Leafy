package com.actiometa.leafy.domain.repository

import com.actiometa.leafy.domain.model.IdentificationResult
import com.actiometa.leafy.domain.model.PlantDetails
import com.actiometa.leafy.domain.model.WeatherForecast
import java.io.File
import kotlinx.coroutines.flow.Flow

interface BotanyRepository {
    suspend fun validatePlantNetKey(apiKey: String): Result<Boolean>
    suspend fun validatePerenualKey(apiKey: String): Result<Boolean>
    suspend fun identifyPlant(
        imageFile: File,
        project: String = "all",
        organ: String = "leaf"
    ): Result<List<IdentificationResult>>
    suspend fun getPlantDetails(scientificName: String): Result<PlantDetails>
    suspend fun getPlantDetailsExtended(scientificName: String, commonName: String?): Result<PlantDetails>
}

interface WeatherRepository {
    suspend fun validateOpenWeatherKey(apiKey: String): Result<Boolean>
    suspend fun getForecastByCity(city: String): Result<WeatherForecast>
    suspend fun getForecastByCoordinates(lat: Double, lon: Double): Result<WeatherForecast>
}
