package com.actiometa.leafy.domain.usecase

import com.actiometa.leafy.domain.model.CareAction
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class WaterPlantUseCaseTest {

    private val recordCareActionUseCase = mockk<RecordCareActionUseCase>(relaxed = true)
    private val useCase = WaterPlantUseCase(recordCareActionUseCase)

    @Test
    fun `when watering a plant, it should call recordCareActionUseCase with WATER action`() = runTest {
        // Arrange
        val plantId = 1

        // Act
        useCase(plantId)

        // Assert
        coVerify { 
            recordCareActionUseCase(
                plantId = plantId,
                action = CareAction.WATER,
                timestamp = any(),
                content = any()
            ) 
        }
    }
}
