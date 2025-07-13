package io.github.septianrin.kotodextcg.utils

import androidx.lifecycle.SavedStateHandle
import io.github.septianrin.kotodextcg.domain.PokemonCardRepository
import io.github.septianrin.kotodextcg.ui.feature.carddetail.CardDetailViewModel
import io.github.septianrin.kotodextcg.ui.feature.cardlist.CardListViewModel
import io.github.septianrin.kotodextcg.ui.feature.collection.CollectionViewModel
import io.github.septianrin.kotodextcg.ui.feature.gacha.GachaViewModel
import io.mockk.mockk
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

// This is the function that creates and returns our list of test modules.
// It's now a top-level function in this file, making it accessible to other test classes.
fun createTestAppModule(): List<Module> = listOf(TestAppModule)

val TestAppModule = module {
    single<PokemonCardRepository> {
        mockk(relaxed = true)
    }

    viewModel { CardListViewModel(get()) }
    viewModel { CardDetailViewModel(get(), SavedStateHandle()) }
    viewModel { GachaViewModel(get()) }
    viewModel { CollectionViewModel(get()) } // <-- ADD THIS LINE
}