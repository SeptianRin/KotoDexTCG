package io.github.septianrin.kotodextcg.ui.feature.collection

import android.os.Build
import app.cash.turbine.test
import io.github.septianrin.kotodextcg.data.model.TcgCard
import io.github.septianrin.kotodextcg.domain.PokemonCardRepository
import io.github.septianrin.kotodextcg.utils.KotoDexTestRunner
import io.mockk.every
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.inject
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
@RunWith(KotoDexTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU], manifest = Config.NONE)
class CollectionViewModelRoboTest : KoinTest {

    // Inject only the repository at the class level
    private val mockRepository: PokemonCardRepository by inject()

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        // Set the main coroutine dispatcher for unit testing.
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        // Reset the main dispatcher to the original one.
        Dispatchers.resetMain()
    }

    @Test
    fun `collection state SHOULD update with cards from repository`() = runTest {
        // --- GIVEN ---
        val fakeCards = listOf(
            TcgCard(
                id = "dicam",
                name = "Rex Boone",
                hp = "option",
                types = listOf(),
                evolvesFrom = "legere",
                images = null,
                rarity = "vel",
                flavorText = "omnesque",
                attacks = listOf(),
                weaknesses = listOf(),
                resistances = listOf(),
                legalities = null,
                count = 6807
            ),
            TcgCard(
                id = "viris",
                name = "Luke Mercado",
                hp = "petentium",
                types = listOf(),
                evolvesFrom = "utinam",
                images = null,
                rarity = "mucius",
                flavorText = "error",
                attacks = listOf(),
                weaknesses = listOf(),
                resistances = listOf(),
                legalities = null,
                count = 1317
            )
        )
        // Configure the mock repository BEFORE the ViewModel is created
        every { mockRepository.getCollection() } returns flowOf(fakeCards)

        // --- WHEN ---
        // Create the ViewModel now. Its stateIn operator is now configured.
        val viewModel: CollectionViewModel = get()

        // --- THEN ---
        // Use Turbine to subscribe to the flow and test its emissions.
        viewModel.collectionState.test {
            // The first emission is always the initialValue from stateIn (emptyList).
            assertEquals(emptyList(), awaitItem())

            // The second emission is the list from our fake repository's flow.
            val collectedCards = awaitItem()
            assertEquals(2, collectedCards.size)
            assertEquals("Rex Boone", collectedCards[0].name)

            // Cancel the collector as we've verified what we need.
            cancelAndIgnoreRemainingEvents()
        }
    }
}