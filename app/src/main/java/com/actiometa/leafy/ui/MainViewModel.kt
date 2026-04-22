package com.actiometa.leafy.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.actiometa.leafy.domain.usecase.CheckOnboardingStatusUseCase
import com.actiometa.leafy.domain.usecase.ScheduleWeatherAlertsUseCase
import com.actiometa.leafy.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val checkOnboardingStatusUseCase: CheckOnboardingStatusUseCase,
    private val scheduleWeatherAlertsUseCase: ScheduleWeatherAlertsUseCase
) : ViewModel() {

    private val _startDestination = MutableStateFlow<Screen?>(null)
    val startDestination: StateFlow<Screen?> = _startDestination.asStateFlow()

    init {
        viewModelScope.launch {
            checkOnboardingStatusUseCase().collect { isCompleted ->
                _startDestination.value = if (isCompleted) {
                    scheduleWeatherAlertsUseCase()
                    Screen.Garden
                } else {
                    Screen.Onboarding
                }
            }
        }
    }
}
