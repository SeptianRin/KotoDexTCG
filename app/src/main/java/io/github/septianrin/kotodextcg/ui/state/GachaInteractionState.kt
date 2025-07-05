package io.github.septianrin.kotodextcg.ui.state

sealed class GachaInteractionState {
    data object Tearing : GachaInteractionState()
    data object ShowingResults : GachaInteractionState()
}