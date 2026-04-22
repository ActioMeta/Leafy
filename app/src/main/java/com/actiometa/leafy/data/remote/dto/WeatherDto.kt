package com.actiometa.leafy.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class WeatherResponseDto(
    val weather: List<WeatherDescriptionDto>,
    val main: MainWeatherDataDto,
    val name: String,
    val pop: Double? = 0.0
)

@Serializable
data class WeatherDescriptionDto(
    val main: String,
    val description: String
)

@Serializable
data class MainWeatherDataDto(
    val temp: Double,
    val humidity: Int
)
