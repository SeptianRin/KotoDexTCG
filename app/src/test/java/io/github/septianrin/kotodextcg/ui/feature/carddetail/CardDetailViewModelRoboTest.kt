package io.github.septianrin.kotodextcg.ui.feature.carddetail

import android.os.Build
import androidx.lifecycle.SavedStateHandle // <-- Import
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
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.inject
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(KotoDexTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU], manifest = Config.NONE)
class CardDetailViewModelRoboTest : KoinTest {

    // Inject the mock repository as usual
    private val mockRepository: PokemonCardRepository by inject()

    // We will inject the ViewModel later, inside the test
    private lateinit var viewModel: CardDetailViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadCardDetail SHOULD fetch card using id from SavedStateHandle`() = runTest {
        // --- Test-Specific Setup ---
        val testCardId = "xy7-54"
        val fakeCard = TcgCard(
            id = testCardId,
            name = "Seth Mercer",
            hp = "ornatus",
            types = listOf(),
            evolvesFrom = "quaestio",
            images = null,
            rarity = "deterruisset",
            flavorText = "platea",
            attacks = listOf(),
            weaknesses = listOf(),
            resistances = listOf(),
            legalities = null,
            count = 3453,
        )

        // Create a SavedStateHandle with a pre-populated "cardId"
        val testSavedStateHandle = SavedStateHandle(mapOf("cardId" to testCardId))

        // Override the Koin module just for this test
        loadKoinModules(module {
            viewModel { CardDetailViewModel(get(), testSavedStateHandle) }
        })

        // Now we can get the ViewModel instance
        viewModel = get()
        // --- End Setup ---

        // Given: Tell the mock repository what to return for our test ID
        coEvery { mockRepository.getCardById(testCardId) } returns Result.success(fakeCard)

        // When: The ViewModel's init block or a load function is called
        // (Assuming the ViewModel automatically loads from the handle in its init block)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: Assert the state was updated correctly
        val state = viewModel.uiState.value
        assertEquals(testCardId, state.tcgCard?.id)
        assertEquals("Seth Mercer", state.tcgCard?.name)
    }
}