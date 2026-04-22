package com.actiometa.leafy.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "plant_images")
data class PlantImageEntity(
    @PrimaryKey(autoGenerate = true) val imageId: Int = 0,
    val plantId: Int,
    val imageUri: String,
    val timestamp: Long
)

@Serializable
@Entity(tableName = "care_logs")
data class CareLogEntity(
    @PrimaryKey(autoGenerate = true) val logId: Int = 0,
    val plantId: Int,
    val actionType: String, // "WATER", "FERTILIZER", "PRUNE", etc.
    val timestamp: Long,
    val contentBlocks: String // JSON formatted notes/details
)
