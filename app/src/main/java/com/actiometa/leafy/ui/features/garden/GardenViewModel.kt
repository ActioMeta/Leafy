package com.actiometa.leafy.ui.features.garden

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.actiometa.leafy.domain.usecase.GardenPlant
import com.actiometa.leafy.domain.usecase.GetGardenWithStatusUseCase
import com.actiometa.leafy.domain.usecase.WaterPlantUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GardenUiState(
    val plants: List<GardenPlant> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class GardenViewModel @Inject constructor(
    private val getGardenWithStatusUseCase: GetGardenWithStatusUseCase,
    private val waterPlantUseCase: WaterPlantUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GardenUiState())
    val uiState: StateFlow<GardenUiState> = _uiState.asStateFlow()

    init {
        loadGarden()
    }

    private fun loadGarden() {
        viewModelScope.launch {
            getGardenWithStatusUseCase()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { plants ->
                    _uiState.update { it.copy(plants = plants, isLoading = false) }
                }
        }
    }

    fun waterPlant(plantId: Int) {
        viewModelScope.launch {
            waterPlantUseCase(plantId)
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }
}
