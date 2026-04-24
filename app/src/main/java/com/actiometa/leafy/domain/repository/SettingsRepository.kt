package com.actiometa.leafy.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
  val plantNetApiKey: Flow<String?>
  val perenualApiKey: Flow<String?>
  val openWeatherApiKey: Flow<String?>
  val isOnboardingCompleted: Flow<Boolean>

  // Weather & Privacy Preferences
  val isWeatherAlertsEnabled: Flow<Boolean>
  val userCityLocation: Flow<String?> // e.g., "Duran, EC"
  val appLanguage: Flow<String> // e.g., "en", "es"

  suspend fun savePlantNetKey(key: String)
  suspend fun savePerenualKey(key: String)
  suspend fun saveOpenWeatherKey(key: String)
  suspend fun completeOnboarding()
  suspend fun setWeatherPreferences(enabled: Boolean, city: String?)
  suspend fun saveLanguage(languageCode: String)
}
