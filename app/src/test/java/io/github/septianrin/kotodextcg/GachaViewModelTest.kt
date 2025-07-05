package io.github.septianrin.kotodextcg

import app.cash.turbine.test
import io.github.septianrin.kotodextcg.data.model.Card
import io.github.septianrin.kotodextcg.domain.PokemonCardRepository
import io.github.septianrin.kotodextcg.ui.viewmodel.GachaViewModel
import io.github.septianrin.kotodextcg.ui.viewmodel.GachaUiState
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
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
import org.koin.test.get

@ExperimentalCoroutinesApi
class GachaViewModelTest : KoinTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: PokemonCardRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        startKoin {
            modules(
                module {
                    single { repository }
                    // Use factory to create a new instance each time we call get()
                    factory { GachaViewModel(get()) }
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
    fun `prepareNewPack success - state transitions correctly and pack is generated`() = runTest {
        // Arrange
        val commonCards = (1..5).map { Card("c$it", "Common", null, null, null, "Common") }
        val uncommonCards = (1..2).map { Card("u$it", "Uncommon", null, null, null, "Uncommon") }
        val rareCards = listOf(Card("r1", "Charizard", null, null, null, "Rare Holo V"))

        coEvery { repository.getCards(any(), "rarity:Common") } returns Result.success(commonCards)
        coEvery { repository.getCards(any(), "rarity:Uncommon") } returns Result.success(uncommonCards)
        coEvery { repository.getCards(any(), coMatch { it.contains("rarity:") && !it.contains("Common") && !it.contains("Uncommon") }) } returns Result.success(rareCards)

        // Act: Get a fresh ViewModel instance AFTER mocks are configured.
        val viewModel = get<GachaViewModel>()

        // Assert
        viewModel.uiState.test {
            // 1. Await the initial state, which should be 'isPreparing = true'
            assertEquals(GachaUiState(isPreparing = true), awaitItem())

            // 2. Await the final state after the coroutine finishes
            val finalState = awaitItem()

            // 3. Assert the properties of the final loaded state
            assertEquals(false, finalState.isPreparing)
            assertNull(finalState.error)
            assertEquals(8, finalState.pulledCards.size)
            assertEquals("Rare Holo V", finalState.packRarity)
        }
    }

    @Test
    fun `prepareNewPack failure - state shows error if a card type fails to load`() = runTest {
        // Arrange
        val exception = Exception("Network Error")
        coEvery { repository.getCards(any(), "rarity:Common") } returns Result.failure(exception)
        coEvery { repository.getCards(any(), "rarity:Uncommon") } returns Result.success((1..2).map { Card("u$it", "Uncommon", null, null, null, "Uncommon") })
        coEvery { repository.getCards(any(), coMatch { it.contains("rarity:") && !it.contains("Common") && !it.contains("Uncommon") }) } returns Result.success(listOf(Card("r1", "Charizard", null, null, null, "Rare Holo V")))

        // Act: Get a fresh ViewModel instance AFTER mocks are configured.
        val viewModel = get<GachaViewModel>()

        // Assert
        viewModel.uiState.test {
            // 1. Await the initial state, which should be 'isPreparing = true'
            assertEquals(GachaUiState(isPreparing = true), awaitItem())

            // 2. Await the final state after the coroutine fails
            val finalState = awaitItem()

            // 3. Assert the properties of the final error state
            assertEquals(false, finalState.isPreparing)
            assertNotNull(finalState.error)
            assertEquals("Failed to construct a full booster pack.", finalState.error)
        }
    }
}