package io.github.septianrin.kotodextcg.data.model.response

import io.github.septianrin.kotodextcg.data.model.TcgCard

data class PokemonCardsResponse(
    val data: List<TcgCard>,
    val page: Int,
    val pageSize: Int,
    val count: Int,
    val totalCount: Int
)