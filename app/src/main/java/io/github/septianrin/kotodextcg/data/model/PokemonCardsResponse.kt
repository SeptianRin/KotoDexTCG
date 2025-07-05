package io.github.septianrin.kotodextcg.data.model

data class PokemonCardsResponse(
    val data: List<Card>,
    val page: Int,
    val pageSize: Int,
    val count: Int,
    val totalCount: Int
)