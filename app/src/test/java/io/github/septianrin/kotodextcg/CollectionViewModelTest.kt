package io.github.septianrin.kotodextcg

import app.cash.turbine.test
import io.github.septianrin.kotodextcg.data.model.TcgCard
import io.github.septianrin.kotodextcg.domain.PokemonCardRepository
import io.github.septianrin.kotodextcg.ui.feature.collection.CollectionEvent
import io.github.septianrin.kotodextcg.ui.feature.collection.CollectionViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
class CollectionViewModelTest : KoinTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: PokemonCardRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        startKoin {
            modules(
                module {
                    single { repository }
                    factory { CollectionViewModel(get()) }
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
    fun `observes collection from repository`() = runTest {
        val mockCollection = listOf(createFakeTcgCard("col1", "Mew"), createFakeTcgCard("col2", "Mewtwo", count = 2))
        coEvery { repository.getCollection() } returns flowOf(mockCollection)

        val viewModel = get<CollectionViewModel>()

        viewModel.collectionState.test {
            val collection = awaitItem()
            assertEquals(2, collection.size)
            assertEquals(2, collection.find { it.id == "col2" }?.count)
        }
    }

    @Test
    fun `clear collection event calls repository`() = runTest {
        coEvery { repository.clearCollection() } returns Unit

        val viewModel = get<CollectionViewModel>()
        viewModel.handleEvent(CollectionEvent.ClearCollection)

        coVerify(exactly = 1) { repository.clearCollection() }
    }

    private fun createFakeTcgCard(id: String, name: String, count: Int = 1) = TcgCard(
        id = id, name = name, hp = "100", types = listOf("Psychic"), evolvesFrom = null,
        images = null, rarity = "Rare", flavorText = null, attacks = null,
        weaknesses = null, resistances = null, legalities = null, count = count
    )
}