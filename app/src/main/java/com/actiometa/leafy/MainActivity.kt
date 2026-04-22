package com.actiometa.leafy

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.actiometa.leafy.ui.MainViewModel
import com.actiometa.leafy.ui.features.garden.GardenScreen
import com.actiometa.leafy.ui.features.garden.GardenViewModel
import com.actiometa.leafy.ui.features.onboarding.OnboardingScreen
import com.actiometa.leafy.ui.features.onboarding.OnboardingViewModel
import com.actiometa.leafy.ui.features.scanner.ScannerScreen
import com.actiometa.leafy.ui.features.scanner.ScannerViewModel
import com.actiometa.leafy.ui.features.details.PlantDetailsScreen
import com.actiometa.leafy.ui.features.details.PlantDetailsViewModel
import com.actiometa.leafy.ui.features.gallery.GalleryScreen
import com.actiometa.leafy.ui.features.gallery.GalleryViewModel
import com.actiometa.leafy.ui.features.settings.SettingsScreen
import com.actiometa.leafy.ui.features.settings.SettingsViewModel
import com.actiometa.leafy.ui.navigation.Screen
import com.actiometa.leafy.ui.theme.LeafyTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val mainViewModel: MainViewModel = hiltViewModel()
            val language by mainViewModel.appLanguage.collectAsState()
            
            // Apply language to local configuration
            updateLocale(language)

            LeafyTheme {
                val navController = rememberNavController()
                val startDestination by mainViewModel.startDestination.collectAsState()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    startDestination?.let { destination ->
                        NavHost(
                            navController = navController,
                            startDestination = destination
                        ) {
                            composable<Screen.Onboarding> {
                                val onboardingViewModel: OnboardingViewModel = hiltViewModel()
                                OnboardingScreen(
                                    viewModel = onboardingViewModel,
                                    onNavigateToGarden = {
                                        navController.navigate(Screen.Garden) {
                                            popUpTo(Screen.Onboarding) { inclusive = true }
                                        }
                                    }
                                )
                            }

                            composable<Screen.Garden> {
                                val gardenViewModel: GardenViewModel = hiltViewModel()
                                GardenScreen(
                                    viewModel = gardenViewModel,
                                    onNavigateToScanner = { navController.navigate(Screen.Scanner()) },
                                    onNavigateToDetails = { plantId ->
                                        navController.navigate(Screen.PlantDetails(plantId))
                                    },
                                    onNavigateToSettings = { navController.navigate(Screen.Settings) }
                                )
                            }

                            composable<Screen.PlantDetails> {
                                val detailsViewModel: PlantDetailsViewModel = hiltViewModel()
                                PlantDetailsScreen(
                                    viewModel = detailsViewModel,
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToGallery = { plantId ->
                                        navController.navigate(Screen.Gallery(plantId))
                                    },
                                    onNavigateToMonitor = { plantId ->
                                        navController.navigate(Screen.Scanner(plantId))
                                    }
                                )
                            }

                            composable<Screen.Gallery> {
                                val galleryViewModel: GalleryViewModel = hiltViewModel()
                                GalleryScreen(
                                    viewModel = galleryViewModel,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }

                            composable<Screen.Scanner> {
                                val scannerViewModel: ScannerViewModel = hiltViewModel()
                                ScannerScreen(
                                    viewModel = scannerViewModel,
                                    onNavigateBack = {
                                        navController.popBackStack()
                                    }
                                )
                            }

                            composable<Screen.Settings> {
                                val settingsViewModel: SettingsViewModel = hiltViewModel()
                                SettingsScreen(
                                    viewModel = settingsViewModel,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}
