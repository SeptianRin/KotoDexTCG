package io.github.septianrin.kotodextcg.domain

import io.github.septianrin.kotodextcg.data.model.Card

interface PokemonCardRepository {
    suspend fun getCards(page: Int, query: String?): Result<List<Card>>
}