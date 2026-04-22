package com.actiometa.leafy.domain.usecase

import com.actiometa.leafy.data.local.entities.PlantEntity
import com.actiometa.leafy.data.local.entities.SpeciesEntity
import com.actiometa.leafy.domain.model.PlantDetails
import com.actiometa.leafy.domain.repository.GardenRepository
import javax.inject.Inject

class AddPlantToGardenUseCase @Inject constructor(
    private val gardenRepository: GardenRepository
) {
    suspend operator fun invoke(nickname: String, details: PlantDetails): Result<Unit> = runCatching {
        val speciesEntity = SpeciesEntity(
            speciesId = details.speciesId,
            scientificName = details.scientificName,
            commonName = details.commonName,
            wateringFrequencyDays = details.wateringFrequencyDays,
            lightRequirement = details.sunlight,
            cycle = details.cycle,
            maintenance = details.maintenance,
            growthRate = details.growthRate,
            description = details.description,
            edible = details.edible,
            propagation = details.propagation,
            pruningMonths = details.pruningMonths,
            isPoisonousToHumans = details.isPoisonousToHumans,
            isPoisonousToPets = details.isPoisonousToPets,
            isIndoor = details.isIndoor
        )

        val plantEntity = PlantEntity(
            speciesId = details.speciesId,
            nickname = nickname,
            imagePath = details.imagePath,
            transplantDate = System.currentTimeMillis()
        )

        gardenRepository.addPlantToGarden(plantEntity, speciesEntity)
    }
}
