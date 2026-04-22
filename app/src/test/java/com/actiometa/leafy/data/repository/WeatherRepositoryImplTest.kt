package com.actiometa.leafy.data.repository

import com.actiometa.leafy.data.remote.api.WeatherApi
import com.actiometa.leafy.data.remote.dto.MainWeatherDataDto
import com.actiometa.leafy.data.remote.dto.WeatherDescriptionDto
import com.actiometa.leafy.data.remote.dto.WeatherResponseDto
import com.actiometa.leafy.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherRepositoryImplTest {

    private val weatherApi = mockk<WeatherApi>()
    private val settingsRepository = mockk<SettingsRepository>()
    private val repository = WeatherRepositoryImpl(weatherApi, settingsRepository)

    @Test
    fun `when rain probability is high, shouldPauseWatering should be true`() = runTest {
        // Arrange
        coEvery { settingsRepository.openWeatherApiKey } returns flowOf("fake_key")
        coEvery { weatherApi.getCurrentWeather(any(), any(), any()) } returns WeatherResponseDto(
            weather = listOf(WeatherDescriptionDto("Rain", "light rain")),
            main = MainWeatherDataDto(20.0, 80),
            name = "Duran",
            pop = 0.8
        )

        // Act
        val result = repository.getForecastByCity("Duran").getOrThrow()

        // Assert
        assertTrue(result.shouldPauseWatering)
        assertEquals(80, result.rainProbability)
    }

    @Test
    fun `when no api key is present, should return failure`() = runTest {
        // Arrange
        coEvery { settingsRepository.openWeatherApiKey } returns flowOf(null)

        // Act
        val result = repository.getForecastByCity("Duran")

        // Assert
        assertTrue(result.isFailure)
        assertEquals("OpenWeather API Key not found", result.exceptionOrNull()?.message)
    }
}
