package com.actiometa.leafy.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PerenualSpeciesDetailDto(
    val id: Int,
    @SerialName("common_name") val commonName: String? = null,
    @SerialName("scientific_name") val scientificName: List<String> = emptyList(),
    @SerialName("other_name") val otherName: List<String> = emptyList(),
    val cycle: String? = null,
    val watering: String? = null,
    val sunlight: List<String> = emptyList(),
    @SerialName("maintenance") val maintenance: String? = null,
    @SerialName("care_level") val careLevel: String? = null,
    @SerialName("growth_rate") val growthRate: String? = null,
    @SerialName("edible_fruit") val edibleFruit: Int? = null,
    @SerialName("edible_leaf") val edibleLeaf: Int? = null,
    val edible: Boolean? = null,
    val description: String? = null,
    @SerialName("watering_general_benchmark") val wateringBenchmark: WateringBenchmarkDto? = null,
    @SerialName("propagation") val propagation: List<String> = emptyList(),
    @SerialName("pruning_month") val pruningMonth: List<String> = emptyList(),
    @SerialName("poisonous_to_humans") val poisonousToHumans: Int? = null,
    @SerialName("poisonous_to_pets") val poisonousToPets: Int? = null,
    @SerialName("indoor") val indoor: Int? = null,
    @SerialName("care_guides") val careGuides: String? = null,
    @SerialName("default_image") val defaultImage: PerenualImageDto? = null
)

@Serializable
data class PerenualImageDto(
    @SerialName("original_url") val originalUrl: String? = null,
    @SerialName("regular_url") val regularUrl: String? = null,
    @SerialName("medium_url") val mediumUrl: String? = null,
    @SerialName("thumbnail") val thumbnail: String? = null
)

@Serializable
data class WateringBenchmarkDto(
    val value: String? = null,
    val unit: String? = null
)
