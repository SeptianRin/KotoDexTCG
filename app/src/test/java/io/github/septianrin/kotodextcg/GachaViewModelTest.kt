package io.github.septianrin.kotodextcg.ui.feature.gacha

import app.cash.turbine.test
import io.github.septianrin.kotodextcg.data.model.TcgCard
import io.github.septianrin.kotodextcg.domain.PokemonCardRepository
import io.github.septianrin.kotodextcg.ui.state.GachaInteractionState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
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
import org.koin.test.get

@ExperimentalCoroutinesApi
class GachaViewModelTest : KoinTest {

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
    fun `prepareNewPack success - state transitions correctly and pack is generated`() = runTest(testDispatcher) {
        // Arrange
        val commonCards = (1..5).map { createFakeTcgCard("c$it", "Common") }
        val uncommonCards = (1..2).map { createFakeTcgCard("u$it", "Uncommon") }
        val rareCards = listOf(createFakeTcgCard("r1", "Charizard", "Rare Holo V"))

        coEvery { repository.getCards(any(), "rarity:Common") } returns Result.success(commonCards)
        coEvery { repository.getCards(any(), "rarity:Uncommon") } returns Result.success(uncommonCards)
        coEvery { repository.getCards(any(), coMatch { it.contains("Rare") }) } returns Result.success(rareCards)

        // Act
        val viewModel = get<GachaViewModel>()

        // Assert
        viewModel.uiState.test {
            // 1. The ViewModel's init block calls prepareNewPack, so the first state is already preparing
            assertEquals(GachaUiState(isPreparing = true), awaitItem())

            // 2. Advance the dispatcher to allow all coroutines to complete
            advanceUntilIdle()

            // 3. Await the final state
            val finalState = awaitItem()

            // 4. Assert the final state is correct
            assertEquals(false, finalState.isPreparing)
            assertNull(finalState.error)
            assertEquals(8, finalState.pulledTcgCards.size)
            assertEquals("Rare Holo V", finalState.packRarity)
            assertEquals(GachaInteractionState.Tearing, finalState.interactionState)
        }
    }

    @Test
    fun `save cards is triggered correctly`() = runTest(testDispatcher) {
        // Arrange: Mock all repository calls needed for a successful pack creation
        val commonCards = (1..5).map { createFakeTcgCard("c$it", "Common") }
        val uncommonCards = (1..2).map { createFakeTcgCard("u$it", "Uncommon") }
        val rareCards = listOf(createFakeTcgCard("r1", "Charizard", "Rare Holo V"))

        coEvery { repository.getCards(any(), "rarity:Common") } returns Result.success(commonCards)
        coEvery { repository.getCards(any(), "rarity:Uncommon") } returns Result.success(uncommonCards)
        coEvery { repository.getCards(any(), coMatch { it.contains("Rare") }) } returns Result.success(rareCards)

        // Act
        val viewModel = get<GachaViewModel>()

        // Let the initial pack prepare
        advanceUntilIdle()

        // Trigger the save event
        viewModel.handleEvent(GachaEvent.SavePulledCards)
        advanceUntilIdle() // Allow the save coroutine to run

        // Assert
        // Verify that the save method was called for each card in the pack
        coVerify(exactly = 8) { repository.saveCardToCollection(any()) }
    }

    private fun createFakeTcgCard(id: String, name: String, rarity: String? = "Common", count: Int = 1) = TcgCard(
        id = id, name = name, hp = "100", types = listOf("Water"), evolvesFrom = null,
        images = null, rarity = rarity, flavorText = null, attacks = null,
        weaknesses = null, resistances = null, legalities = null, count = count
    )
}