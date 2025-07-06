package io.github.septianrin.kotodextcg.data.api

import io.github.septianrin.kotodextcg.data.model.response.CardDetailResponse
import io.github.septianrin.kotodextcg.data.model.response.PokemonCardsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PokemonTcgApiService {
    @GET("cards")
    suspend fun getCards(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int = 8,
        @Query("q") query: String? = null
    ): Response<PokemonCardsResponse>

    @GET("cards/{id}")
    suspend fun getCardById(@Path("id") cardId: String): Response<CardDetailResponse>
}
