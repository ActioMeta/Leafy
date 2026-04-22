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
    data class Scanner(val plantId: Int? = null) : Screen

    @Serializable
    object Settings : Screen

    @Serializable
    data class Gallery(val plantId: Int) : Screen
}
