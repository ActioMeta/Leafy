package com.actiometa.leafy.ui.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.actiometa.leafy.domain.repository.LocationRepository
import com.actiometa.leafy.domain.repository.LocationResult
import com.actiometa.leafy.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isWeatherEnabled: Boolean = false,
    val cityLocation: String = "",
    val language: String = "en",
    val isLoading: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val isEnabled = settingsRepository.isWeatherAlertsEnabled.first()
            val city = settingsRepository.userCityLocation.first() ?: ""
            val lang = settingsRepository.appLanguage.first()
            _uiState.value = _uiState.value.copy(
                isWeatherEnabled = isEnabled,
                cityLocation = city,
                language = lang
            )
        }
    }

    fun toggleWeather(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setWeatherPreferences(enabled, _uiState.value.cityLocation)
            _uiState.value = _uiState.value.copy(isWeatherEnabled = enabled)
        }
    }

    fun updateCity(city: String) {
        _uiState.value = _uiState.value.copy(cityLocation = city)
        viewModelScope.launch {
            settingsRepository.setWeatherPreferences(_uiState.value.isWeatherEnabled, city)
        }
    }

    fun updateLanguage(lang: String) {
        _uiState.value = _uiState.value.copy(language = lang)
        viewModelScope.launch {
            settingsRepository.saveLanguage(lang)
        }
    }

    fun useGps() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = locationRepository.getUserLocation()
            if (result is LocationResult.Coordinates) {
                // In a real app, we might reverse geocode here, 
                // but for now we'll just clear the manual city to indicate GPS usage.
                updateCity("")
                _uiState.value = _uiState.value.copy(isLoading = false, message = "GPS Enabled")
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, message = "GPS Error")
            }
        }
    }
    
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}
