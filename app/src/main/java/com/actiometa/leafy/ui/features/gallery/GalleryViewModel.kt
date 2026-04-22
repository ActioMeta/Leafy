package com.actiometa.leafy.ui.features.gallery

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.actiometa.leafy.data.local.entities.PlantImageEntity
import com.actiometa.leafy.domain.usecase.GetPlantGalleryUseCase
import com.actiometa.leafy.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class GalleryUiState(
    val images: List<PlantImageEntity> = emptyList(),
    val isLoading: Boolean = true,
    val selectedPastIndex: Int = 0,
    val latestImage: PlantImageEntity? = null,
    val comparisonImage: PlantImageEntity? = null
)

@HiltViewModel
class GalleryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPlantGalleryUseCase: GetPlantGalleryUseCase
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Screen.Gallery>()
    val plantId = route.plantId

    private val _uiState = MutableStateFlow(GalleryUiState())
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    init {
        loadGallery()
    }

    private fun loadGallery() {
        getPlantGalleryUseCase(plantId)
            .onEach { images ->
                val sortedImages = images.sortedByDescending { it.timestamp }
                val latest = sortedImages.firstOrNull()
                val comparison = if (sortedImages.size > 1) sortedImages[1] else null
                
                _uiState.update { it.copy(
                    images = sortedImages,
                    isLoading = false,
                    latestImage = latest,
                    comparisonImage = comparison,
                    selectedPastIndex = if (sortedImages.size > 1) 1 else 0
                ) }
            }
            .launchIn(viewModelScope)
    }

    fun selectComparisonImage(index: Int) {
        val images = _uiState.value.images
        if (index >= 0 && index < images.size) {
            _uiState.update { it.copy(
                selectedPastIndex = index,
                comparisonImage = images[index]
            ) }
        }
    }
}
