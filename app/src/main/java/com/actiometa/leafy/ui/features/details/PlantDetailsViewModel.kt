package com.actiometa.leafy.ui.features.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.actiometa.leafy.domain.model.WeatherForecast
import com.actiometa.leafy.domain.repository.GardenRepository
import com.actiometa.leafy.domain.usecase.GetWeatherAlertUseCase
import com.actiometa.leafy.domain.usecase.GardenPlant
import com.actiometa.leafy.domain.usecase.WaterPlantUseCase
import com.actiometa.leafy.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlantDetailsUiState(
    val plant: GardenPlant? = null,
    val weather: WeatherForecast? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class PlantDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val gardenRepository: GardenRepository,
    private val getWeatherAlertUseCase: GetWeatherAlertUseCase,
    private val waterPlantUseCase: WaterPlantUseCase
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Screen.PlantDetails>()
    private val plantId = route.plantId

    private val _uiState = MutableStateFlow(PlantDetailsUiState())
    val uiState: StateFlow<PlantDetailsUiState> = _uiState.asStateFlow()

    init {
        loadPlantDetails()
    }

    private fun loadPlantDetails() {
        viewModelScope.launch {
            // Buscamos la planta y combinamos con su último riego
            gardenRepository.getGardenPlants().flatMapLatest { plantList ->
                val relation = plantList.find { it.plant.plantId == plantId }
                if (relation == null) return@flatMapLatest flowOf(null)
                
                val plant = relation.plant
                val species = relation.species
                gardenRepository.getLastWateringForPlant(plantId).map { lastWatering ->
                    val frequency = species?.wateringFrequencyDays ?: 7
                    val nextWatering = if (lastWatering != null) lastWatering + (frequency * 24 * 60 * 60 * 1000L) else 0L
                    val needsWater = System.currentTimeMillis() >= nextWatering

                    GardenPlant(
                        plantId = plant.plantId,
                        nickname = plant.nickname,
                        speciesId = plant.speciesId,
                        scientificName = species?.scientificName ?: "Unknown",
                        commonName = species?.commonName ?: plant.nickname,
                        wateringFrequencyDays = frequency,
                        sunlight = species?.lightRequirement ?: "Unknown",
                        lastWatering = lastWatering,
                        isNeedsWater = needsWater,
                        imagePath = plant.imagePath,
                        cycle = species?.cycle,
                        maintenance = species?.maintenance,
                        growthRate = species?.growthRate,
                        description = species?.description,
                        edible = species?.edible,
                        propagation = species?.propagation,
                        pruningMonths = species?.pruningMonths,
                        isPoisonousToHumans = species?.isPoisonousToHumans ?: false,
                        isPoisonousToPets = species?.isPoisonousToPets ?: false,
                        isIndoor = species?.isIndoor ?: false
                    )
                }
            }.filterNotNull()
            .collect { plant ->
                _uiState.update { it.copy(plant = plant, isLoading = false) }
                loadWeather()
            }
        }
    }

    private fun loadWeather() {
        viewModelScope.launch {
            getWeatherAlertUseCase()
                .onSuccess { forecast ->
                    _uiState.update { it.copy(weather = forecast) }
                }
        }
    }

    fun waterPlant() {
        viewModelScope.launch {
            waterPlantUseCase(plantId)
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }
}
