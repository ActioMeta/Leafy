package com.actiometa.leafy.ui.features.scanner

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.actiometa.leafy.domain.model.IdentificationResult
import com.actiometa.leafy.domain.model.PlantDetails
import com.actiometa.leafy.domain.usecase.IdentifyAndGetDetailsUseCase
import com.actiometa.leafy.domain.usecase.AddPlantToGardenUseCase
import com.actiometa.leafy.domain.repository.GardenRepository
import com.actiometa.leafy.data.local.entities.PlantImageEntity
import com.actiometa.leafy.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import android.util.Log

sealed class ScannerUiState {
    object Idle : ScannerUiState()
    object Scanning : ScannerUiState()
    data class Success(val identification: IdentificationResult, val details: PlantDetails) : ScannerUiState()
    data class ConfirmPhoto(val file: File) : ScannerUiState()
    data class Error(val message: String) : ScannerUiState()
    object Added : ScannerUiState()
    object PhotoSaved : ScannerUiState()
}

@HiltViewModel
class ScannerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val identifyUseCase: IdentifyAndGetDetailsUseCase,
    private val addPlantUseCase: AddPlantToGardenUseCase,
    private val gardenRepository: GardenRepository
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Screen.Scanner>()
    val plantId: Int? = route.plantId
    val isMonitoring: Boolean = plantId != null

    private val _uiState = MutableStateFlow<ScannerUiState>(ScannerUiState.Idle)
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    fun onImageCaptured(file: File) {
        if (isMonitoring) {
            // Unicamente capturar y pedir confirmación para guardar
            _uiState.value = ScannerUiState.ConfirmPhoto(file)
        } else {
            // Identificar (solo desde Home)
            identifyPlant(file)
        }
    }

    private fun identifyPlant(imageFile: File) {
        viewModelScope.launch {
            _uiState.value = ScannerUiState.Scanning
            identifyUseCase(imageFile, "all", "leaf")
                .onSuccess { (identification, details) ->
                    _uiState.value = ScannerUiState.Success(identification, details)
                }
                .onFailure { error ->
                    Log.e("ScannerViewModel", "Identification failed", error)
                    _uiState.value = ScannerUiState.Error(error.message ?: "Unknown error")
                }
        }
    }

    fun saveMonitoringPhoto(imageFile: File) {
        val id = plantId ?: return
        viewModelScope.launch {
            try {
                val imageEntity = PlantImageEntity(
                    plantId = id,
                    imagePath = imageFile.absolutePath,
                    timestamp = System.currentTimeMillis(),
                    caption = "Manual Monitoring"
                )
                gardenRepository.addPlantImage(imageEntity)
                _uiState.value = ScannerUiState.PhotoSaved
            } catch (e: Exception) {
                _uiState.value = ScannerUiState.Error("Failed to save photo: ${e.message}")
            }
        }
    }

    fun addToGarden(nickname: String, details: PlantDetails) {
        viewModelScope.launch {
            addPlantUseCase(nickname, details)
                .onSuccess {
                    _uiState.value = ScannerUiState.Added
                }
                .onFailure { error ->
                    _uiState.value = ScannerUiState.Error("Failed to save plant: ${error.message}")
                }
        }
    }
    
    fun reset() {
        _uiState.value = ScannerUiState.Idle
    }
}
