package com.actiometa.leafy.data.local.dao

import androidx.room.*
import com.actiometa.leafy.data.local.entities.PlantEntity
import com.actiometa.leafy.data.local.entities.SpeciesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GardenDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpecies(species: SpeciesEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlant(plant: PlantEntity)

    @Query("SELECT * FROM plants WHERE isActive = 1")
    fun getActivePlants(): Flow<List<PlantEntity>>

    @Query("SELECT * FROM species WHERE speciesId = :speciesId")
    suspend fun getSpeciesById(speciesId: String): SpeciesEntity?

    @Transaction
    @Query("SELECT * FROM plants WHERE isActive = 1")
    fun getPlantsWithSpecies(): Flow<List<com.actiometa.leafy.data.local.entities.PlantWithSpecies>>

    @Query("""
        SELECT MAX(timestamp) FROM care_logs 
        WHERE plantId = :plantId AND actionType = 'WATER'
    """)
    fun getLastWateringForPlant(plantId: Int): Flow<Long?>

    @Delete
    suspend fun deletePlant(plant: PlantEntity)
}
