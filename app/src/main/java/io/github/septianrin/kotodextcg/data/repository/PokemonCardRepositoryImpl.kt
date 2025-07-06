package io.github.septianrin.kotodextcg.data.repository

import io.github.septianrin.kotodextcg.data.api.PokemonTcgApiService
import io.github.septianrin.kotodextcg.data.db.TcgCardDao
import io.github.septianrin.kotodextcg.data.model.TcgCard
import io.github.septianrin.kotodextcg.domain.PokemonCardRepository
import kotlinx.coroutines.flow.Flow

/**
 * Implementation of the PokemonCardRepository.
 * This class is responsible for fetching data from the network using the ApiService
 * and handling potential errors.
 *
 * @param apiService The Retrofit service for making network requests.
 */
class PokemonCardRepositoryImpl(
    private val apiService: PokemonTcgApiService,
    private val tcgCardDao: TcgCardDao
) : PokemonCardRepository {

    // --- Network Operations ---
    override suspend fun getCards(page: Int, query: String?): Result<List<TcgCard>> {
        return try {
            val response = apiService.getCards(page = page, query = query)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("API Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCardById(cardId: String): Result<TcgCard> {
        return try {
            val response = apiService.getCardById(cardId = cardId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("API Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Database (Collection) Operations ---

    override fun getCollection(): Flow<List<TcgCard>> {
        return tcgCardDao.getCollection()
    }

    override suspend fun saveCardToCollection(card: TcgCard) {
        // Check if the card already exists in the collection
        if (tcgCardDao.cardExists(card.id)) {
            // If it exists, just increment the count
            tcgCardDao.incrementCardCount(card.id)
        } else {
            // If it's a new card, ensure its count is 1 and insert it
            val cardToInsert = card.copy(count = 1)
            tcgCardDao.insertCard(cardToInsert)
        }
    }

    override suspend fun clearCollection() {
        tcgCardDao.clearCollection()
    }
}