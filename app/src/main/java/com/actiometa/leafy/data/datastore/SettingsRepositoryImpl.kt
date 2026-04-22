package com.actiometa.leafy.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.actiometa.leafy.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepositoryImpl @Inject constructor(
    private val context: Context
) : SettingsRepository {

    private object PreferencesKeys {
        val PLANT_NET_KEY = stringPreferencesKey("plant_net_key")
        val PERENUAL_KEY = stringPreferencesKey("perenual_key")
        val OPEN_WEATHER_KEY = stringPreferencesKey("open_weather_key")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val WEATHER_ALERTS_ENABLED = booleanPreferencesKey("weather_alerts_enabled")
        val USER_CITY_LOCATION = stringPreferencesKey("user_city_location")
        val APP_LANGUAGE = stringPreferencesKey("app_language")
    }

    override val plantNetApiKey: Flow<String?> = context.dataStore.data.map { it[PreferencesKeys.PLANT_NET_KEY] }
    override val perenualApiKey: Flow<String?> = context.dataStore.data.map { it[PreferencesKeys.PERENUAL_KEY] }
    override val openWeatherApiKey: Flow<String?> = context.dataStore.data.map { it[PreferencesKeys.OPEN_WEATHER_KEY] }
    override val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { it[PreferencesKeys.ONBOARDING_COMPLETED] ?: false }
    override val isWeatherAlertsEnabled: Flow<Boolean> = context.dataStore.data.map { it[PreferencesKeys.WEATHER_ALERTS_ENABLED] ?: false }
    override val userCityLocation: Flow<String?> = context.dataStore.data.map { it[PreferencesKeys.USER_CITY_LOCATION] }
    override val appLanguage: Flow<String> = context.dataStore.data.map { 
        it[PreferencesKeys.APP_LANGUAGE] ?: java.util.Locale.getDefault().language 
    }

    override suspend fun savePlantNetKey(key: String) {
        context.dataStore.edit { it[PreferencesKeys.PLANT_NET_KEY] = key }
    }

    override suspend fun savePerenualKey(key: String) {
        context.dataStore.edit { it[PreferencesKeys.PERENUAL_KEY] = key }
    }

    override suspend fun saveOpenWeatherKey(key: String) {
        context.dataStore.edit { it[PreferencesKeys.OPEN_WEATHER_KEY] = key }
    }

    override suspend fun completeOnboarding() {
        context.dataStore.edit { it[PreferencesKeys.ONBOARDING_COMPLETED] = true }
    }

    override suspend fun setWeatherPreferences(enabled: Boolean, city: String?) {
        context.dataStore.edit {
            it[PreferencesKeys.WEATHER_ALERTS_ENABLED] = enabled
            if (city != null) {
                it[PreferencesKeys.USER_CITY_LOCATION] = city
            } else {
                it.remove(PreferencesKeys.USER_CITY_LOCATION)
            }
        }
    }

    override suspend fun saveLanguage(languageCode: String) {
        context.dataStore.edit { it[PreferencesKeys.APP_LANGUAGE] = languageCode }
    }
}
