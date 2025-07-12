package io.github.septianrin.kotodextcg.ui.feature.cardlist

import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.septianrin.kotodextcg.data.model.TcgCard
import io.github.septianrin.kotodextcg.ui.theme.KotoDexTCGTheme
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CardListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun whenCardIsClicked_onCardClickedIsCalled() {
        // Arrange
        val mockViewModel = mockk<CardListViewModel>(relaxed = true)
        val testCard = TcgCard("xy7-54", "Gyarados", "130", listOf("Water"), null, null, "Rare", null, null, null, null, null, 1)
        val fakeState = MutableStateFlow(CardListUiState(tcgCards = listOf(testCard), isLoading = false))
        every { mockViewModel.uiState } returns fakeState

        val onCardClicked: (String) -> Unit = mockk(relaxed = true)

        // Act
        composeTestRule.setContent {
            KotoDexTCGTheme {
                CardListScreen(
                    listState = rememberLazyGridState(),
                    onCardClicked = onCardClicked,
                    viewModel = mockViewModel
                )
            }
        }

        // Find the card by its content description and click it
        composeTestRule.onNodeWithContentDescription("Gyarados").performClick()

        // Assert
        // Verify that the callback was triggered with the correct card ID
        verify { onCardClicked("xy7-54") }
    }
}