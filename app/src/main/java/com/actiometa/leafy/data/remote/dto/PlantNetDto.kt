package com.actiometa.leafy.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlantNetResponseDto(
    val results: List<PlantNetResultDto>
)

@Serializable
data class PlantNetResultDto(
    val score: Double,
    val species: PlantNetSpeciesDto
)

@Serializable
data class PlantNetSpeciesDto(
    val scientificName: String,
    @SerialName("commonNames") val commonNames: List<String> = emptyList(),
    @SerialName("scientificNameWithoutAuthor") val scientificNameWithoutAuthor: String
)
