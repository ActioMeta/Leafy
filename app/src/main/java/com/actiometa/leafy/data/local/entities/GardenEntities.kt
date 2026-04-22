package com.actiometa.leafy.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "species")
data class SpeciesEntity(
    @PrimaryKey val speciesId: String,
    val scientificName: String,
    val commonName: String,
    val wateringFrequencyDays: Int,
    val lightRequirement: String,
    val cycle: String? = null,
    val maintenance: String? = null,
    val growthRate: String? = null,
    val description: String? = null,
    val edible: Boolean? = false,
    val propagation: String? = null,
    val pruningMonths: String? = null,
    val isPoisonousToHumans: Boolean = false,
    val isPoisonousToPets: Boolean = false,
    val isIndoor: Boolean = false
)

@Serializable
@Entity(
    tableName = "plants",
    foreignKeys = [
        ForeignKey(
            entity = SpeciesEntity::class,
            parentColumns = ["speciesId"],
            childColumns = ["speciesId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["speciesId"])]
)
data class PlantEntity(
    @PrimaryKey(autoGenerate = true) val plantId: Int = 0,
    val speciesId: String,
    val nickname: String,
    val imagePath: String? = null,
    val transplantDate: Long,
    val isActive: Boolean = true
)
