package com.actiometa.leafy.domain.usecase

import com.actiometa.leafy.data.local.entities.CareLogEntity
import com.actiometa.leafy.domain.model.CareAction
import com.actiometa.leafy.domain.model.CareLogContent
import com.actiometa.leafy.domain.repository.GardenRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
        val contentJson = Json.encodeToString(CareLogContent(text = content))
        
        val log = CareLogEntity(
            plantId = plantId,
            actionType = action.name,
            timestamp = timestamp,
            contentBlocks = contentJson
        )
        gardenRepository.addCareLog(log)
    }
}
