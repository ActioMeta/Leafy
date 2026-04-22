package com.actiometa.leafy.data.repository

import android.util.Log
import com.actiometa.leafy.data.remote.api.BotanyApi
import com.actiometa.leafy.data.remote.api.TrefleBasicInfoDto
import com.actiometa.leafy.domain.model.IdentificationResult
import com.actiometa.leafy.domain.model.PlantDetails
import com.actiometa.leafy.domain.repository.BotanyRepository
import com.actiometa.leafy.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

class BotanyRepositoryImpl @Inject constructor(
    private val botanyApi: BotanyApi,
    private val settingsRepository: SettingsRepository
) : BotanyRepository {

    companion object {
        private const val TAG = "BotanyRepository"
        private const val PLANTNET_BASE_URL = "https://my-api.plantnet.org/v2/"
    }

    override suspend fun validatePlantNetKey(apiKey: String): Result<Boolean> = runCatching {
        val cleanKey = apiKey.trim()
        try {
            botanyApi.listProjects("${PLANTNET_BASE_URL}projects", cleanKey)
            true
        } catch (e: Exception) {
            Log.e(TAG, "PlantNet validation failed: ${e.message}", e)
            throw e
        }
    }

    override suspend fun validatePerenualKey(apiKey: String): Result<Boolean> = runCatching {
        val cleanKey = apiKey.trim()
        try {
            botanyApi.searchTrefleSpecies(query = "Rosa", apiKey = cleanKey)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Trefle validation failed: ${e.message}", e)
            throw e
        }
    }

    override suspend fun identifyPlant(
        imageFile: File,
        project: String,
        organ: String
    ): Result<List<IdentificationResult>> = runCatching {
        val apiKey = settingsRepository.plantNetApiKey.first()
            ?: throw Exception("Pl@ntNet API Key not found")
        
        val lang = settingsRepository.appLanguage.first()

        val imagePart = MultipartBody.Part.createFormData(
            "images",
            imageFile.name,
            imageFile.asRequestBody("image/*".toMediaTypeOrNull())
        )
        val organBody = MultipartBody.Part.createFormData("organs", organ)

        val response = botanyApi.identifyPlant(
            url = "${PLANTNET_BASE_URL}identify/$project",
            apiKey = apiKey,
            lang = lang,
            images = listOf(imagePart),
            organs = listOf(organBody)
        )
        Log.d(TAG, "PlantNet identify response: $response")

        response.results.map {
            IdentificationResult(
                scientificName = it.species.scientificNameWithoutAuthor,
                commonName = it.species.commonNames.firstOrNull(),
                confidence = it.score
            )
        }
    }

    override suspend fun getPlantDetails(scientificName: String): Result<PlantDetails> {
        return getPlantDetailsExtended(scientificName, null)
    }

    override suspend fun getPlantDetailsExtended(scientificName: String, commonName: String?): Result<PlantDetails> = runCatching {
        val apiKey = settingsRepository.perenualApiKey.first() 
            ?: throw Exception("Trefle API Key not found")

        val cleanName = scientificName.replace(Regex("\\s+(spp\\.|subsp\\.|var\\.|f\\.).*"), "").trim()
        Log.d(TAG, "Starting Trefle search: Scientific=$scientificName, Clean=$cleanName, Common=$commonName")

        var basicInfo: TrefleBasicInfoDto? = null

        // 1. Search by Common Name (User Priority)
        if (!commonName.isNullOrBlank()) {
            try {
                val response = botanyApi.searchTrefleSpecies(query = commonName, apiKey = apiKey)
                basicInfo = response.data.firstOrNull()
            } catch (e: Exception) {
                Log.e(TAG, "Trefle search by Common Name failed: ${e.message}")
            }
        }

        // 2. Search by Scientific Name
        if (basicInfo == null) {
            try {
                val response = botanyApi.searchTrefleSpecies(query = cleanName, apiKey = apiKey)
                basicInfo = response.data.firstOrNull()
            } catch (e: Exception) {
                Log.e(TAG, "Trefle search by Scientific Name failed: ${e.message}")
            }
        }

        // 3. Genus Fallback
        if (basicInfo == null && cleanName.contains(" ")) {
            val genus = cleanName.split(" ").first()
            try {
                val response = botanyApi.searchTrefleSpecies(query = genus, apiKey = apiKey)
                basicInfo = response.data.firstOrNull()
            } catch (e: Exception) {
                Log.e(TAG, "Trefle search by Genus failed: ${e.message}")
            }
        }

        if (basicInfo == null) throw Exception("Species not found in Trefle")

        val detailsResponse = botanyApi.getTrefleDetails(id = basicInfo.id, apiKey = apiKey)
        Log.d(TAG, "Trefle details response: $detailsResponse")
        
        val data = detailsResponse.data
        var mainSpecies = data.mainSpecies
        var growth = mainSpecies?.growth
        var specs = mainSpecies?.specifications

        // Genus Fallback for missing data
        if (growth?.light == null && data.genus?.name != null) {
            try {
                val genusSearch = botanyApi.searchTrefleSpecies(query = data.genus.name, apiKey = apiKey)
                val genusInfo = genusSearch.data.firstOrNull()
                if (genusInfo != null && genusInfo.id != basicInfo.id) {
                    val genusDetails = botanyApi.getTrefleDetails(id = genusInfo.id, apiKey = apiKey)
                    if (genusDetails.data.mainSpecies?.growth?.light != null) {
                        growth = genusDetails.data.mainSpecies.growth
                        specs = genusDetails.data.mainSpecies.specifications
                    }
                }
            } catch (e: Exception) { /* ignore */ }
        }

        // Mapping logic
        val minPrec = growth?.minPrecipitation?.mm ?: 500f
        val wateringDays = when {
            minPrec > 1500 -> 3
            minPrec > 800 -> 7
            minPrec > 300 -> 14
            else -> 21
        }

        val lightLevel = growth?.light ?: 5
        val sunlight = when {
            lightLevel >= 8 -> "Full Sun"
            lightLevel >= 5 -> "Partial Shade"
            else -> "Full Shade"
        }

        val toxicity = specs?.toxicity?.lowercase()
        val isPoisonous = when {
            toxicity == null -> null
            toxicity.contains("none") -> false
            else -> true
        }

        val phRange = if (growth?.phMin != null && growth.phMax != null) "${growth.phMin} - ${growth.phMax}" else null
        val tempRange = if (growth?.minTemp?.degC != null && growth.maxTemp?.degC != null) "${growth.minTemp.degC}°C - ${growth.maxTemp.degC}°C" else null
        val heightMeters = specs?.averageHeight?.cm?.let { "%.1f m".format(it / 100f) }

        PlantDetails(
            speciesId = data.id.toString(),
            scientificName = data.scientificName,
            commonName = commonName ?: data.commonName ?: data.scientificName,
            wateringFrequencyDays = wateringDays,
            sunlight = sunlight,
            imagePath = data.imageUrl ?: basicInfo.imageUrl,
            cycle = mainSpecies?.duration?.joinToString(", "),
            maintenance = growth?.growthRate,
            growthRate = growth?.growthRate,
            edible = mainSpecies?.edible,
            isPoisonous = isPoisonous,
            isIndoor = null,
            family = data.family?.name ?: basicInfo.family,
            genus = data.genus?.name ?: basicInfo.genus,
            year = data.year,
            author = data.author,
            status = data.status,
            rank = data.rank,
            growthHabit = specs?.growthHabit,
            phRange = phRange,
            tempRange = tempRange,
            avgHeight = heightMeters,
            lightLevel = growth?.light,
            atmosphericHumidity = growth?.atmosphericHumidity,
            minPrecipitation = growth?.minPrecipitation?.mm
        )
    }
}
