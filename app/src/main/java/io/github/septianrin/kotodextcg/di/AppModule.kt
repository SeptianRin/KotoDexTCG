package io.github.septianrin.kotodextcg.di

import androidx.room.Room
import io.github.septianrin.kotodextcg.data.api.PokemonTcgApiService
import io.github.septianrin.kotodextcg.data.db.KotoDexDatabase
import io.github.septianrin.kotodextcg.data.repository.PokemonCardRepositoryImpl
import io.github.septianrin.kotodextcg.domain.PokemonCardRepository
import io.github.septianrin.kotodextcg.ui.feature.carddetail.CardDetailViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import io.github.septianrin.kotodextcg.ui.feature.cardlist.CardListViewModel
import io.github.septianrin.kotodextcg.ui.feature.collection.CollectionViewModel
import io.github.septianrin.kotodextcg.ui.feature.gacha.GachaViewModel
import org.koin.android.ext.koin.androidApplication
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

    single {
        Room.databaseBuilder(
            androidApplication(),
            KotoDexDatabase::class.java,
            "kotodex-database"
        ).build()
    }

    single {
        get<KotoDexDatabase>().tcgCardDao()
    }

    single<PokemonCardRepository> {
        PokemonCardRepositoryImpl(apiService = get(), tcgCardDao = get())
    }

    viewModel {
        CardListViewModel(get())
    }

    viewModel {
        GachaViewModel(get())
    }
    viewModel { params ->
        CardDetailViewModel(
            repository = get(),
            savedStateHandle = params.get()
        )
    }

    viewModel {
        CollectionViewModel(get())
    }
}