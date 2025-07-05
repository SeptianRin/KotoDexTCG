package io.github.septianrin.kotodextcg.data.repository

import io.github.septianrin.kotodextcg.data.api.PokemonTcgApiService
import io.github.septianrin.kotodextcg.data.model.Card
import io.github.septianrin.kotodextcg.domain.PokemonCardRepository

/**
 * Implementation of the PokemonCardRepository.
 * This class is responsible for fetching data from the network using the ApiService
 * and handling potential errors.
 *
 * @param apiService The Retrofit service for making network requests.
 */
class PokemonCardRepositoryImpl(
    private val apiService: PokemonTcgApiService
) : PokemonCardRepository {

    override suspend fun getCards(page: Int, query: String?): Result<List<Card>> {
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
}