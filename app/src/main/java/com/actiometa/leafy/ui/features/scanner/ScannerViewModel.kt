package com.actiometa.leafy.ui.features.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.actiometa.leafy.domain.model.IdentificationResult
import com.actiometa.leafy.domain.model.PlantDetails
import com.actiometa.leafy.domain.usecase.IdentifyAndGetDetailsUseCase
import com.actiometa.leafy.domain.usecase.AddPlantToGardenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import android.util.Log

sealed class ScannerUiState {
    object Idle : ScannerUiState()
    object Scanning : ScannerUiState()
    data class Success(val identification: IdentificationResult, val details: PlantDetails) : ScannerUiState()
    data class Error(val message: String) : ScannerUiState()
    object Added : ScannerUiState()
}

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val identifyUseCase: IdentifyAndGetDetailsUseCase,
    private val addPlantUseCase: AddPlantToGardenUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScannerUiState>(ScannerUiState.Idle)
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    private val _selectedProject = MutableStateFlow("all")
    val selectedProject: StateFlow<String> = _selectedProject.asStateFlow()

    private val _selectedOrgan = MutableStateFlow("leaf")
    val selectedOrgan: StateFlow<String> = _selectedOrgan.asStateFlow()

    fun setProject(project: String) {
        _selectedProject.value = project
    }

    fun setOrgan(organ: String) {
        _selectedOrgan.value = organ
    }

    fun identifyPlant(imageFile: File) {
        viewModelScope.launch {
            _uiState.value = ScannerUiState.Scanning
            identifyUseCase(imageFile, _selectedProject.value, _selectedOrgan.value)
                .onSuccess { (identification, details) ->
                    _uiState.value = ScannerUiState.Success(identification, details)
                }
                .onFailure { error ->
                    Log.e("ScannerViewModel", "Identification failed", error)
                    _uiState.value = ScannerUiState.Error(error.message ?: "Unknown error")
                }
        }
    }

    fun addToGarden(nickname: String, details: PlantDetails) {
        viewModelScope.launch {
            addPlantUseCase(nickname, details)
                .onSuccess {
                    Log.d("ScannerViewModel", "Plant added successfully: $nickname")
                    _uiState.value = ScannerUiState.Added
                }
                .onFailure { error ->
                    Log.e("ScannerViewModel", "Failed to add plant", error)
                    _uiState.value = ScannerUiState.Error("Failed to save plant: ${error.message}")
                }
        }
    }
    
    fun reset() {
        _uiState.value = ScannerUiState.Idle
    }
}
