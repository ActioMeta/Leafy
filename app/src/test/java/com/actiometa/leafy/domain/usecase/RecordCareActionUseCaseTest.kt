package com.actiometa.leafy.domain.usecase

import com.actiometa.leafy.domain.model.CareAction
import com.actiometa.leafy.domain.repository.GardenRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RecordCareActionUseCaseTest {

    private val repository = mockk<GardenRepository>(relaxed = true)
    private val useCase = RecordCareActionUseCase(repository)

    @Test
    fun `when recording a pruning action, it should be saved in the repository`() = runTest {
        // Arrange
        val plantId = 1
        val action = CareAction.PRUNE
        val timestamp = 123456789L

        // Act
        useCase(plantId, action, timestamp)

        // Assert
        coVerify { 
            repository.addCareLog(match { 
                it.plantId == plantId && it.actionType == "PRUNE" && it.timestamp == timestamp 
            }) 
        }
    }

    @Test
    fun `when recording a note with content, it should be saved as JSON in contentBlocks`() = runTest {
        // Arrange
        val plantId = 1
        val action = CareAction.NOTE
        val timestamp = 123456789L
        val noteContent = "Looking healthy today"

        // Act
        useCase(plantId, action, timestamp, noteContent)

        // Assert
        coVerify { 
            repository.addCareLog(match { 
                it.plantId == plantId && 
                it.actionType == "NOTE" && 
                it.contentBlocks.contains("\"text\":\"$noteContent\"") 
            }) 
        }
    }
}
