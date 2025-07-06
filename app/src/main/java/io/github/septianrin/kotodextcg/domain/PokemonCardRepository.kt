package io.github.septianrin.kotodextcg.domain

import io.github.septianrin.kotodextcg.data.model.TcgCard
import kotlinx.coroutines.flow.Flow

interface PokemonCardRepository {
    // Network operations
    suspend fun getCards(page: Int, query: String?): Result<List<TcgCard>>
    suspend fun getCardById(cardId: String): Result<TcgCard>

    // Database (Collection) operations
    fun getCollection(): Flow<List<TcgCard>>
    suspend fun saveCardToCollection(card: TcgCard)
    suspend fun clearCollection()}