package com.actiometa.leafy.domain.usecase

import com.actiometa.leafy.domain.repository.SettingsRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CheckOnboardingStatusUseCaseTest {

    private val repository = mockk<SettingsRepository>()
    private val useCase = CheckOnboardingStatusUseCase(repository)

    @Test
    fun `when onboarding is not completed, should return false`() = runTest {
        // Arrange
        every { repository.isOnboardingCompleted } returns flowOf(false)

        // Act
        val result = useCase().first()

        // Assert
        assertEquals(false, result)
    }

    @Test
    fun `when onboarding is completed, should return true`() = runTest {
        // Arrange
        every { repository.isOnboardingCompleted } returns flowOf(true)

        // Act
        val result = useCase().first()

        // Assert
        assertEquals(true, result)
    }
}
