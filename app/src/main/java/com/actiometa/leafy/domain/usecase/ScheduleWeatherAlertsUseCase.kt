package com.actiometa.leafy.domain.usecase

import android.content.Context
import androidx.work.*
import com.actiometa.leafy.data.worker.WeatherWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ScheduleWeatherAlertsUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    operator fun invoke() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val weatherRequest = PeriodicWorkRequestBuilder<WeatherWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .addTag("weather_worker")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "WeatherAlertsWork",
            ExistingPeriodicWorkPolicy.KEEP,
            weatherRequest
        )
    }
}
