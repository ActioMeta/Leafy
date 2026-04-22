package com.actiometa.leafy.data.repository

import com.actiometa.leafy.data.remote.api.BotanyApi
import com.actiometa.leafy.data.remote.api.PerenualBasicInfoDto
import com.actiometa.leafy.data.remote.api.PerenualSearchResponseDto
import com.actiometa.leafy.data.remote.dto.*
import com.actiometa.leafy.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BotanyRepositoryImplTest {

    private val botanyApi = mockk<BotanyApi>()
    private val settingsRepository = mockk<SettingsRepository>()
    private val repository = BotanyRepositoryImpl(botanyApi, settingsRepository)

    @Test
    fun `getPlantDetails should chain search and detail calls correctly`() = runTest {
        // Arrange
        coEvery { settingsRepository.perenualApiKey } returns flowOf("fake_key")
        
        coEvery { botanyApi.searchSpecies(any(), "Rosa", "fake_key") } returns PerenualSearchResponseDto(
            data = listOf(PerenualBasicInfoDto(123, "Rose", listOf("Rosa")))
        )
        
        coEvery { botanyApi.getSpeciesDetails(any(), "fake_key") } returns PerenualSpeciesDetailDto(
            id = 123,
            commonName = "Rose",
            scientificName = listOf("Rosa"),
            wateringBenchmark = WateringBenchmarkDto("7", "days"),
            sunlight = listOf("Full Sun"),
            careLevel = "Easy"
        )

        // Act
        val result = repository.getPlantDetails("Rosa").getOrThrow()

        // Assert
        assertEquals("123", result.speciesId)
        assertEquals(7, result.wateringFrequencyDays)
        assertEquals("Full Sun", result.sunlight)
    }
}
