package com.actiometa.leafy.ui.features.garden

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.actiometa.leafy.R
import com.actiometa.leafy.domain.usecase.GardenPlant
import com.actiometa.leafy.ui.components.shimmerEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GardenScreen(
    viewModel: GardenViewModel,
    onNavigateToScanner: () -> Unit,
    onNavigateToDetails: (Int) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.my_garden), fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings))
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToScanner,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.add_plant)) },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            GardenSkeletonList(padding)
        } else if (uiState.plants.isEmpty()) {
            EmptyGardenView(padding)
        } else {
            PlantList(
                plants = uiState.plants, 
                padding = padding,
                onWaterClick = { viewModel.waterPlant(it) },
                onPlantClick = onNavigateToDetails
            )
        }
    }
}

@Composable
fun PlantList(
    plants: List<GardenPlant>,
    padding: PaddingValues,
    onWaterClick: (Int) -> Unit,
    onPlantClick: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(plants, key = { it.plantId }) { plant ->
            PlantCard(
                plant = plant,
                onWaterClick = { onWaterClick(plant.plantId) },
                onClick = { onPlantClick(plant.plantId) }
            )
        }
    }
}

@Composable
fun PlantCard(
    plant: GardenPlant,
    onWaterClick: () -> Unit,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (plant.isNeedsWater) MaterialTheme.colorScheme.errorContainer 
                        else MaterialTheme.colorScheme.secondaryContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (plant.imagePath != null) {
                    AsyncImage(
                        model = plant.imagePath,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.LocalFlorist,
                        contentDescription = null,
                        tint = if (plant.isNeedsWater) MaterialTheme.colorScheme.onErrorContainer 
                               else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(plant.nickname, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (plant.commonName != plant.nickname) {
                    Text(plant.commonName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                val lastWateringText = if (plant.lastWatering != null) {
                    DateUtils.getRelativeTimeSpanString(
                        plant.lastWatering,
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS
                    ).toString()
                } else {
                    stringResource(R.string.needs_water)
                }
                
                Text(
                    stringResource(R.string.last_watered, lastWateringText),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (plant.isNeedsWater) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
            
            IconButton(
                onClick = onWaterClick,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = if (plant.isNeedsWater) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    Icons.Default.WaterDrop,
                    contentDescription = stringResource(R.string.watered)
                )
            }
        }
    }
}

@Composable
fun GardenSkeletonList(padding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(5) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .shimmerEffect()
            )
        }
    }
}

@Composable
fun EmptyGardenView(padding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            stringResource(R.string.no_plants_yet),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
