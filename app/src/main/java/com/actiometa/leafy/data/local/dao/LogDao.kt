package com.actiometa.leafy.data.local.dao

import androidx.room.*
import com.actiometa.leafy.data.local.entities.CareLogEntity
import com.actiometa.leafy.data.local.entities.PlantImageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCareLog(log: CareLogEntity)

    @Query("SELECT * FROM care_logs WHERE plantId = :plantId ORDER BY timestamp DESC")
    fun getLogsForPlant(plantId: Int): Flow<List<CareLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlantImage(image: PlantImageEntity)

    @Query("SELECT * FROM plant_images WHERE plantId = :plantId ORDER BY timestamp ASC")
    fun getImagesForPlant(plantId: Int): Flow<List<PlantImageEntity>>
}
