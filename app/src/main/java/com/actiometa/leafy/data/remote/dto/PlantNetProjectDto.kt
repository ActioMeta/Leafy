package com.actiometa.leafy.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PlantNetProjectDto(
    val id: String,
    val title: String,
    val description: String? = null,
    val speciesCount: Int? = null
)
