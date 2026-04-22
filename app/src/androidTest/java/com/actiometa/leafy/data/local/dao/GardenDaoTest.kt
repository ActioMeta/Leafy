package com.actiometa.leafy.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.actiometa.leafy.data.local.LeafyDatabase
import com.actiometa.leafy.data.local.entities.PlantEntity
import com.actiometa.leafy.data.local.entities.SpeciesEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GardenDaoTest {

    private lateinit var database: LeafyDatabase
    private lateinit var gardenDao: GardenDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, LeafyDatabase::class.java).build()
        gardenDao = database.gardenDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndGetActivePlants() = runTest {
        // Arrange
        val species = SpeciesEntity("rose_01", "Rosa", "Rose", 3, "Full Sun")
        val plant = PlantEntity(1, "rose_01", "My Rose", System.currentTimeMillis())

        // Act
        gardenDao.insertSpecies(species)
        gardenDao.insertPlant(plant)
        val activePlants = gardenDao.getActivePlants().first()

        // Assert
        assertEquals(1, activePlants.size)
        assertEquals("My Rose", activePlants[0].nickname)
    }

    @Test
    fun getPlantsWithSpeciesMapping() = runTest {
        // Arrange
        val species = SpeciesEntity("cactus_01", "Cactaceae", "Cactus", 15, "Direct Light")
        val plant = PlantEntity(1, "cactus_01", "Spiky", System.currentTimeMillis())

        // Act
        gardenDao.insertSpecies(species)
        gardenDao.insertPlant(plant)
        val mapping = gardenDao.getPlantsWithSpecies().first()

        // Assert
        assertEquals(1, mapping.size)
        val entry = mapping.entries.first()
        assertEquals("Spiky", entry.key.nickname)
        assertEquals("Cactus", entry.value.commonName)
    }
}
