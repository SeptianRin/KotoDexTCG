package io.github.septianrin.kotodextcg.ui.feature.gacha

import android.os.Build
import io.github.septianrin.kotodextcg.data.model.TcgCard
import io.github.septianrin.kotodextcg.domain.PokemonCardRepository
import io.github.septianrin.kotodextcg.utils.KotoDexTestRunner
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.inject
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(KotoDexTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU], manifest = Config.NONE)
class GachaViewModelRoboTest : KoinTest {

    private val mockRepository: PokemonCardRepository by inject()

    // ViewModel is instantiated inside tests to control its lifecycle
    private lateinit var viewModel: GachaViewModel

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
    fun `init SHOULD prepare new pack and update state correctly`() = runTest {
        // --- GIVEN ---
        // Create predictable card pools for each rarity category
        val commonCards = List(10) {
            TcgCard(
                id = "common$it",
                name = "Common Card $it",
                hp = "metus",
                types = listOf(),
                evolvesFrom = "evertitur",
                images = null,
                rarity = "dicit",
                flavorText = "habitasse",
                attacks = listOf(),
                weaknesses = listOf(),
                resistances = listOf(),
                legalities = null,
                count = 3582
            )
        }
        val uncommonCards = List(5) {
            TcgCard(
                id = "uncommon$it",
                name = "Uncommon Card $it",
                hp = "sociosqu",
                types = listOf(),
                evolvesFrom = "varius",
                images = null,
                rarity = "invenire",
                flavorText = "congue",
                attacks = listOf(),
                weaknesses = listOf(),
                resistances = listOf(),
                legalities = null,
                count = 9244
            )
        }
        val rareCards = List(3) {
            TcgCard(
                id = "rare$it",
                name = "Rare Card $it",
                hp = "porttitor",
                types = listOf(),
                evolvesFrom = "constituto",
                images = null,
                rarity = "suas",
                flavorText = "constituam",
                attacks = listOf(),
                weaknesses = listOf(),
                resistances = listOf(),
                legalities = null,
                count = 7430
            )
        }

        // Mock the repository calls that will be triggered by the ViewModel's init block
        coEvery { mockRepository.getCards(any(), "rarity:Common") } returns Result.success(
            commonCards
        )
        coEvery { mockRepository.getCards(any(), "rarity:Uncommon") } returns Result.success(
            uncommonCards
        )
        // Use coMatch to catch any query that contains "rarity:" for the rare card call
        coEvery {
            mockRepository.getCards(
                any(),
                coMatch { it.contains("rarity:") && !it.contains("Common") && !it.contains("Uncommon") })
        } returns Result.success(rareCards)

        // --- WHEN ---
        // Creating the ViewModel triggers its init block, which calls prepareNewPack()
        viewModel = get()
        testDispatcher.scheduler.advanceUntilIdle()

        // --- THEN ---
        val state = viewModel.uiState.value

        // Assert that the state was updated correctly after preparing the pack
        assertFalse(state.isPreparing)
        assertEquals(8, state.pulledTcgCards.size) // 5 common + 2 uncommon + 1 rare
        assertNull(state.error)
    }

    @Test
    fun `SavePulledCards event SHOULD save all pulled cards to collection`() = runTest {
        // --- GIVEN ---
        // 1. Set up the mocks to successfully prepare a pack first
        val commonCards = List(10) {
            TcgCard(
                id = "common$it",
                name = "Common Card $it",
                hp = "mea",
                types = listOf(),
                evolvesFrom = "partiendo",
                images = null,
                rarity = "sadipscing",
                flavorText = "sumo",
                attacks = listOf(),
                weaknesses = listOf(),
                resistances = listOf(),
                legalities = null,
                count = 7825
            )
        }
        val uncommonCards = List(5) {
            TcgCard(
                id = "uncommon$it",
                name = "Uncommon Card $it",
                hp = "petentium",
                types = listOf(),
                evolvesFrom = "vehicula",
                images = null,
                rarity = "movet",
                flavorText = "populo",
                attacks = listOf(),
                weaknesses = listOf(),
                resistances = listOf(),
                legalities = null,
                count = 4438
            )
        }
        val rareCards = List(3) {
            TcgCard(
                id = "rare$it",
                name = "Rare Card $it",
                hp = "massa",
                types = listOf(),
                evolvesFrom = "mus",
                images = null,
                rarity = "elementum",
                flavorText = "eloquentiam",
                attacks = listOf(),
                weaknesses = listOf(),
                resistances = listOf(),
                legalities = null,
                count = 5649
            )
        }
        coEvery {
            mockRepository.getCards(
                any(),
                any()
            )
        } returns Result.success(commonCards + uncommonCards + rareCards)
        coEvery { mockRepository.saveCardToCollection(any()) } returns Unit

        // 2. Initialize the ViewModel and let it prepare the pack
        viewModel = get()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(8, viewModel.uiState.value.pulledTcgCards.size) // Verify pack is ready

        // --- WHEN ---
        // Trigger the event to save the cards
        viewModel.handleEvent(GachaEvent.SavePulledCards)
        testDispatcher.scheduler.advanceUntilIdle()

        // --- THEN ---
        // Verify that the saveCardToCollection method was called for each of the 8 cards in the pack
        coVerify(exactly = 8) {
            mockRepository.saveCardToCollection(any())
        }
    }
}