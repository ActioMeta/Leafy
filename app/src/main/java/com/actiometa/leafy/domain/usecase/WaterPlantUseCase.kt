package com.actiometa.leafy.domain.usecase

import com.actiometa.leafy.data.local.entities.CareLogEntity
import com.actiometa.leafy.domain.repository.GardenRepository
import javax.inject.Inject

class WaterPlantUseCase @Inject constructor(
    private val gardenRepository: GardenRepository
) {
    suspend operator fun invoke(plantId: Int): Result<Unit> = runCatching {
        val log = CareLogEntity(
            plantId = plantId,
            actionType = "WATER",
            timestamp = System.currentTimeMillis(),
            contentBlocks = "Quick water action"
        )
        gardenRepository.addCareLog(log)
    }
}
