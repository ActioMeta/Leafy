package com.actiometa.leafy.data.remote.api

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class WeatherApiTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var api: WeatherApi
    private val json = Json { ignoreUnknownKeys = true }

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        val contentType = "application/json".toMediaType()
        api = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(WeatherApi::class.java)
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `getCurrentWeather should parse response correctly`() = runTest {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("""
                {
                    "weather": [{"main": "Rain", "description": "light rain"}],
                    "main": {"temp": 25.5, "humidity": 80},
                    "name": "Duran",
                    "pop": 0.85
                }
            """.trimIndent())
        mockWebServer.enqueue(mockResponse)

        // Act
        val response = api.getCurrentWeather("Duran", "test")

        // Assert
        assertEquals("Duran", response.name)
        assertEquals("Rain", response.weather[0].main)
        assertEquals(25.5, response.main.temp, 0.1)
        assertEquals(0.85, response.pop ?: 0.0, 0.01)
    }
}
