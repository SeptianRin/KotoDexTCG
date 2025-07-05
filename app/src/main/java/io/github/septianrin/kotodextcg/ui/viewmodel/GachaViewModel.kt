package io.github.septianrin.kotodextcg.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.septianrin.kotodextcg.data.model.Card
import io.github.septianrin.kotodextcg.domain.PokemonCardRepository
import io.github.septianrin.kotodextcg.ui.state.GachaInteractionState
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GachaUiState(
    val pulledCards: List<Card> = emptyList(),
    val packRarity: String? = null,
    val isPreparing: Boolean = true,
    val error: String? = null,
    val interactionState: GachaInteractionState = GachaInteractionState.Tearing
)

sealed interface GachaEvent {
    data object PrepareNewPack : GachaEvent
    data object ResetToTear : GachaEvent
    data object ShowResults : GachaEvent
}

class GachaViewModel(
    private val repository: PokemonCardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GachaUiState())
    val uiState = _uiState.asStateFlow()

    init {
        handleEvent(GachaEvent.PrepareNewPack)
    }

    fun handleEvent(event: GachaEvent) {
        when (event) {
            is GachaEvent.PrepareNewPack -> prepareNewPack()
            is GachaEvent.ResetToTear -> _uiState.update {
                it.copy(interactionState = GachaInteractionState.Tearing)
            }
            is GachaEvent.ShowResults -> _uiState.update {
                it.copy(interactionState = GachaInteractionState.ShowingResults)
            }
        }
    }

    private fun prepareNewPack() {
        viewModelScope.launch {
            _uiState.update { it.copy(isPreparing = true, error = null)}

            try {
                val packContents = coroutineScope {
                    val commonDeferred = async { repository.getCards(page = (1..50).random(), query = "rarity:Common") }
                    val uncommonDeferred = async { repository.getCards(page = (1..50).random(), query = "rarity:Uncommon") }
                    val rareDeferred = async { getRareCard() }

                    val results = awaitAll(commonDeferred, uncommonDeferred, rareDeferred)

                    val commons = results[0].getOrNull()?.shuffled()?.take(5) ?: emptyList()
                    val uncommons = results[1].getOrNull()?.shuffled()?.take(2) ?: emptyList()
                    val rare = results[2].getOrNull()?.shuffled()?.take(1) ?: emptyList()

                    if (commons.isEmpty() || uncommons.isEmpty() || rare.isEmpty()) {
                        throw IllegalStateException("Failed to construct a full booster pack.")
                    }

                    (commons + uncommons + rare).shuffled()
                }

                _uiState.update {
                    it.copy(
                        pulledCards = packContents,
                        packRarity = determineBestRarity(packContents),
                        interactionState = GachaInteractionState.Tearing
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isPreparing = false, error = e.message ?: "Failed to prepare pack.")
                }
            } finally {
                _uiState.update {
                    it.copy(isPreparing = false)
                }
            }
        }
    }

    private suspend fun getRareCard(): Result<List<Card>> {
        val rarityRoll = (0..5).random()
        val rareQuery = when {
            rarityRoll > 4 -> "rarity:\"Rare Secret\" or rarity:\"Rare Rainbow\""
            rarityRoll > 3 -> "rarity:\"Rare Holo VMAX\" or rarity:\"Rare Holo VSTAR\""
            rarityRoll > 2 -> "rarity:\"Rare Holo V\""
            rarityRoll > 1 -> "rarity:\"Rare Holo\""
            else -> "rarity:Rare"
        }
        return repository.getCards(page = (1..5).random(), query = rareQuery)
    }

    private fun determineBestRarity(cards: List<Card>): String {
        val rarityOrder = listOf(
            "Rare Secret", "Rare Rainbow", "Rare Holo VMAX", "Rare Holo VSTAR",
            "Rare Holo V", "Rare Holo", "Rare", "Uncommon", "Common"
        )
        return cards
            .mapNotNull { it.rarity }
            .minByOrNull { rarityOrder.indexOf(it).takeIf { i -> i != -1 } ?: Int.MAX_VALUE }
            ?: "Common"
    }
}