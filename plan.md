# Documento de Arquitectura y Especificación - Leafy

**Plataforma:** Android Nativo (Kotlin, Jetpack Compose, Material Design 3)
**Arquitectura:** Offline-First, Spec-Driven Development (SDD), Test-Driven Development (TDD), Clean Architecture + mvvm
**Almacenamiento Local:** Room (SQLite), DataStore (Preferences/Encrypted)
**Trabajos en Segundo Plano:** WorkManager
**Estrategia de APIs:** BYOK (Bring Your Own Key) para botánica + OpenWeatherMap (Free Tier) para clima.

## Funciones Core de la Aplicación (Features)

1. **Onboarding y BYOK:** Pantalla de configuración inicial obligatoria. El usuario debe ingresar sus propias API Keys (Pl@ntNet, Perenual y OpenWeatherMap). Soporte i18n (Inglés/Español) nativo mediante `res/values`.
2. **Escáner y Diagnóstico:** Captura de fotos para identificar especies (API Pl@ntNet). Bloqueo de UI si faltan credenciales.
3. **Mi Jardín (Offline-First):** Base de datos relacional local (Room). Evita duplicación de datos botánicos usando referencias de `speciesId`.
4. **Programar alertar:** poder programar poda, fertilización, riego, etc.
5. **Care Logs (Diario Visual):** Registro de acciones (`WATER`, `FERTILIZER`, `PRUNE`, etc.) guardados con bloques visuales (JSON). *Feedback Loop:* Registrar un riego manual recalcula las alertas futuras.
6. **Timeline Fotográfico:** Galería cronológica local (Scoped Storage) con slider de comparación "Antes y Después".
7. **Alertas Climáticas Inteligentes (WorkManager):** Tarea en segundo plano que evalúa el clima local para posponer riegos usando la API Key de OpenWeatherMap provista por el usuario. **Enfoque de Privacidad:** El usuario puede ingresar su ciudad manualmente (ej. "Durán, Guayas") para evitar otorgar permisos de GPS, ahorrando batería y protegiendo su privacidad.
8. **Soberanía de Datos:** Exportación/Importación del huerto completo en formato `.json`.

---

## Fase 1: SDD - Especificación de Datos y Contratos

### 1. Gestión de Estado y Privacidad (DataStore)
Controla el flujo inicial, las credenciales y las preferencias de privacidad del clima.

```kotlin
interface SettingsRepository {
    val plantNetApiKey: Flow<String?>
    val perenualApiKey: Flow<String?>
    val openWeatherApiKey: Flow<String?>
    val isOnboardingCompleted: Flow<Boolean>

    // Preferencias de Clima y Privacidad
    val isWeatherAlertsEnabled: Flow<Boolean>
    val userCityLocation: Flow<String?> // Ej: "Durán, EC". Si es nulo, se usa GPS (si hay permiso)

    suspend fun savePlantNetKey(key: String)
    suspend fun savePerenualKey(key: String)
    suspend fun saveOpenWeatherKey(key: String)
    suspend fun completeOnboarding()
    suspend fun setWeatherPreferences(enabled: Boolean, city: String?)
}
```

### 2. Entidades Core de Room (La Base de Datos)

```kotlin
@Serializable
@Entity(tableName = "species")
data class SpeciesEntity(
    @PrimaryKey val speciesId: String, 
    val scientificName: String,
    val commonName: String,
    val wateringFrequencyDays: Int,
    val lightRequirement: String
)

@Serializable
@Entity(
    tableName = "plants",
    foreignKeys = [ForeignKey(entity = SpeciesEntity::class, parentColumns = ["speciesId"], childColumns = ["speciesId"])]
)
data class PlantEntity(
    @PrimaryKey(autoGenerate = true) val plantId: Int = 0,
    val speciesId: String,
    val nickname: String, 
    val transplantDate: Long,
    val isActive: Boolean = true
)

@Serializable
@Entity(tableName = "plant_images")
data class PlantImageEntity(
    @PrimaryKey(autoGenerate = true) val imageId: Int = 0,
    val plantId: Int,
    val imageUri: String,
    val timestamp: Long
)

@Serializable
@Entity(tableName = "care_logs")
data class CareLogEntity(
    @PrimaryKey(autoGenerate = true) val logId: Int = 0,
    val plantId: Int,
    val actionType: String, // "WATER", "FERTILIZER", "BIOCONTROL", "PRUNE", "NOTE"
    val timestamp: Long,
    val contentBlocks: String 
)
```

