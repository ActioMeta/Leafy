package com.actiometa.leafy.domain.usecase

import com.actiometa.leafy.data.local.entities.CareLogEntity
import com.actiometa.leafy.domain.model.CareAction
import com.actiometa.leafy.domain.repository.GardenRepository
import javax.inject.Inject

class RecordCareActionUseCase @Inject constructor(
    private val gardenRepository: GardenRepository
) {
    suspend operator fun invoke(
        plantId: Int,
        action: CareAction,
        timestamp: Long,
        content: String? = null
    ) {
        val log = CareLogEntity(
            plantId = plantId,
            actionType = action.name,
            timestamp = timestamp,
            contentBlocks = content ?: ""
        )
        gardenRepository.addCareLog(log)
    }
}
