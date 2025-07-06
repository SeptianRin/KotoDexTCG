package io.github.septianrin.kotodextcg

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import io.github.septianrin.kotodextcg.data.model.TcgCard
import io.github.septianrin.kotodextcg.domain.PokemonCardRepository
import io.github.septianrin.kotodextcg.ui.feature.carddetail.CardDetailViewModel
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest

@ExperimentalCoroutinesApi
class CardDetailViewModelTest : KoinTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: PokemonCardRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        startKoin {
            modules(
                module {
                    single { repository }
                }
            )
        }
    }

    @After
    fun tearDown() {
        stopKoin()
        Dispatchers.resetMain()
    }

    @Test
    fun `loads card details successfully`() = runTest(testDispatcher) {
        val cardId = "xy7-54"
        val mockCard = createFakeTcgCard(cardId, "Gyarados")
        val savedStateHandle = SavedStateHandle(mapOf("cardId" to cardId))
        coEvery { repository.getCardById(cardId) } returns Result.success(mockCard)

        val viewModel = CardDetailViewModel(repository, savedStateHandle)

        viewModel.uiState.test {
            // 1. The initial state is available immediately upon collection.
            val initialState = awaitItem()
            assertEquals(true, initialState.isLoading)

            // 2. Advance the dispatcher to allow the coroutine in the init block to run.
            advanceUntilIdle()

            // 3. The next item is the final state after the data is loaded.
            val finalState = awaitItem()
            assertEquals(false, finalState.isLoading)
            assertNotNull(finalState.tcgCard)
            assertEquals("Gyarados", finalState.tcgCard?.name)
        }
    }

    private fun createFakeTcgCard(id: String, name: String) = TcgCard(
        id = id, name = name, hp = "130", types = listOf("Water"), evolvesFrom = "Magikarp",
        images = null, rarity = "Rare", flavorText = null, attacks = null,
        weaknesses = null, resistances = null, legalities = null, count = 1
    )
}