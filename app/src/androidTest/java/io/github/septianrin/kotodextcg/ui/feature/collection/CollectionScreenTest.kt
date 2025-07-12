package io.github.septianrin.kotodextcg.ui.feature.collection

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
class CollectionScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun whenClearButtonIsClickedAndConfirmed_clearCollectionEventIsFired() {
        // Arrange
        val mockViewModel = mockk<CollectionViewModel>(relaxed = true)
        val testCards = listOf(
            TcgCard("1", "Charmander", "50", listOf("Fire"), null, null, "Common", null, null, null, null, null, 1)
        )
        val fakeState = MutableStateFlow(testCards)
        every { mockViewModel.collectionState } returns fakeState

        composeTestRule.setContent {
            KotoDexTCGTheme {
                CollectionScreen(onCardClicked = {}, onGoToGachaClicked = {}, viewModel = mockViewModel)
            }
        }

        // Act
        // 1. Click the "Clear Collection" button to show the dialog
        composeTestRule.onNodeWithText("Clear Collection").performClick()

        // 2. Click the "Clear" button on the confirmation dialog
        composeTestRule.onNodeWithText("Clear").performClick()

        // Assert
        // 3. Verify that the ViewModel's event handler was called
        verify { mockViewModel.handleEvent(CollectionEvent.ClearCollection) }
    }
}