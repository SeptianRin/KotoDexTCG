package io.github.septianrin.kotodextcg.ui.feature.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.septianrin.kotodextcg.data.model.TcgCard
import io.github.septianrin.kotodextcg.domain.PokemonCardRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface CollectionEvent {
    object ClearCollection : CollectionEvent
}

class CollectionViewModel(
    private val repository: PokemonCardRepository
) : ViewModel() {

    val collectionState: StateFlow<List<TcgCard>> = repository.getCollection()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun handleEvent(event: CollectionEvent) {
        when (event) {
            is CollectionEvent.ClearCollection -> {
                viewModelScope.launch {
                    repository.clearCollection()
                }
            }
        }
    }
}