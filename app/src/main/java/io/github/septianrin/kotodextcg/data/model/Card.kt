package io.github.septianrin.kotodextcg.data.model

data class Card(
    val id: String,
    val name: String,
    val types: List<String>?,
    val evolvesFrom: String?,
    val images: CardImages?,
    val rarity: String?
)