package io.github.septianrin.kotodextcg.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.septianrin.kotodextcg.data.model.Card
import io.github.septianrin.kotodextcg.domain.PokemonCardRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CardListUiState(
    val cards: List<Card> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingNextPage: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val canLoadMore: Boolean = true,
    val searchQuery: String = ""
)

sealed interface CardListEvent {
    data object LoadFirstPage : CardListEvent
    data object LoadNextPage : CardListEvent
    data class OnSearchQueryChanged(val query: String) : CardListEvent
}

class CardListViewModel(
    private val repository: PokemonCardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CardListUiState())
    val uiState = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        // Load the initial data when the ViewModel is created.
        loadCards(page = 1)
    }

    fun handleEvent(event: CardListEvent) {
        when (event) {
            is CardListEvent.LoadFirstPage -> {
                _uiState.update { it.copy(currentPage = 1, searchQuery = "") }
                loadCards(page = 1)
            }

            is CardListEvent.LoadNextPage -> {
                if (_uiState.value.canLoadMore && !_uiState.value.isLoadingNextPage) {
                    loadCards(page = _uiState.value.currentPage + 1)
                }
            }

            is CardListEvent.OnSearchQueryChanged -> {
                _uiState.update { it.copy(searchQuery = event.query, currentPage = 1) }
                searchJob?.cancel()
                searchJob = viewModelScope.launch {
                    delay(1000L)
                    loadCards(page = 1)
                }
            }
        }
    }

    private fun loadCards(page: Int) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = if (page == 1) true else it.isLoading,
                    isLoadingNextPage = page > 1,
                    error = null
                )
            }

            val query = buildSearchQuery()

            repository.getCards(page, query)
                .onSuccess { newCards ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoadingNextPage = false,
                            cards = if (page == 1) newCards else it.cards + newCards,
                            currentPage = page,
                            canLoadMore = newCards.size == 8
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoadingNextPage = false,
                            error = error.message ?: "An unknown error occurred"
                        )
                    }
                }
        }
    }

    private fun buildSearchQuery(): String? {
        val query = _uiState.value.searchQuery.trim()
        return if (query.isNotBlank()) {
            "name:\"$query*\" or types:\"$query*\" or evolvesFrom:\"$query*\""
        } else {
            null
        }
    }
}