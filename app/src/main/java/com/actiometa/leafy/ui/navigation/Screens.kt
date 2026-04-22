package com.actiometa.leafy.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    object Onboarding : Screen

    @Serializable
    object Garden : Screen

    @Serializable
    data class PlantDetails(val plantId: Int) : Screen

    @Serializable
    object Scanner : Screen

    @Serializable
    object Settings : Screen
}
