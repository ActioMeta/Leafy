package com.actiometa.leafy.core.di

import com.actiometa.leafy.data.datastore.SettingsRepositoryImpl
import com.actiometa.leafy.data.repository.BotanyRepositoryImpl
import com.actiometa.leafy.data.repository.GardenRepositoryImpl
import com.actiometa.leafy.data.repository.LocationRepositoryImpl
import com.actiometa.leafy.data.repository.WeatherRepositoryImpl
import com.actiometa.leafy.domain.repository.BotanyRepository
import com.actiometa.leafy.domain.repository.GardenRepository
import com.actiometa.leafy.domain.repository.LocationRepository
import com.actiometa.leafy.domain.repository.SettingsRepository
import com.actiometa.leafy.domain.repository.WeatherRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindGardenRepository(
        gardenRepositoryImpl: GardenRepositoryImpl
    ): GardenRepository

    @Binds
    @Singleton
    abstract fun bindBotanyRepository(
        botanyRepositoryImpl: BotanyRepositoryImpl
    ): BotanyRepository

    @Binds
    @Singleton
    abstract fun bindWeatherRepository(
        weatherRepositoryImpl: WeatherRepositoryImpl
    ): WeatherRepository

    @Binds
    @Singleton
    abstract fun bindLocationRepository(
        locationRepositoryImpl: LocationRepositoryImpl
    ): LocationRepository
}
