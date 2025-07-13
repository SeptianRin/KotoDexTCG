package io.github.septianrin.kotodextcg.ui.feature.cardlist

import android.os.Build
import io.github.septianrin.kotodextcg.data.model.TcgCard
import io.github.septianrin.kotodextcg.domain.PokemonCardRepository
import io.github.septianrin.kotodextcg.utils.KotoDexTestRunner
import io.mockk.coEvery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.koin.test.inject
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(KotoDexTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU], manifest = Config.NONE)
class CardListViewModelRoboTest : KoinTest {

    // Injecting the mock repository and the ViewModel via Koin
    private val mockRepository: PokemonCardRepository by inject()
    private val viewModel: CardListViewModel by inject()

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        // Set the main coroutine dispatcher for unit testing
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        // Reset the main dispatcher to the original one after the test
        Dispatchers.resetMain()
    }

    @Test
    fun `loadNextPage SHOULD fetch cards and update state`() = runTest {
        // --- GIVEN ---
        // 1. Mock the initial load from the ViewModel's init block.
        //    Return 8 cards to ensure `canLoadMore` is true.
        val initialCards = List(8) {
            TcgCard(
                id = "init$it",
                name = "Initial Card $it",
                hp = "dicit",
                types = listOf(),
                evolvesFrom = "ponderum",
                images = null,
                rarity = "his",
                flavorText = "consetetur",
                attacks = listOf(),
                weaknesses = listOf(),
                resistances = listOf(),
                legalities = null,
                count = 1713
            )
        }
        coEvery { mockRepository.getCards(page = 1, any()) } returns Result.success(initialCards)

        // 2. Mock the load for the *next* page, which is what we want to test.
        val nextPageCards = listOf(
            TcgCard(
                id = "next1",
                name = "Next Page Card 1",
                hp = "honestatis",
                types = listOf(),
                evolvesFrom = "ad",
                images = null,
                rarity = "eirmod",
                flavorText = "eloquentiam",
                attacks = listOf(),
                weaknesses = listOf(),
                resistances = listOf(),
                legalities = null,
                count = 8985
            ),
            TcgCard(
                id = "next2",
                name = "Next Page Card 2",
                hp = "nonumy",
                types = listOf(),
                evolvesFrom = "sed",
                images = null,
                rarity = "constituto",
                flavorText = "pertinax",
                attacks = listOf(),
                weaknesses = listOf(),
                resistances = listOf(),
                legalities = null,
                count = 8612
            )
        )
        coEvery { mockRepository.getCards(page = 2, any()) } returns Result.success(nextPageCards)

        // --- WHEN ---
        // The ViewModel's init block runs automatically. We need to advance the dispatcher
        // to let the initial load complete.
        testDispatcher.scheduler.advanceUntilIdle()

        // Now, trigger the event we actually want to test.
        viewModel.handleEvent(CardListEvent.LoadNextPage)
        testDispatcher.scheduler.advanceUntilIdle()

        // --- THEN ---
        val state = viewModel.uiState.value
        // The list should contain both the initial cards and the next page cards
        assertEquals(10, state.tcgCards.size)
        // The current page should now be 2
        assertEquals(2, state.currentPage)
        assertEquals(false, state.isLoading)
        assertEquals(false, state.isLoadingNextPage)
    }

    @Test
    fun `loadNextPage on failure SHOULD update error state`() = runTest {
        // --- GIVEN ---
        // 1. Mock the initial load as successful to allow the next page to be loaded.
        val initialCards = List(8) {
            TcgCard(
                id = "init$it",
                name = "Initial Card $it",
                hp = "sonet",
                types = listOf(),
                evolvesFrom = "sociis",
                images = null,
                rarity = "prompta",
                flavorText = "tempor",
                attacks = listOf(),
                weaknesses = listOf(),
                resistances = listOf(),
                legalities = null,
                count = 5164
            )
        }
        coEvery { mockRepository.getCards(page = 1, any()) } returns Result.success(initialCards)

        // 2. Mock the load for the next page to return a failure.
        val errorMessage = "Network Error"
        coEvery { mockRepository.getCards(page = 2, any()) } returns Result.failure(
            Exception(
                errorMessage
            )
        )

        // --- WHEN ---
        // Let the initial load complete.
        testDispatcher.scheduler.advanceUntilIdle()

        // Trigger the event that will fail.
        viewModel.handleEvent(CardListEvent.LoadNextPage)
        testDispatcher.scheduler.advanceUntilIdle()

        // --- THEN ---
        val state = viewModel.uiState.value
        // The card list should still contain the cards from the successful first page load.
        assertEquals(8, state.tcgCards.size)
        // The error message should be set.
        assertEquals(errorMessage, state.error)
        assertEquals(false, state.isLoading)
        assertEquals(false, state.isLoadingNextPage)
    }
}