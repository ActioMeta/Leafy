package com.actiometa.leafy.domain.usecase

import com.actiometa.leafy.data.local.entities.PlantImageEntity
import com.actiometa.leafy.domain.repository.GardenRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetPlantGalleryUseCase @Inject constructor(
    private val gardenRepository: GardenRepository
) {
    operator fun invoke(plantId: Int): Flow<List<PlantImageEntity>> {
        return gardenRepository.getPlantGallery(plantId).map { images ->
            images.sortedByDescending { it.timestamp }
        }
    }
}
