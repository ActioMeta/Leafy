package com.actiometa.leafy.domain.repository

import com.actiometa.leafy.data.local.entities.PlantEntity
import com.actiometa.leafy.data.local.entities.SpeciesEntity
import com.actiometa.leafy.data.local.entities.CareLogEntity
import com.actiometa.leafy.data.local.entities.PlantImageEntity
import kotlinx.coroutines.flow.Flow

interface GardenRepository {
    // Plant & Species Management
    fun getGardenPlants(): Flow<Map<PlantEntity, SpeciesEntity?>>
    suspend fun addPlantToGarden(plant: PlantEntity, species: SpeciesEntity)
    suspend fun removePlant(plant: PlantEntity)
    suspend fun getSpecies(speciesId: String): SpeciesEntity?

    // Logs & History
    fun getCareLogs(plantId: Int): Flow<List<CareLogEntity>>
    suspend fun addCareLog(log: CareLogEntity)
    fun getLastWateringForPlant(plantId: Int): Flow<Long?>
    
    // Gallery
    fun getPlantGallery(plantId: Int): Flow<List<PlantImageEntity>>
    suspend fun addPlantImage(image: PlantImageEntity)
}