### 3. Contratos de Clima y Ubicación

```kotlin
// Contrato de Ubicación (Abstrae si viene de Input manual o GPS)
sealed class LocationResult {
    data class Coordinates(val lat: Double, val lon: Double) : LocationResult()
    data class CityName(val name: String) : LocationResult()
    object NotConfigured : LocationResult()
}

interface LocationRepository {
    suspend fun getUserLocation(): LocationResult
}

// Contrato de Clima (Consumirá OpenWeatherMap API)
data class WeatherForecast(
    val willRainToday: Boolean,
    val rainProbability: Int, // 0 a 100
    val conditionDescription: String
)

interface WeatherRepository {
    // endpoint esperado: api.openweathermap.org/data/2.5/weather?q={city} ó ?lat={lat}&lon={lon}
    suspend fun getTodayForecast(location: LocationResult): WeatherForecast?
}

// Contrato de Evaluación Lógica
sealed class WeatherCheckResult {
    data class SuggestPause(val affectedPlantIds: List<Int>) : WeatherCheckResult()
    object NoActionNeeded : WeatherCheckResult()
}
```

---

## Fase 2: Arquitectura de Background (WorkManager)

Para evitar drenar la batería, la aplicación **no** consultará el clima mientras el usuario navega por ella. 

**Clase `WeatherWorker` (Hereda de `CoroutineWorker`):**
1.  **Programación:** Se encola mediante `PeriodicWorkRequestBuilder` para ejecutarse cada 24 horas (idealmente temprano en la mañana, ej. 6:00 AM).
2.  **Restricciones (Constraints):** Solo se ejecuta si hay conexión a Internet (`NetworkType.CONNECTED`).
3.  **Flujo de Ejecución interno:**
    * Lee `isWeatherAlertsEnabled` del `SettingsRepository`. Si es falso, retorna `Result.success()` y termina.
    * Llama a `LocationRepository` (lee la ciudad manual o pide GPS si está autorizado).
    * Llama a `WeatherRepository.getTodayForecast()`.
    * Si `willRainToday == true`, consulta el DAO (`GardenDao.getPlantsNeedingWaterToday()`).
    * Si hay plantas, utiliza `NotificationManagerCompat` para disparar una Alerta Push Local sugeriendo pausar el riego.

---

## Fase 3: Plan de Implementación TDD

### Paso 1: TDD para el Caso de Uso de Clima (Lógica Pura)
Verificar que la orquestación entre clima y base de datos funciona sin invocar a Android.

1.  **Escribir la prueba (Rojo): Ruta de Lluvia**
```kotlin
@Test
fun `Si hay lluvia pronosticada y hay plantas por regar, se genera una alerta de posposición`() = runTest {
    // Arrange
    val locationRepo = mockk<LocationRepository>()
    coEvery { locationRepo.getUserLocation() } returns LocationResult.CityName("Guayas")
    
    val weatherRepo = mockk<WeatherRepository>()
    coEvery { weatherRepo.getTodayForecast(any()) } returns WeatherForecast(true, 85, "Lluvia intensa")
    
    val gardenDao = mockk<GardenDao>()
    coEvery { gardenDao.getPlantsNeedingWaterToday() } returns listOf(PlantEntity(1, "Coffea", "Café", 123L))

    val useCase = CheckWeatherAndWateringUseCase(locationRepo, weatherRepo, gardenDao)

    // Act
    val result = useCase.invoke()

    // Assert
    assertTrue(result is WeatherCheckResult.SuggestPause)
    assertEquals(1, (result as WeatherCheckResult.SuggestPause).affectedPlantIds.size)
}
```

2.  **Escribir la prueba (Rojo): Ruta Segura (No hay plantas que regar)**
```kotlin
@Test
fun `Si hay lluvia pronosticada pero NO hay plantas por regar, no se hace nada`() = runTest {
    // Arrange
    val locationRepo = mockk<LocationRepository>()
    coEvery { locationRepo.getUserLocation() } returns LocationResult.Coordinates(-2.17, -79.82)
    
    val weatherRepo = mockk<WeatherRepository>()
    coEvery { weatherRepo.getTodayForecast(any()) } returns WeatherForecast(true, 90, "Tormenta")
    
    val gardenDao = mockk<GardenDao>()
    coEvery { gardenDao.getPlantsNeedingWaterToday() } returns emptyList() // Base de datos vacía hoy

    val useCase = CheckWeatherAndWateringUseCase(locationRepo, weatherRepo, gardenDao)

    // Act
    val result = useCase.invoke()

    // Assert
    assertTrue(result is WeatherCheckResult.NoActionNeeded)
}
```
*Implementación (Verde):* Escribir la lógica en `CheckWeatherAndWateringUseCase`.

