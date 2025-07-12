package io.github.septianrin.kotodextcg.ui.feature.gacha

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.septianrin.kotodextcg.ui.state.GachaInteractionState
import io.github.septianrin.kotodextcg.ui.theme.KotoDexTCGTheme
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class GachaScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun whenPackIsSlashed_showResultsEventIsFired() {
        // Arrange
        val mockViewModel = mockk<GachaViewModel>(relaxed = true)
        val fakeState = MutableStateFlow(
            GachaUiState(
                isPreparing = false,
                interactionState = GachaInteractionState.Tearing,
                packRarity = "Rare Holo"
            )
        )
        every { mockViewModel.uiState } returns fakeState
        every { mockViewModel.handleEvent(any()) } just runs

        composeTestRule.setContent {
            KotoDexTCGTheme {
                GachaScreen(onCardClicked = {}, viewModel = mockViewModel)
            }
        }

        // Act: Find the interactive area by its testTag and perform a swipe.
        composeTestRule.onNodeWithTag("interactive_pack_area")
            .performTouchInput {
                // The gesture must start in the top half of the component.
                val startOffset = Offset(width * 0.9f, height * 0.25f)
                val endOffset = Offset(width * 0.1f, height * 0.25f)
                swipe(start = startOffset, end = endOffset, durationMillis = 200)
            }

        // The swipe triggers a state change, which starts a 600ms animation after recomposition.
        // We must manually control the flow to ensure the animation's
        // finishedListener is called before we verify the result.

        // 1. Wait for the recomposition to complete, which starts the animation.
        composeTestRule.waitForIdle()

        // 2. Now that the animation is running, advance the clock past its duration.
        composeTestRule.mainClock.advanceTimeBy(601L) // 600ms duration + 1ms buffer

        // 3. Wait for the finishedListener to be processed.
        composeTestRule.waitForIdle()


        // Assert: Verify the correct event was sent to the ViewModel.
        assertEquals(GachaInteractionState.Tearing, mockViewModel.uiState.value.interactionState)
        verify { mockViewModel.handleEvent(GachaEvent.ShowResults) }
    }
}