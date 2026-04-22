package com.actiometa.leafy.data.repository

import com.actiometa.leafy.data.local.dao.GardenDao
import com.actiometa.leafy.data.local.dao.LogDao
import com.actiometa.leafy.data.local.entities.PlantEntity
import com.actiometa.leafy.data.local.entities.SpeciesEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GardenRepositoryImplTest {

    private val gardenDao = mockk<GardenDao>(relaxed = true)
    private val logDao = mockk<LogDao>(relaxed = true)
    private val repository = GardenRepositoryImpl(gardenDao, logDao)

    @Test
    fun `when adding a plant, species should be inserted first`() = runTest {
        // Arrange
        val species = SpeciesEntity("sp1", "Scientific Name", "Common Name", 7, "Sun")
        val plant = PlantEntity(plantId = 1, speciesId = "sp1", nickname = "Nickname", transplantDate = 1000L)

        // Act
        repository.addPlantToGarden(plant, species)

        // Assert
        coVerify(exactly = 1) { gardenDao.insertSpecies(species) }
        coVerify(exactly = 1) { gardenDao.insertPlant(plant) }
    }

    @Test
    fun `when removing a plant, it should be deleted from gardenDao`() = runTest {
        // Arrange
        val plant = PlantEntity(plantId = 1, speciesId = "sp1", nickname = "Nickname", transplantDate = 1000L)

        // Act
        repository.removePlant(plant)

        // Assert
        coVerify(exactly = 1) { gardenDao.deletePlant(plant) }
    }
}
