package io.github.septianrin.kotodextcg.ui.feature.cardlist

import app.cash.turbine.test
import io.github.septianrin.kotodextcg.data.model.TcgCard
import io.github.septianrin.kotodextcg.domain.PokemonCardRepository
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get

@ExperimentalCoroutinesApi
class CardListViewModelTest : KoinTest {

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
                    factory { CardListViewModel(get()) }
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
    fun `initial load success`() = runTest(testDispatcher) {
        val mockCards = listOf(createFakeTcgCard("1", "Pikachu"))
        coEvery { repository.getCards(1, null) } returns Result.success(mockCards)

        val viewModel = get<CardListViewModel>()

        viewModel.uiState.test {
            // 1. Await the very first, default state
            assertEquals(CardListUiState(), awaitItem())

            // 2. The init block runs, emitting the loading state
            assertEquals(true, awaitItem().isLoading)

            // 3. Advance the dispatcher to allow the coroutine to complete
            advanceUntilIdle()

            // 4. Await the final loaded state
            val finalState = awaitItem()
            assertEquals(false, finalState.isLoading)
            assertEquals(1, finalState.tcgCards.size)
        }
    }

    @Test
    fun `search query updates state and fetches new cards`() = runTest(testDispatcher) {
        val initialCards = listOf(createFakeTcgCard("1", "Pikachu"))
        val searchResultCards = listOf(createFakeTcgCard("2", "Charizard"))
        val searchQuery = "name:\"charizard*\" or types:\"charizard*\" or evolvesFrom:\"charizard*\""

        coEvery { repository.getCards(1, null) } returns Result.success(initialCards)
        coEvery { repository.getCards(1, searchQuery) } returns Result.success(searchResultCards)

        val viewModel = get<CardListViewModel>()

        viewModel.uiState.test {
            // Let the initial load complete
            advanceUntilIdle()
            skipItems(3) // Skip default, loading, and initial loaded states

            // Act: Send the search event
            viewModel.handleEvent(CardListEvent.OnSearchQueryChanged("charizard"))

            // Assert: Check the immediate state update with the new query
            val queryState = awaitItem()
            assertEquals("charizard", queryState.searchQuery)

            // Advance past the debounce
            advanceTimeBy(1001)

            // Assert: Check the loading state for the search
            val searchLoadingState = awaitItem()
            assertEquals(true, searchLoadingState.isLoading)

            // Assert: Check the final result state
            val finalState = awaitItem()
            assertEquals(false, finalState.isLoading)
            assertEquals("Charizard", finalState.tcgCards.first().name)
        }
    }

    private fun createFakeTcgCard(id: String, name: String) = TcgCard(
        id = id, name = name, hp = "100", types = listOf("Electric"), evolvesFrom = null,
        images = null, rarity = "Common", flavorText = null, attacks = null,
        weaknesses = null, resistances = null, legalities = null, count = 1
    )
}