package io.github.septianrin.kotodextcg.utils

import io.github.septianrin.kotodextcg.data.model.TcgCard
import io.github.septianrin.kotodextcg.domain.PokemonCardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A fake implementation of the PokemonCardRepository for use in instrumentation tests.
 * This allows us to control the data returned without making real network or database calls.
 */
class FakePokemonCardRepository : PokemonCardRepository {

    private val collectionFlow = MutableStateFlow<List<TcgCard>>(emptyList())
    private var cardsToReturn: Result<List<TcgCard>> = Result.success(emptyList())
    private var cardDetailToReturn: Result<TcgCard>? = null

    fun setCards(cards: List<TcgCard>) {
        cardsToReturn = Result.success(cards)
    }

    fun setCardDetail(card: TcgCard) {
        cardDetailToReturn = Result.success(card)
    }

    fun setCollection(cards: List<TcgCard>) {
        collectionFlow.value = cards
    }

    fun setError(exception: Exception) {
        cardsToReturn = Result.failure(exception)
        cardDetailToReturn = Result.failure(exception)
    }

    override suspend fun getCards(page: Int, query: String?): Result<List<TcgCard>> {
        return cardsToReturn
    }

    override suspend fun getCardById(cardId: String): Result<TcgCard> {
        return cardDetailToReturn ?: Result.failure(Exception("Card detail not set for this test"))
    }

    override fun getCollection(): Flow<List<TcgCard>> {
        return collectionFlow.asStateFlow()
    }

    override suspend fun saveCardToCollection(card: TcgCard) {
        val currentList = collectionFlow.value.toMutableList()
        val existingCard = currentList.find { it.id == card.id }
        if (existingCard != null) {
            existingCard.count++
        } else {
            currentList.add(card.copy(count = 1))
        }
        collectionFlow.value = currentList
    }

    override suspend fun clearCollection() {
        collectionFlow.value = emptyList()
    }
}
