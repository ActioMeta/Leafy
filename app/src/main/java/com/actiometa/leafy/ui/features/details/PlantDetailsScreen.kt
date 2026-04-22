package com.actiometa.leafy.ui.features.details

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.actiometa.leafy.R
import com.actiometa.leafy.domain.usecase.GardenPlant

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@Composable
fun PlantDetailsScreen(
    viewModel: PlantDetailsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToGallery: (Int) -> Unit,
    onNavigateToMonitor: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    Text(
                        text = uiState.error ?: stringResource(R.string.error_occurred),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                uiState.plant != null -> {
                    val plant = uiState.plant!!
                    
                    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
                        // Header Header
                        Box(modifier = Modifier.fillMaxWidth().height(360.dp)) {
                            AsyncImage(
                                model = plant.imagePath,
                                contentDescription = plant.commonName,
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)),
                                contentScale = ContentScale.Crop
                            )
                            
                            Box(modifier = Modifier.fillMaxSize().background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                                    startY = 500f
                                )
                            ))

                            // Buttons Overlay
                            Row(
                                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FloatingActionButton(
                                    onClick = onNavigateBack,
                                    containerColor = Color.Black.copy(alpha = 0.4f),
                                    contentColor = Color.White,
                                    shape = CircleShape,
                                    modifier = Modifier.size(48.dp),
                                    elevation = FloatingActionButtonDefaults.elevation(0.dp)
                                ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }

                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    FloatingActionButton(
                                        onClick = { onNavigateToMonitor(plant.plantId) },
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.size(48.dp)
                                    ) { Icon(Icons.Default.CameraAlt, "Monitor") }
                                    
                                    FloatingActionButton(
                                        onClick = { onNavigateToGallery(plant.plantId) },
                                        containerColor = Color.Black.copy(alpha = 0.4f),
                                        contentColor = Color.White,
                                        shape = CircleShape,
                                        modifier = Modifier.size(48.dp),
                                        elevation = FloatingActionButtonDefaults.elevation(0.dp)
                                    ) { Icon(Icons.Default.PhotoLibrary, "Gallery") }
                                }
                            }

                            // Title Overlay
                            Column(modifier = Modifier.align(Alignment.BottomStart).padding(24.dp)) {
                                Text(text = plant.commonName, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                Text(text = "${plant.scientificName} ${plant.author ?: ""}", style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.8f), fontStyle = FontStyle.Italic)
                            }
                        }

                        // Content
                        Column(modifier = Modifier.padding(20.dp)) {
                            // Botanical Specs
                            BotanicalSpecsCard(plant)

                            Spacer(Modifier.height(16.dp))

                            // Environmental Needs
                            EnvironmentalLimitsCard(plant)

                            Spacer(Modifier.height(16.dp))

                            // Safety Section (Merged Toxic)
                            SafetyCard(plant)

                            Spacer(Modifier.height(32.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BotanicalSpecsCard(plant: GardenPlant) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Detailed Specifications", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            HorizontalDivider(Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
            
            Row(Modifier.fillMaxWidth()) {
                SmallInfoColumn("Family", plant.family ?: "N/A", Modifier.weight(1f))
                SmallInfoColumn("Genus", plant.genus ?: "N/A", Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth()) {
                SmallInfoColumn("Habit", plant.growthHabit ?: "N/A", Modifier.weight(1f))
                SmallInfoColumn("Growth", plant.growthRate ?: "N/A", Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth()) {
                SmallInfoColumn("Rank", plant.rank?.uppercase() ?: "N/A", Modifier.weight(1f))
                SmallInfoColumn("Year", plant.year?.toString() ?: "N/A", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun EnvironmentalLimitsCard(plant: GardenPlant) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Growth Environment", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            HorizontalDivider(Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
            
            MetricRow("Soil pH Range", plant.phRange ?: "N/A", Icons.Default.Science)
            MetricRow("Temp. Range", plant.tempRange ?: "N/A", Icons.Default.Thermostat)
            MetricRow("Atm. Humidity", plant.atmosphericHumidity?.let { "$it/10" } ?: "N/A", Icons.Default.WaterDrop)
            MetricRow("Light Intensity", plant.lightLevel?.let { "$it/10" } ?: "N/A", Icons.Default.WbSunny)
            MetricRow("Min. Precipitation", plant.minPrecipitation?.let { "${it.toInt()} mm/y" } ?: "N/A", Icons.Default.CloudQueue)
            MetricRow("Avg. Height", plant.avgHeight ?: "N/A", Icons.Default.Height)
        }
    }
}

@Composable
fun SafetyCard(plant: GardenPlant) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SafetyIndicator("Edible", plant.edible, Icons.Default.Restaurant)
            SafetyIndicator("Toxic", plant.isPoisonous, Icons.Default.Warning)
        }
    }
}

@Composable
fun SmallInfoColumn(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun MetricRow(label: String, value: String, icon: ImageVector) {
    Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SafetyIndicator(label: String, isTrue: Boolean?, icon: ImageVector) {
    val tint = when (isTrue) {
        true -> if (label == "Toxic") Color(0xFFF44336) else Color(0xFF4CAF50)
        false -> if (label == "Toxic") Color(0xFF4CAF50) else Color(0xFFF44336)
        null -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    val textValue = when (isTrue) {
        true -> "Yes"
        false -> "No"
        null -> "N/A"
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = tint)
        Text(label, style = MaterialTheme.typography.labelSmall)
        Text(textValue, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = tint)
    }
}

@Composable
fun InfoSectionCard(title: String, items: List<InfoItem>) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            items.forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                    Icon(item.icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Text(item.label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    Text(item.value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

data class InfoItem(val label: String, val value: String, val icon: ImageVector)
