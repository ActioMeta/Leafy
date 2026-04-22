package com.actiometa.leafy.ui.features.scanner

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.actiometa.leafy.R
import com.actiometa.leafy.core.util.FileUtils
import com.actiometa.leafy.domain.model.PlantDetails
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun ScannerScreen(
    viewModel: ScannerViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val selectedOrgan by viewModel.selectedOrgan.collectAsState()
    
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                val file = FileUtils.uriToFile(context, it)
                if (file != null) viewModel.identifyPlant(file)
            }
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is ScannerUiState.Added) {
            onNavigateBack()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (hasCameraPermission) {
                when (val state = uiState) {
                    is ScannerUiState.Idle -> {
                        CameraView(
                            selectedOrgan = selectedOrgan,
                            onOrganSelected = { viewModel.setOrgan(it) },
                            onImageCaptured = { file -> 
                                val permanentPath = FileUtils.saveImageToInternalStorage(context, file)
                                viewModel.identifyPlant(File(permanentPath)) 
                            },
                            onGalleryClick = { galleryLauncher.launch("image/*") },
                            onError = { /* Handle camera error */ }
                        )
                    }
                    is ScannerUiState.Scanning -> {
                        // ... (sin cambios)
                    }
                    is ScannerUiState.Success -> {
                        ResultView(
                            scientificName = state.identification.scientificName,
                            commonName = state.details.commonName,
                            imagePath = state.details.imagePath,
                            onAdd = { nickname ->
                                viewModel.addToGarden(nickname, state.details)
                            },
                            onCancel = { viewModel.reset() }
                        )
                    }
                    is ScannerUiState.Added -> {
                        // Just a placeholder while navigating back
                    }
                    is ScannerUiState.Error -> {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.ErrorOutline, null, Modifier.size(64.dp), MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(16.dp))
                            Text(state.message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(24.dp))
                            Button(onClick = { viewModel.reset() }) { Text(stringResource(R.string.try_again)) }
                        }
                    }
                }
            } else {
                PermissionDeniedView { launcher.launch(Manifest.permission.CAMERA) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraView(
    selectedOrgan: String,
    onOrganSelected: (String) -> Unit,
    onImageCaptured: (File) -> Unit,
    onGalleryClick: () -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    val imageCapture: ImageCapture = remember { ImageCapture.Builder().build() }
    val previewView: PreviewView = remember { PreviewView(context) }
    var flashMode by remember { mutableIntStateOf(ImageCapture.FLASH_MODE_OFF) }
    
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    var zoomState by remember { mutableStateOf<ZoomState?>(null) }

    LaunchedEffect(flashMode) {
        imageCapture.flashMode = flashMode
    }

    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }
            try {
                cameraProvider.unbindAll()
                val camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture
                )
                cameraControl = camera.cameraControl
                camera.cameraInfo.zoomState.observe(lifecycleOwner) { zoomState = it }
            } catch (e: Exception) { }
        }, ContextCompat.getMainExecutor(context))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                detectTransformGestures { _, _, zoom, _ ->
                    zoomState?.let { state ->
                        val newZoom = (state.linearZoom + (zoom - 1) * 0.1f).coerceIn(0f, 1f)
                        cameraControl?.setLinearZoom(newZoom)
                    }
                }
            }
        )

        ScannerOverlay()

        // Organ Selector (Top)
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val organLabel = when(selectedOrgan) {
                "leaf" -> stringResource(R.string.organ_leaf)
                "flower" -> stringResource(R.string.organ_flower)
                "fruit" -> stringResource(R.string.organ_fruit)
                "bark" -> stringResource(R.string.organ_bark)
                else -> selectedOrgan
            }
            
            Text(
                stringResource(R.string.part_to_scan, organLabel),
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(24.dp)).padding(4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                val organs = listOf("leaf", "flower", "fruit", "bark")
                organs.forEach { organ ->
                    val isSelected = selectedOrgan == organ
                    val label = when(organ) {
                        "leaf" -> stringResource(R.string.organ_leaf)
                        "flower" -> stringResource(R.string.organ_flower)
                        "fruit" -> stringResource(R.string.organ_fruit)
                        "bark" -> stringResource(R.string.organ_bark)
                        else -> organ
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { onOrganSelected(organ) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            label,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.White,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }

        // Zoom Slider & Controls (Bottom)
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Zoom Slider
            zoomState?.let { state ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 48.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Remove, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Slider(
                        value = state.linearZoom,
                        onValueChange = { cameraControl?.setLinearZoom(it) },
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        )
                    )
                    Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
                Text(
                    String.format("%.1fx", state.zoomRatio),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { flashMode = if (flashMode == ImageCapture.FLASH_MODE_OFF) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF },
                    modifier = Modifier.size(48.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        if (flashMode == ImageCapture.FLASH_MODE_ON) Icons.Default.FlashOn else Icons.Default.FlashOff, 
                        stringResource(if (flashMode == ImageCapture.FLASH_MODE_ON) R.string.flash_on else R.string.flash_off), 
                        tint = Color.White
                    )
                }

                // Capture Button
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .border(4.dp, Color.White, CircleShape)
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable {
                            val file = FileUtils.createTempImageFile(context)
                            val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
                            imageCapture.takePicture(
                                outputOptions, cameraExecutor,
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(results: ImageCapture.OutputFileResults) { onImageCaptured(file) }
                                    override fun onError(exc: ImageCaptureException) { onError(exc) }
                                }
                            )
                        }
                )

                IconButton(
                    onClick = onGalleryClick,
                    modifier = Modifier.size(48.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.PhotoLibrary, stringResource(R.string.select_from_gallery), tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun ScannerOverlay() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val boxSize = width * 0.7f
        val left = (width - boxSize) / 2
        val top = (height - boxSize) / 2
        val cornerSize = 40.dp.toPx()
        val strokeWidth = 4.dp.toPx()
        val color = Color.White

        drawRect(color = Color.Black.copy(alpha = 0.3f))

        // Top Left
        drawPath(Path().apply {
            moveTo(left, top + cornerSize); lineTo(left, top + 16.dp.toPx())
            quadraticBezierTo(left, top, left + 16.dp.toPx(), top); lineTo(left + cornerSize, top)
        }, color, style = Stroke(strokeWidth))
        // Top Right
        drawPath(Path().apply {
            moveTo(left + boxSize - cornerSize, top); lineTo(left + boxSize - 16.dp.toPx(), top)
            quadraticBezierTo(left + boxSize, top, left + boxSize, top + 16.dp.toPx()); lineTo(left + boxSize, top + cornerSize)
        }, color, style = Stroke(strokeWidth))
        // Bottom Left
        drawPath(Path().apply {
            moveTo(left, top + boxSize - cornerSize); lineTo(left, top + boxSize - 16.dp.toPx())
            quadraticBezierTo(left, top + boxSize, left + 16.dp.toPx(), top + boxSize); lineTo(left + cornerSize, top + boxSize)
        }, color, style = Stroke(strokeWidth))
        // Bottom Right
        drawPath(Path().apply {
            moveTo(left + boxSize - cornerSize, top + boxSize); lineTo(left + boxSize - 16.dp.toPx(), top + boxSize)
            quadraticBezierTo(left + boxSize, top + boxSize, left + boxSize, top + boxSize - 16.dp.toPx()); lineTo(left + boxSize, top + boxSize - cornerSize)
        }, color, style = Stroke(strokeWidth))
    }
}

@Composable
fun ResultView(
    scientificName: String,
    commonName: String,
    imagePath: String?,
    onAdd: (String) -> Unit,
    onCancel: () -> Unit
) {
    var nickname by remember { mutableStateOf(commonName) }
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Card(
            modifier = Modifier.padding(24.dp).align(Alignment.Center),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                    AsyncImage(model = imagePath, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                }
                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.identification_success), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(commonName, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
                Text(stringResource(R.string.scientific_name, scientificName), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                Spacer(Modifier.height(24.dp))
                OutlinedTextField(value = nickname, onValueChange = { nickname = it }, label = { Text(stringResource(R.string.plant_nickname_label)) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
                Spacer(Modifier.height(24.dp))
                Button(onClick = { onAdd(nickname) }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp)) { Text(stringResource(R.string.add_to_garden_btn)) }
                TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.try_again)) }
            }
        }
    }
}

@Composable
fun PermissionDeniedView(onGrant: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(80.dp))
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.camera_permission_denied), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onGrant) { Text(stringResource(R.string.request_permission)) }
    }
}
