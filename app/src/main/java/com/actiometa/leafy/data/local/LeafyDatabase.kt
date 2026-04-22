package com.actiometa.leafy.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.actiometa.leafy.data.local.dao.GardenDao
import com.actiometa.leafy.data.local.dao.LogDao
import com.actiometa.leafy.data.local.entities.CareLogEntity
import com.actiometa.leafy.data.local.entities.PlantEntity
import com.actiometa.leafy.data.local.entities.PlantImageEntity
import com.actiometa.leafy.data.local.entities.SpeciesEntity

@Database(
    entities = [
        SpeciesEntity::class,
        PlantEntity::class,
        PlantImageEntity::class,
        CareLogEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class LeafyDatabase : RoomDatabase() {
    abstract fun gardenDao(): GardenDao
    abstract fun logDao(): LogDao
}
