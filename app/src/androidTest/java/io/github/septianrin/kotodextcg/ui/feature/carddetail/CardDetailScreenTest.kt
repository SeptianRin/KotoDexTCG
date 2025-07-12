package io.github.septianrin.kotodextcg.ui.feature.carddetail

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.septianrin.kotodextcg.data.model.TcgCard
import io.github.septianrin.kotodextcg.ui.theme.KotoDexTCGTheme
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CardDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun whenCardImageIsClicked_zoomOverlayIsDisplayed() {
        // Arrange
        val mockViewModel = mockk<CardDetailViewModel>(relaxed = true)
        val testCard = TcgCard("1", "Gyarados", "130", listOf("Water"), null, null, "Rare Holo", null, null, null, null, null, 1)
        val fakeState = MutableStateFlow(CardDetailUiState(tcgCard = testCard, isLoading = false))
        every { mockViewModel.uiState } returns fakeState

        composeTestRule.setContent {
            KotoDexTCGTheme {
                CardDetailScreen(viewModel = mockViewModel)
            }
        }

        // Act: Click the card image (we find it by its content description)
        composeTestRule.onNodeWithContentDescription("Gyarados").performClick()

        // Assert: Verify that the zoomable overlay is now displayed
        composeTestRule.onNodeWithContentDescription("Gyarados detail view").assertIsDisplayed()
    }
}