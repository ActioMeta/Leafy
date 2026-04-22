package com.actiometa.leafy.data.repository

import com.actiometa.leafy.data.local.dao.GardenDao
import com.actiometa.leafy.data.local.dao.LogDao
import com.actiometa.leafy.data.local.entities.CareLogEntity
import com.actiometa.leafy.data.local.entities.PlantEntity
import com.actiometa.leafy.data.local.entities.PlantImageEntity
import com.actiometa.leafy.data.local.entities.SpeciesEntity
import com.actiometa.leafy.data.local.entities.PlantWithSpecies
import com.actiometa.leafy.domain.repository.GardenRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GardenRepositoryImpl @Inject constructor(
    private val gardenDao: GardenDao,
    private val logDao: LogDao
) : GardenRepository {

    override fun getGardenPlants(): Flow<List<PlantWithSpecies>> =
        gardenDao.getPlantsWithSpecies()

    override suspend fun addPlantToGarden(plant: PlantEntity, species: SpeciesEntity) {
        gardenDao.insertSpecies(species)
        gardenDao.insertPlant(plant)
    }

    override suspend fun removePlant(plant: PlantEntity) {
        gardenDao.deletePlant(plant)
    }

    override suspend fun getSpecies(speciesId: String): SpeciesEntity? =
        gardenDao.getSpeciesById(speciesId)

    override fun getCareLogs(plantId: Int): Flow<List<CareLogEntity>> =
        logDao.getLogsForPlant(plantId)

    override suspend fun addCareLog(log: CareLogEntity) {
        logDao.insertCareLog(log)
    }

    override fun getLastWateringForPlant(plantId: Int): Flow<Long?> =
        gardenDao.getLastWateringForPlant(plantId)

    override fun getPlantGallery(plantId: Int): Flow<List<PlantImageEntity>> =
        logDao.getImagesForPlant(plantId)

    override suspend fun addPlantImage(image: PlantImageEntity) {
        logDao.insertPlantImage(image)
    }
}
