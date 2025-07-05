package io.github.septianrin.kotodextcg

import app.cash.turbine.test
import io.github.septianrin.kotodextcg.data.model.Card
import io.github.septianrin.kotodextcg.domain.PokemonCardRepository
import io.github.septianrin.kotodextcg.ui.viewmodel.CardListEvent
import io.github.septianrin.kotodextcg.ui.viewmodel.CardListUiState
import io.github.septianrin.kotodextcg.ui.viewmodel.CardListViewModel
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.GlobalContext.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import kotlin.test.Test

@ExperimentalCoroutinesApi
class CardListViewModelTest : KoinTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: PokemonCardRepository

    // Lazily inject the ViewModel to have control over its creation timing
    private val viewModel: CardListViewModel by inject()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
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
    fun `on init, state transitions through loading to loaded`() = runTest {
        // Arrange
        val mockCards = listOf(Card("1", "Pikachu", null, null, null, "Common"))
        // Mock the repository call that will be triggered by the ViewModel's init block
        coEvery { repository.getCards(1, null) } returns Result.success(mockCards)

        // Act & Assert
        viewModel.uiState.test {
            // 1. The first item emitted is the default state from the MutableStateFlow constructor.
            assertEquals(CardListUiState(), awaitItem())

            // 2. The init block's coroutine runs, setting isLoading to true.
            val loadingState = awaitItem()
            assertEquals(true, loadingState.isLoading)

            // 3. The coroutine finishes, setting isLoading to false and adding cards.
            val finalState = awaitItem()
            assertEquals(false, finalState.isLoading)
            assertEquals(1, finalState.cards.size)
            assertEquals("Pikachu", finalState.cards.first().name)
        }
    }

    @Test
    fun `search query updates state and fetches new cards`() = runTest {
        // Arrange
        coEvery { repository.getCards(1, null) } returns Result.success(listOf(Card("1", "Pikachu", null, null, null, "Common")))
        val searchResultCards = listOf(Card("2", "Charizard", null, null, null, "Rare"))
        val searchQuery = "name:\"charizard*\" or types:\"charizard*\" or evolvesFrom:\"charizard*\""
        coEvery { repository.getCards(1, searchQuery) } returns Result.success(searchResultCards)

        viewModel.uiState.test {
            // Consume the items from the init call
            awaitItem() // Default state
            awaitItem() // Loading state
            awaitItem() // Loaded "Pikachu" state

            // Act: send search event
            viewModel.handleEvent(CardListEvent.OnSearchQueryChanged("charizard"))

            // Assert: state updates immediately with the new query text
            val queryState = awaitItem()
            assertEquals("charizard", queryState.searchQuery)

            // Advance past the debounce delay to trigger the network call
            advanceUntilIdle()

            // Assert: state becomes loading for the search
            val searchLoadingState = awaitItem()
            assertEquals(true, searchLoadingState.isLoading)
            assertEquals("charizard", searchLoadingState.searchQuery)

            // Assert: final state has the search results
            val finalState = awaitItem()
            assertEquals(false, finalState.isLoading)
            assertEquals(1, finalState.cards.size)
            assertEquals("Charizard", finalState.cards.first().name)
        }
    }
}