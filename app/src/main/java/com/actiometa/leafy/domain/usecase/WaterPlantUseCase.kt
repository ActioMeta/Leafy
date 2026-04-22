package com.actiometa.leafy.domain.usecase

import com.actiometa.leafy.domain.model.CareAction
import javax.inject.Inject

class WaterPlantUseCase @Inject constructor(
    private val recordCareActionUseCase: RecordCareActionUseCase
) {
    suspend operator fun invoke(plantId: Int): Result<Unit> = runCatching {
        recordCareActionUseCase(
            plantId = plantId,
            action = CareAction.WATER,
            timestamp = System.currentTimeMillis(),
            content = "Quick water action"
        )
    }
}
