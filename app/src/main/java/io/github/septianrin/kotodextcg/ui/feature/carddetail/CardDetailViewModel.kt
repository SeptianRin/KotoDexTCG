package io.github.septianrin.kotodextcg.ui.feature.carddetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.septianrin.kotodextcg.data.model.TcgCard
import io.github.septianrin.kotodextcg.domain.PokemonCardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CardDetailUiState(
    val tcgCard: TcgCard? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

sealed interface CardDetailEvent {
    data object LoadDetails : CardDetailEvent
    data object ClearError : CardDetailEvent
}

class CardDetailViewModel(
    private val repository: PokemonCardRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(CardDetailUiState())
    val uiState = _uiState.asStateFlow()

    private val cardId: String = checkNotNull(savedStateHandle["cardId"])

    init {
        handleEvent(CardDetailEvent.LoadDetails)
    }

    fun handleEvent(event: CardDetailEvent) {
        when (event) {
            is CardDetailEvent.LoadDetails -> loadCardDetails()
            is CardDetailEvent.ClearError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun loadCardDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            repository.getCardById(cardId)
                .onSuccess { card ->
                    _uiState.update {
                        it.copy(isLoading = false, tcgCard = card, error = null)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "An unknown error occurred."
                        )
                    }
                }
        }
    }
}