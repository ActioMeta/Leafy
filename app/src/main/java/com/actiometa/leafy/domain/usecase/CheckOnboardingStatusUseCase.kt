package com.actiometa.leafy.domain.usecase

import com.actiometa.leafy.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

import javax.inject.Inject

class CheckOnboardingStatusUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    operator fun invoke(): Flow<Boolean> = repository.isOnboardingCompleted
}
