package com.actiometa.leafy.core.di

import android.content.Context
import androidx.room.Room
import com.actiometa.leafy.data.local.LeafyDatabase
import com.actiometa.leafy.data.local.dao.GardenDao
import com.actiometa.leafy.data.local.dao.LogDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): LeafyDatabase {
        return Room.databaseBuilder(
            context,
            LeafyDatabase::class.java,
            "leafy_db"
        ).fallbackToDestructiveMigration()
         .build()
    }

    @Provides
    fun provideGardenDao(database: LeafyDatabase): GardenDao = database.gardenDao()

    @Provides
    fun provideLogDao(database: LeafyDatabase): LogDao = database.logDao()
}
