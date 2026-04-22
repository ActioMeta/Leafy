package com.actiometa.leafy.domain.model

import kotlinx.serialization.Serializable

data class IdentificationResult(
    val scientificName: String,
    val commonName: String?,
    val confidence: Double
)

data class PlantDetails(
    val speciesId: String,
    val scientificName: String,
    val commonName: String,
    val wateringFrequencyDays: Int,
    val sunlight: String,
    val imagePath: String? = null,
    val cycle: String? = "Unknown",
    val maintenance: String? = "Unknown",
    val growthRate: String? = "Unknown",
    val description: String? = null,
    val edible: Boolean? = false,
    val propagation: String? = "Not specified",
    val pruningMonths: String? = "Not specified",
    val isPoisonousToHumans: Boolean = false,
    val isPoisonousToPets: Boolean = false,
    val isIndoor: Boolean = false
)

data class WeatherForecast(
    val cityName: String,
    val condition: String,
    val temperature: Double,
    val rainProbability: Int, // 0 to 100
    val shouldPauseWatering: Boolean
)

enum class CareAction {
    WATER,
    FERTILIZE,
    PRUNE,
    BIOCONTROL,
    REPOT,
    MIST,
    CLEAN_LEAVES,
    NOTE
}

@Serializable
data class CareLogContent(
    val text: String? = null,
    val imageUrls: List<String> = emptyList()
)
