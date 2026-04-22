package com.actiometa.leafy.data.remote.api

import com.actiometa.leafy.data.remote.dto.PlantNetProjectDto
import com.actiometa.leafy.data.remote.dto.PlantNetResponseDto
import kotlinx.serialization.SerialName
import okhttp3.MultipartBody
import retrofit2.http.*

interface BotanyApi {

    // For Validation
    @GET
    suspend fun listProjects(@Url url: String, @Query("api-key") apiKey: String): List<PlantNetProjectDto>

    // Pl@ntNet Identification
    @Multipart
    @POST
    suspend fun identifyPlant(
        @Url url: String,
        @Query("api-key") apiKey: String,
        @Query("lang") lang: String = "en",
        @Part images: List<MultipartBody.Part>,
        @Part organs: List<MultipartBody.Part>
    ): PlantNetResponseDto

    // Trefle API
    @GET("plants/search")
    suspend fun searchTrefleSpecies(
        @Query("q") query: String,
        @Query("token") apiKey: String
    ): TrefleSearchResponseDto

    @GET("plants/{id}")
    suspend fun getTrefleDetails(
        @Path("id") id: Int,
        @Query("token") apiKey: String
    ): TrefleDetailsResponseDto
}

@kotlinx.serialization.Serializable
data class TrefleSearchResponseDto(
    val data: List<TrefleBasicInfoDto>
)

@kotlinx.serialization.Serializable
data class TrefleBasicInfoDto(
    val id: Int,
    @SerialName("common_name") val commonName: String? = null,
    @SerialName("scientific_name") val scientificName: String,
    @SerialName("image_url") val imageUrl: String? = null,
    val family: String? = null,
    val genus: String? = null
)

@kotlinx.serialization.Serializable
data class TrefleDetailsResponseDto(
    val data: TrefleFullDetailsDto
)

@kotlinx.serialization.Serializable
data class TrefleFullDetailsDto(
    val id: Int,
    @SerialName("common_name") val commonName: String? = null,
    @SerialName("scientific_name") val scientificName: String,
    @SerialName("main_species") val mainSpecies: TrefleMainSpeciesDto? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    val family: TrefleNamedEntityDto? = null,
    val genus: TrefleNamedEntityDto? = null,
    val year: Int? = null,
    val author: String? = null,
    val status: String? = null,
    val rank: String? = null
)

@kotlinx.serialization.Serializable
data class TrefleNamedEntityDto(
    val name: String? = null
)

@kotlinx.serialization.Serializable
data class TrefleMainSpeciesDto(
    val duration: List<String>? = null,
    val edible: Boolean? = null,
    val growth: TrefleGrowthDto? = null,
    val specifications: TrefleSpecificationsDto? = null
)

@kotlinx.serialization.Serializable
data class TrefleGrowthDto(
    val light: Int? = null, 
    @SerialName("growth_rate") val growthRate: String? = null,
    @SerialName("minimum_precipitation") val minPrecipitation: TrefleValueDto? = null,
    @SerialName("maximum_precipitation") val maxPrecipitation: TrefleValueDto? = null,
    @SerialName("atmospheric_humidity") val atmosphericHumidity: Int? = null,
    @SerialName("ph_maximum") val phMax: Float? = null,
    @SerialName("ph_minimum") val phMin: Float? = null,
    @SerialName("minimum_temperature") val minTemp: TrefleTemperatureDto? = null,
    @SerialName("maximum_temperature") val maxTemp: TrefleTemperatureDto? = null
)

@kotlinx.serialization.Serializable
data class TrefleSpecificationsDto(
    val toxicity: String? = null,
    @SerialName("growth_habit") val growthHabit: String? = null,
    @SerialName("average_height") val averageHeight: TrefleHeightDto? = null
)

@kotlinx.serialization.Serializable
data class TrefleHeightDto(
    val cm: Int? = null
)

@kotlinx.serialization.Serializable
data class TrefleTemperatureDto(
    @SerialName("deg_c") val degC: Int? = null
)

@kotlinx.serialization.Serializable
data class TrefleValueDto(
    val mm: Float? = null
)
