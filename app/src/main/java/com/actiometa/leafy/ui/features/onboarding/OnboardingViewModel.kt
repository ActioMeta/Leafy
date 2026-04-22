package com.actiometa.leafy.ui.features.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.actiometa.leafy.domain.repository.BotanyRepository
import com.actiometa.leafy.domain.repository.SettingsRepository
import com.actiometa.leafy.domain.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val plantNetKey: String = "",
    val perenualKey: String = "",
    val openWeatherKey: String = "",
    val isSaving: Boolean = false,
    val validationMessage: String? = null,
    val error: String? = null
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val botanyRepository: BotanyRepository,
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onPlantNetKeyChange(newKey: String) {
        _uiState.update { it.copy(plantNetKey = newKey, error = null) }
    }

    fun onPerenualKeyChange(newKey: String) {
        _uiState.update { it.copy(perenualKey = newKey, error = null) }
    }

    fun onOpenWeatherKeyChange(newKey: String) {
        _uiState.update { it.copy(openWeatherKey = newKey, error = null) }
    }

    fun saveAndContinue() {
        val currentState = _uiState.value
        if (currentState.plantNetKey.isBlank() || 
            currentState.perenualKey.isBlank() || 
            currentState.openWeatherKey.isBlank()) {
            _uiState.update { it.copy(error = "All keys are required") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, validationMessage = "Validating keys...") }
            
            // 1. Validate Pl@ntNet
            val plantNetResult = botanyRepository.validatePlantNetKey(currentState.plantNetKey)
            if (plantNetResult.isFailure) {
                _uiState.update { it.copy(isSaving = false, error = "Invalid Pl@ntNet API Key") }
                return@launch
            }

            // 2. Validate Trefle
            val perenualResult = botanyRepository.validatePerenualKey(currentState.perenualKey)
            if (perenualResult.isFailure) {
                _uiState.update { it.copy(isSaving = false, error = "Invalid Trefle.io API Key") }
                return@launch
            }

            // 3. Validate OpenWeather
            val weatherResult = weatherRepository.validateOpenWeatherKey(currentState.openWeatherKey)
            if (weatherResult.isFailure) {
                _uiState.update { it.copy(isSaving = false, error = "Invalid OpenWeather API Key") }
                return@launch
            }

            // All valid, save
            try {
                settingsRepository.savePlantNetKey(currentState.plantNetKey)
                settingsRepository.savePerenualKey(currentState.perenualKey)
                settingsRepository.saveOpenWeatherKey(currentState.openWeatherKey)
                settingsRepository.completeOnboarding()
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }
}
