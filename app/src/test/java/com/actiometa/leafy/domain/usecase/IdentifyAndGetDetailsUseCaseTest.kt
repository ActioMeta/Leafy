package com.actiometa.leafy.domain.usecase

import com.actiometa.leafy.domain.model.IdentificationResult
import com.actiometa.leafy.domain.model.PlantDetails
import com.actiometa.leafy.domain.repository.BotanyRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class IdentifyAndGetDetailsUseCaseTest {

    private val repository = mockk<BotanyRepository>()
    private val useCase = IdentifyAndGetDetailsUseCase(repository)

    @Test
    fun `when identification succeeds, it should fetch details from perenual`() = runTest {
        // Arrange
        val file = mockk<File>(relaxed = true)
        val identification = IdentificationResult("Rosa", "Rose", 0.9)
        val details = PlantDetails("123", "Rosa", "Rose", 7, "Sun")

        coEvery { repository.identifyPlant(file, any(), any()) } returns Result.success(listOf(identification))
        coEvery { repository.getPlantDetailsExtended("Rosa", any()) } returns Result.success(details)

        // Act
        val result = useCase(file).getOrThrow()

        // Assert
        assertEquals("Rosa", result.first.scientificName)
        assertEquals("123", result.second.speciesId)
    }
}
