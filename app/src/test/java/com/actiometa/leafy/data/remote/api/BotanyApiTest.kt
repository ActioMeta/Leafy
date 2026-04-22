package com.actiometa.leafy.data.remote.api

import com.actiometa.leafy.data.remote.dto.PlantNetResponseDto
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class BotanyApiTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var api: BotanyApi
    private val json = Json { ignoreUnknownKeys = true }

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        val contentType = "application/json".toMediaType()
        api = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(BotanyApi::class.java)
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `identifyPlant should parse response correctly`() = runTest {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("""
                {
                    "results": [
                        {
                            "score": 0.95,
                            "species": {
                                "scientificName": "Rosa",
                                "scientificNameWithoutAuthor": "Rosa",
                                "commonNames": ["Rose"]
                            }
                        }
                    ]
                }
            """.trimIndent())
        mockWebServer.enqueue(mockResponse)

        // Crear una parte de multipart válida para evitar IllegalStateException
        val imagePart = MultipartBody.Part.createFormData("images", "test.jpg", "dummy".toRequestBody())
        val organPart = MultipartBody.Part.createFormData("organs", "leaf")

        // Act
        val response = api.identifyPlant(
            apiKey = "test", 
            images = listOf(imagePart), 
            organs = listOf(organPart)
        )

        // Assert
        assertEquals(1, response.results.size)
        assertEquals("Rosa", response.results[0].species.scientificName)
    }

    @Test
    fun `getSpeciesDetails should parse perenual response correctly`() = runTest {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("""
                {
                    "id": 1,
                    "common_name": "Rose",
                    "scientific_name": ["Rosa L."],
                    "sunlight": ["Full Sun"],
                    "care_level": "Medium",
                    "watering_general_benchmark": {
                        "value": "7",
                        "unit": "days"
                    }
                }
            """.trimIndent())
        mockWebServer.enqueue(mockResponse)

        // Act
        val response = api.getSpeciesDetails(url = mockWebServer.url("/details").toString(), apiKey = "test")

        // Assert
        assertEquals("Rose", response.commonName)
        assertEquals("7", response.wateringBenchmark?.value)
    }
}
