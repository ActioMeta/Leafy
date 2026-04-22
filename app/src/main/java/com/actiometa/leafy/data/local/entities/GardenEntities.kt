package com.actiometa.leafy.data.local.entities

import androidx.room.*
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
    val edible: Boolean? = null,
    val propagation: String? = null,
    val pruningMonths: String? = null,
    val isPoisonous: Boolean? = null,
    val isIndoor: Boolean? = null,
    // Trefle Specific
    val family: String? = null,
    val genus: String? = null,
    val year: Int? = null,
    val author: String? = null,
    val status: String? = null,
    val rank: String? = null,
    val growthHabit: String? = null,
    val phRange: String? = null,
    val tempRange: String? = null,
    val avgHeight: String? = null,
    val lightLevel: Int? = null,
    val atmosphericHumidity: Int? = null,
    val minPrecipitation: Float? = null
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

data class PlantWithSpecies(
    @Embedded val plant: PlantEntity,
    @Relation(
        parentColumn = "speciesId",
        entityColumn = "speciesId"
    )
    val species: SpeciesEntity?
)
