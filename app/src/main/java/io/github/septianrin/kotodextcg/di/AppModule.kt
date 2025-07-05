package io.github.septianrin.kotodextcg.di

import io.github.septianrin.kotodextcg.data.api.PokemonTcgApiService
import io.github.septianrin.kotodextcg.data.repository.PokemonCardRepositoryImpl
import io.github.septianrin.kotodextcg.domain.PokemonCardRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import io.github.septianrin.kotodextcg.ui.viewmodel.CardListViewModel
import io.github.septianrin.kotodextcg.ui.viewmodel.GachaViewModel
import org.koin.androidx.viewmodel.dsl.viewModel

val appModule = module {

    single {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    single {
        Retrofit.Builder()
            .baseUrl("https://api.pokemontcg.io/v2/")
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single {
        get<Retrofit>().create(PokemonTcgApiService::class.java)
    }

    single<PokemonCardRepository> {
        PokemonCardRepositoryImpl(get())
    }

    viewModel {
        CardListViewModel(get())
    }

    viewModel {
        GachaViewModel(get())
    }
}