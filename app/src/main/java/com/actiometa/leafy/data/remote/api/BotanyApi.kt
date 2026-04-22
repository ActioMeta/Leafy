package com.actiometa.leafy.data.remote.api

import com.actiometa.leafy.data.remote.dto.PerenualSpeciesDetailDto
import com.actiometa.leafy.data.remote.dto.PlantNetProjectDto
import com.actiometa.leafy.data.remote.dto.PlantNetResponseDto
import kotlinx.serialization.SerialName
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.*

interface BotanyApi {

    // For Validation
    @GET("projects")
    suspend fun listProjects(
        @Query("api-key") apiKey: String
    ): List<PlantNetProjectDto>

    // Pl@ntNet Identification (Using Base URL from Retrofit)
    @Multipart
    @POST("identify/{project}")
    suspend fun identifyPlant(
        @Path("project") project: String = "all",
        @Query("api-key") apiKey: String,
        @Part images: List<MultipartBody.Part>,
        @Part organs: List<MultipartBody.Part>
    ): PlantNetResponseDto

    // Perenual Species Details (Using full URL or dynamic base)
    @GET
    suspend fun getSpeciesDetails(
        @Url url: String, // e.g., "https://perenual.com/api/species-details/{id}"
        @Query("key") apiKey: String
    ): PerenualSpeciesDetailDto
    
    @GET
    suspend fun searchSpecies(
        @Url url: String, // e.g., "https://perenual.com/api/species-list"
        @Query("q") scientificName: String,
        @Query("key") apiKey: String
    ): PerenualSearchResponseDto
}

@kotlinx.serialization.Serializable
data class PerenualSearchResponseDto(
    val data: List<PerenualBasicInfoDto>
)

@kotlinx.serialization.Serializable
data class PerenualBasicInfoDto(
    val id: Int,
    @SerialName("common_name") val commonName: String?,
    @SerialName("scientific_name") val scientificName: List<String>
)
