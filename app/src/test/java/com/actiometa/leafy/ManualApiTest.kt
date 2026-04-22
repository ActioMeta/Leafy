package com.actiometa.leafy

import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Test

/**
 * Test para verificar manualmente la API Key contra los servidores reales.
 * Ejecuta este test en Android Studio haciendo clic en el icono "Play" al lado del nombre.
 */
class ManualApiTest {

    @Test
    fun testPlantNetApiKey() {
        // PON TU API KEY AQUÍ PARA PROBAR
        val apiKey = "2b10RaCBRdhsySoikwmr04" 
        
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
                println("--- RESULTADO DE LA PRUEBA ---")
                println("URL probada: https://my-api.plantnet.org/v2/projects?api-key=***")
                println("Código HTTP: ${response.code}")
                println("Mensaje: ${response.message}")
                println("Cuerpo: ${response.body?.string()}")
                
                if (response.isSuccessful) {
                    println("La API Key es VÁLIDA.")
                } else {
                    println("La API Key es INVÁLIDA o el servidor la rechazó.")
                }
            } catch (e: Exception) {
                println("Error de red: ${e.message}")
            }
        }
    }
}
