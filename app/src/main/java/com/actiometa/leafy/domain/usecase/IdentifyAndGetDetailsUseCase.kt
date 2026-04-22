package com.actiometa.leafy.domain.usecase

import com.actiometa.leafy.domain.model.IdentificationResult
import com.actiometa.leafy.domain.model.PlantDetails
import com.actiometa.leafy.domain.repository.BotanyRepository
import java.io.File
import javax.inject.Inject

/**
 * Orquestador que identifica una planta por foto y automáticamente busca sus 
 * detalles de cuidado en la base de datos de Perenual.
 */
class IdentifyAndGetDetailsUseCase @Inject constructor(
    private val botanyRepository: BotanyRepository
) {
    suspend operator fun invoke(
        imageFile: File,
        project: String = "all",
        organ: String = "leaf"
    ): Result<Pair<IdentificationResult, PlantDetails>> = runCatching {
        // 1. Identificar con Pl@ntNet
        val identifications = botanyRepository.identifyPlant(imageFile, project, organ).getOrThrow()
        val bestMatch = identifications.firstOrNull() 
            ?: throw Exception("No se pudo identificar la planta")

        // 2. Obtener detalles de Perenual usando búsqueda extendida
        val details = botanyRepository.getPlantDetailsExtended(
            scientificName = bestMatch.scientificName,
            commonName = bestMatch.commonName
        ).getOrElse {
            PlantDetails(
                speciesId = "unknown",
                scientificName = bestMatch.scientificName,
                commonName = bestMatch.commonName ?: bestMatch.scientificName,
                wateringFrequencyDays = 7,
                sunlight = "Unknown"
            )
        }.copy(
            imagePath = imageFile.absolutePath
        )

        bestMatch to details
    }
}