### Paso 2: TDD para el Enrutamiento (Onboarding)
Verificar la redirección a la pantalla de Setup.

1.  **Escribir la prueba (Rojo):**
```kotlin
@Test
fun `Cuando isOnboardingCompleted es falso, la ruta inicial es SetupScreen`() = runTest {
    // Arrange
    val settingsRepo = mockk<SettingsRepository>()
    every { settingsRepo.isOnboardingCompleted } returns flowOf(false)
    val viewModel = MainViewModel(settingsRepo)

    // Act
    val startDestination = viewModel.startDestination.first()

    // Assert
    assertEquals(Routes.SetupScreen, startDestination)
}
```

---

## Fase 4: Diseño UX/UI (Compose & Material Design 3)

### Pantalla de Ajustes: Componente de Privacidad Climática
La interfaz de usuario evitará solicitar permisos de sistema (`ACCESS_COARSE_LOCATION`) a menos que el usuario lo exija explícitamente.

**Especificación Visual (Compose):**
1.  **Switch Component:** `Switch` etiquetado con `stringResource(R.string.enable_weather_alerts)`.
2.  **AnimatedVisibility:** Si el Switch está en `true`, se despliega hacia abajo el contenedor de ubicación.
3.  **Input Manual (Recomendado):** Un `OutlinedTextField` donde el usuario escribe su ciudad (Ej: "Guayaquil", "Durán"). El *label* debe indicar "(Recomendado para ahorrar batería)".
4.  **Botón GPS (Opcional):** Un `TextButton` debajo del input que diga "Usar mi ubicación actual". Solo si el usuario hace clic aquí, se lanza el `rememberLauncherForActivityResult` para pedir permisos de GPS usando `FusedLocationProviderClient`.

---

## Estado de Avance (Actualizado: 21 de Abril, 2026)

### Fase 1: Completada
- [x] **Onboarding BYOK:** Validación y guardado de API Keys (Pl@ntNet, Perenual, OpenWeather).
- [x] **Arquitectura Base:** Clean Architecture + MVVM + Hilt + Room + DataStore.
- [x] **Escáner (Identificación):** Integración de CameraX. Identificación orquestada entre Pl@ntNet y Perenual.
- [x] **Mi Jardín:** Lista reactiva con estados de carga (shimmer).
- [x] **Riego Rápido:** Lógica de `WaterPlantUseCase` e historial de logs funcional.
- [x] **Lógica de Estado:** Cálculo dinámico de necesidad de riego basado en frecuencia de especie y último log.
- [x] **Traducciones Base:** Archivos `strings.xml` (EN/ES) sincronizados con las funciones actuales.

### Fase 2: Completada (Alertas Climáticas)
- [x] **LocationRepository:** Lógica híbrida para ubicación (Ciudad manual / GPS).
- [x] **WeatherWorker:** Proceso en segundo plano con WorkManager (24h) y notificaciones de lluvia.
- [x] **Configuración de Ubicación:** Implementada `SettingsScreen` con control de privacidad y GPS.
- [x] **Notificaciones:** Configuración de canales y alertas push locales.

### Fase 3: Completada (Detalle y Care Logs Base)
- [x] **Detalle de Planta Enriquecido:** Pantalla con información completa de la API (toxicidad, poda, propagación, interiores).
- [x] **Integración de Clima en Tiempo Real:** El detalle de la planta muestra el pronóstico basado en la ubicación configurada.
- [x] **Persistencia Extendida:** Actualización de Room para guardar atributos botánicos avanzados.

### Fase 4: Siguiente Prioridad (Timeline y Refactorización)
- [ ] **Timeline Fotográfico:** Implementar el slider de comparación "Antes y Después" y galería cronológica.
- [ ] **Optimización Scanner:** Migrar `LocalLifecycleOwner` a `androidx.lifecycle.compose`.
- [ ] **Care Logs Avanzados:** Registro visual de acciones específicas (Poda, Fertilización).
- [ ] **Exportación/Importación:** Funcionalidad de backup en JSON.

