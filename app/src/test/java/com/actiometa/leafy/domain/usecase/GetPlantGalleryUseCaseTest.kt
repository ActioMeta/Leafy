package com.actiometa.leafy.domain.usecase

import com.actiometa.leafy.data.local.entities.PlantImageEntity
import com.actiometa.leafy.domain.repository.GardenRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetPlantGalleryUseCaseTest {

    private val repository = mockk<GardenRepository>()
    private val useCase = GetPlantGalleryUseCase(repository)

    @Test
    fun `when fetching gallery, it should return images sorted by timestamp descending`() = runTest {
        // Arrange
        val plantId = 1
        val images = listOf(
            PlantImageEntity(1, plantId, "uri1", 100L),
            PlantImageEntity(2, plantId, "uri2", 300L),
            PlantImageEntity(3, plantId, "uri3", 200L)
        )
        every { repository.getPlantGallery(plantId) } returns flowOf(images)

        // Act
        val result = useCase(plantId).first()

        // Assert
        assertEquals(3, result.size)
        assertEquals(300L, result[0].timestamp)
        assertEquals(200L, result[1].timestamp)
        assertEquals(100L, result[2].timestamp)
    }
}
