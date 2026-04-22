package com.actiometa.leafy.data.repository

import com.actiometa.leafy.data.remote.api.BotanyApi
import com.actiometa.leafy.domain.model.IdentificationResult
import com.actiometa.leafy.domain.model.PlantDetails
import com.actiometa.leafy.domain.repository.BotanyRepository
import com.actiometa.leafy.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

class BotanyRepositoryImpl @Inject constructor(
    private val botanyApi: BotanyApi,
    private val settingsRepository: SettingsRepository
) : BotanyRepository {

    companion object {
        private const val TAG = "BotanyRepository"
        private const val PERENUAL_BASE_URL = "https://perenual.com/api/v2"
    }

    override suspend fun validatePlantNetKey(apiKey: String): Result<Boolean> = runCatching {
        botanyApi.listProjects(apiKey)
        true
    }

    override suspend fun validatePerenualKey(apiKey: String): Result<Boolean> = runCatching {
        botanyApi.searchSpecies(
            url = "$PERENUAL_BASE_URL/species-list",
            scientificName = "Rosa",
            apiKey = apiKey
        )
        true
    }

    override suspend fun identifyPlant(
        imageFile: File,
        project: String,
        organ: String
    ): Result<List<IdentificationResult>> = runCatching {
        val apiKey = settingsRepository.plantNetApiKey.first()
            ?: throw Exception("Pl@ntNet API Key not found")

        val imagePart = MultipartBody.Part.createFormData(
            "images",
            imageFile.name,
            imageFile.asRequestBody("image/*".toMediaTypeOrNull())
        )
        val organBody = MultipartBody.Part.createFormData("organs", organ)

        val response = botanyApi.identifyPlant(
            project = project,
            apiKey = apiKey,
            images = listOf(imagePart),
            organs = listOf(organBody)
        )

        response.results.map {
            IdentificationResult(
                scientificName = it.species.scientificNameWithoutAuthor,
                commonName = it.species.commonNames.firstOrNull(),
                confidence = it.score
            )
        }
    }

    override suspend fun getPlantDetails(scientificName: String): Result<PlantDetails> {
        // Para compatibilidad con la interfaz, llamamos a la nueva función con null en commonName
        return getPlantDetailsExtended(scientificName, null)
    }

    override suspend fun getPlantDetailsExtended(scientificName: String, commonName: String?): Result<PlantDetails> = runCatching {
        val apiKey = settingsRepository.perenualApiKey.first()
            ?: throw Exception("Perenual API Key not found")

        // Intento 1: Nombre Científico
        var searchResponse = botanyApi.searchSpecies(
            url = "$PERENUAL_BASE_URL/species-list",
            scientificName = scientificName.trim(),
            apiKey = apiKey
        )
        var speciesBasicInfo = searchResponse.data.firstOrNull()

        // Intento 2: Nombre Común (si el científico falló)
        if (speciesBasicInfo == null && !commonName.isNullOrBlank()) {
            searchResponse = botanyApi.searchSpecies(
                url = "$PERENUAL_BASE_URL/species-list",
                scientificName = commonName.trim(),
                apiKey = apiKey
            )
            speciesBasicInfo = searchResponse.data.firstOrNull()
        }

        // Intento 3: Género
        if (speciesBasicInfo == null && scientificName.contains(" ")) {
            val genus = scientificName.split(" ").first()
            searchResponse = botanyApi.searchSpecies(
                url = "$PERENUAL_BASE_URL/species-list",
                scientificName = genus,
                apiKey = apiKey
            )
            speciesBasicInfo = searchResponse.data.firstOrNull()
        }

        if (speciesBasicInfo == null) throw Exception("Species not found in Perenual")

        // Obtener detalles completos
        val details = botanyApi.getSpeciesDetails(
            url = "$PERENUAL_BASE_URL/species/details/${speciesBasicInfo.id}",
            apiKey = apiKey
        )

        PlantDetails(
            speciesId = details.id.toString(),
            scientificName = scientificName,
            commonName = details.commonName ?: commonName ?: scientificName,
            wateringFrequencyDays = details.wateringBenchmark?.value?.toIntOrNull() ?: 7,
            sunlight = details.sunlight.joinToString(", "),
            cycle = details.cycle,
            maintenance = details.maintenance ?: details.careLevel,
            growthRate = details.growthRate,
            description = details.description,
            edible = details.edible,
            propagation = details.propagation.joinToString(", "),
            pruningMonths = details.pruningMonth.joinToString(", "),
            isPoisonousToHumans = (details.poisonousToHumans ?: 0) > 0,
            isPoisonousToPets = (details.poisonousToPets ?: 0) > 0,
            isIndoor = details.indoor ?: false,
            imagePath = details.defaultImage?.regularUrl
        )
    }
}
