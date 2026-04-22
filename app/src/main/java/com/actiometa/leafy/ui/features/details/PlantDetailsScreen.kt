package com.actiometa.leafy.ui.features.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.actiometa.leafy.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailsScreen(
    viewModel: PlantDetailsViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        val plant = uiState.plant

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (plant != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // Header Image
                Box(modifier = Modifier.height(350.dp).fillMaxWidth()) {
                    AsyncImage(
                        model = plant.imagePath,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, MaterialTheme.colorScheme.background),
                                    startY = 500f
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(24.dp)
                    ) {
                        Text(
                            text = plant.nickname,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = plant.scientificName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }

                // Info Content
                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 32.dp)
                ) {
                    // Quick Stats Chips
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        plant.cycle?.let { AttributeChip(it, Icons.Default.History) }
                        plant.maintenance?.let { AttributeChip(it, Icons.Default.Build) }
                        if (plant.isIndoor) AttributeChip(stringResource(R.string.indoor_label), Icons.Default.Home)
                        if (plant.edible == true) AttributeChip("Edible", Icons.Default.Restaurant)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Care Summary Card
                    CareStatusCard(uiState)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Description Section
                    if (!plant.description.isNullOrBlank()) {
                        SectionHeader(stringResource(R.string.plant_info))
                        Text(
                            text = plant.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Justify,
                            lineHeight = 22.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Requirements Grid
                    SectionHeader("Requirements")
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            InfoRow(Icons.Default.WaterDrop, stringResource(R.string.watering_label), stringResource(R.string.watering_every_days, plant.wateringFrequencyDays))
                            InfoRow(Icons.Default.WbSunny, stringResource(R.string.sunlight_label), plant.sunlight)
                            plant.growthRate?.let { InfoRow(Icons.Default.Speed, stringResource(R.string.growth_rate_label), it) }
                            plant.cycle?.let { InfoRow(Icons.Default.Autorenew, stringResource(R.string.cycle_label), it) }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Specific Care (Propagation & Pruning)
                    if (!plant.propagation.isNullOrBlank() || !plant.pruningMonths.isNullOrBlank()) {
                        SectionHeader("Care Specifics")
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (!plant.propagation.isNullOrBlank()) {
                                CareDetailCard(Modifier.weight(1f), stringResource(R.string.propagation_label), plant.propagation, Icons.Default.Category)
                            }
                            if (!plant.pruningMonths.isNullOrBlank()) {
                                CareDetailCard(Modifier.weight(1f), stringResource(R.string.pruning_label), plant.pruningMonths, Icons.Default.ContentCut)
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Safety Section
                    SectionHeader(stringResource(R.string.poisonous_label))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SafetyCard(
                            Modifier.weight(1f), 
                            stringResource(R.string.pets_label), 
                            plant.isPoisonousToPets, 
                            Icons.Default.Pets
                        )
                        SafetyCard(
                            Modifier.weight(1f), 
                            stringResource(R.string.humans_label), 
                            plant.isPoisonousToHumans, 
                            Icons.Default.Person
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { viewModel.waterPlant() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (plant.isNeedsWater) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                        ),
                        enabled = plant.isNeedsWater
                    ) {
                        Icon(Icons.Default.WaterDrop, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (plant.isNeedsWater) stringResource(R.string.register_watering_btn) 
                            else stringResource(R.string.already_watered)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
fun CareDetailCard(modifier: Modifier, label: String, value: String, icon: ImageVector) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
            Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SafetyCard(modifier: Modifier, label: String, isPoisonous: Boolean, icon: ImageVector) {
    val color = if (isPoisonous) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, modifier = Modifier.size(24.dp), tint = color)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelSmall)
                Text(
                    if (isPoisonous) stringResource(R.string.yes) else stringResource(R.string.no),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}

@Composable
fun AttributeChip(text: String, icon: ImageVector) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.width(4.dp))
            Text(text, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CareStatusCard(uiState: PlantDetailsUiState) {
    val weather = uiState.weather
    val plant = uiState.plant ?: return

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (plant.isNeedsWater) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f) 
                            else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(if (plant.isNeedsWater) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (plant.isNeedsWater) Icons.Default.WaterDrop else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                val statusTitle = if (plant.isNeedsWater) stringResource(R.string.status_needs_water) else stringResource(R.string.status_hydrated)
                val statusDesc = if (plant.isNeedsWater) stringResource(R.string.desc_needs_water) else stringResource(R.string.desc_hydrated)
                
                Text(
                    text = if (weather?.shouldPauseWatering == true) stringResource(R.string.rain_predicted) else statusTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (weather?.shouldPauseWatering == true) stringResource(R.string.watering_paused) else statusDesc,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}
