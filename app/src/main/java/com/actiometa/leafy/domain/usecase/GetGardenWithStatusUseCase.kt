package com.actiometa.leafy.domain.usecase

import com.actiometa.leafy.data.local.entities.PlantEntity
import com.actiometa.leafy.data.local.entities.SpeciesEntity
import com.actiometa.leafy.domain.repository.GardenRepository
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class GardenPlant(
    val plantId: Int,
    val nickname: String,
    val speciesId: String,
    val scientificName: String,
    val commonName: String,
    val wateringFrequencyDays: Int,
    val sunlight: String,
    val lastWatering: Long?,
    val isNeedsWater: Boolean,
    val imagePath: String? = null,
    val cycle: String? = null,
    val maintenance: String? = null,
    val growthRate: String? = null,
    val description: String? = null,
    val edible: Boolean? = false,
    val propagation: String? = null,
    val pruningMonths: String? = null,
    val isPoisonousToHumans: Boolean = false,
    val isPoisonousToPets: Boolean = false,
    val isIndoor: Boolean = false
)

class GetGardenWithStatusUseCase @Inject constructor(
    private val gardenRepository: GardenRepository
) {
    operator fun invoke(): Flow<List<GardenPlant>> {
        return gardenRepository.getGardenPlants().flatMapLatest { plantMap ->
            if (plantMap.isEmpty()) return@flatMapLatest flowOf(emptyList())
            
            val plantFlows = plantMap.map { (plant, species) ->
                gardenRepository.getLastWateringForPlant(plant.plantId).map { lastWatering ->
                    val wateringDays = species?.wateringFrequencyDays ?: 7
                    val needsWater = checkNeedsWater(lastWatering, wateringDays)
                    GardenPlant(
                        plantId = plant.plantId,
                        nickname = plant.nickname,
                        speciesId = plant.speciesId,
                        scientificName = species?.scientificName ?: "Unknown",
                        commonName = species?.commonName ?: plant.nickname,
                        wateringFrequencyDays = wateringDays,
                        sunlight = species?.lightRequirement ?: "Unknown",
                        lastWatering = lastWatering,
                        isNeedsWater = needsWater,
                        imagePath = plant.imagePath,
                        cycle = species?.cycle,
                        maintenance = species?.maintenance,
                        growthRate = species?.growthRate,
                        description = species?.description,
                        edible = species?.edible,
                        propagation = species?.propagation,
                        pruningMonths = species?.pruningMonths,
                        isPoisonousToHumans = species?.isPoisonousToHumans ?: false,
                        isPoisonousToPets = species?.isPoisonousToPets ?: false,
                        isIndoor = species?.isIndoor ?: false
                    )
                }
            }
            combine(plantFlows) { it.toList() }
        }
    }

    private fun checkNeedsWater(lastWatering: Long?, frequencyDays: Int): Boolean {
        if (lastWatering == null) return true
        val nextWatering = lastWatering + (frequencyDays * 24 * 60 * 60 * 1000L)
        return System.currentTimeMillis() >= nextWatering
    }
}
