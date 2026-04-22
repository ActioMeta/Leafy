package com.actiometa.leafy

import com.actiometa.leafy.data.remote.dto.PlantNetProjectDto
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Test

/**
 * Test para verificar manualmente la API Key contra los servidores reales.
 * Ejecuta este test en Android Studio haciendo clic en el icono "Play" al lado del nombre.
 */
class ManualApiTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testPlantNetApiKey() {
        // PON TU API KEY AQUÍ PARA PROBAR - FORCING RE-EXECUTION
        val apiKey = "" 
        
        if (apiKey.isEmpty()) {
            println("Error: Debes poner tu API Key en la variable 'apiKey' del test.")
            return
        }

        val client = OkHttpClient()
        val url = "https://my-api.plantnet.org/v2/projects?api-key=${apiKey.trim()}"
        
        val request = Request.Builder()
            .url(url)
            .build()

        runBlocking {
            try {
                val response = client.newCall(request).execute()
                val bodyString = response.body?.string() ?: ""
                
                println("--- RESULTADO DE LA PRUEBA ---")
                println("Código HTTP: ${response.code}")
                
                if (response.isSuccessful) {
                    val projects: List<PlantNetProjectDto> = json.decodeFromString(bodyString)
                    println("✅ La API Key es VÁLIDA.")
                    println("✅ Serialización exitosa. Proyectos encontrados: ${projects.size}")
                    println("Ejemplo: ${projects.firstOrNull()?.title}")
                } else {
                    println("❌ Error: ${response.code} - $bodyString")
                }
            } catch (e: Exception) {
                println("❌ Error: ${e.message}")
            }
        }
    }
}
