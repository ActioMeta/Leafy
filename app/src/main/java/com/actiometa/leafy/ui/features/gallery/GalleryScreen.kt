package com.actiometa.leafy.ui.features.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.actiometa.leafy.R
import com.actiometa.leafy.data.local.entities.PlantImageEntity
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    viewModel: GalleryViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header without Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(Modifier.width(16.dp))
                Text(
                    stringResource(R.string.plant_gallery),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.images.isEmpty()) {
                EmptyGalleryView()
            } else {
                // Comparison Section
                if (uiState.latestImage != null && uiState.comparisonImage != null) {
                    ComparisonView(
                        latest = uiState.latestImage!!,
                        past = uiState.comparisonImage!!
                    )
                } else if (uiState.latestImage != null) {
                    // Solo una imagen
                    LatestOnlyView(uiState.latestImage!!)
                }

                Spacer(Modifier.height(16.dp))

                // Past Image Selector (Slider Navigation)
                Text(
                    text = "Select past photo to compare:",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(uiState.images) { index, image ->
                        val isSelected = index == uiState.selectedPastIndex
                        val isLatest = index == 0
                        
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.selectComparisonImage(index) }
                        ) {
                            AsyncImage(
                                model = image.imagePath,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            if (isLatest) {
                                Badge(
                                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp),
                                    containerColor = MaterialTheme.colorScheme.tertiary
                                ) { Text("Latest") }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ComparisonView(
    latest: PlantImageEntity,
    past: PlantImageEntity
) {
    var sliderPosition by remember { mutableFloatStateOf(0.5f) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Column(modifier = Modifier.padding(16.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            // Latest (Current) - Foreground
            AsyncImage(
                model = latest.imagePath,
                contentDescription = "Current",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Past (Selected) - Background (clipped)
            Box(
                modifier = Modifier
                    .fillMaxWidth(sliderPosition)
                    .fillMaxHeight()
            ) {
                AsyncImage(
                    model = past.imagePath,
                    contentDescription = "Past",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Divider Line
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .align(Alignment.CenterStart)
                    // We use weight and fillMaxWidth for the clipped box, 
                    // so we position the divider based on screen percentage
                    .fillMaxWidth(sliderPosition)
                    .padding(start = (0.dp)) // This puts the edge right at the limit
            ) {
                Box(modifier = Modifier.fillMaxHeight().width(2.dp).background(Color.White).align(Alignment.CenterEnd))
            }
            
            // Labels
            Surface(
                modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
                color = Color.Black.copy(alpha = 0.5f),
                shape = CircleShape
            ) {
                Text(
                    text = "Past: ${dateFormat.format(Date(past.timestamp))}",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            
            Surface(
                modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                shape = CircleShape
            ) {
                Text(
                    text = "Latest: ${dateFormat.format(Date(latest.timestamp))}",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Slider(
            value = sliderPosition,
            onValueChange = { sliderPosition = it },
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun LatestOnlyView(image: PlantImageEntity) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Latest Photo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        AsyncImage(
            model = image.imagePath,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().height(300.dp).clip(RoundedCornerShape(24.dp)),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun EmptyGalleryView() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(stringResource(R.string.no_images_yet), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
