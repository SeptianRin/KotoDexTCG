package io.github.septianrin.kotodextcg.util

object AppRoutes {
    const val HOME = "home"
    const val GACHA = "gacha"
    const val DETAIL = "detail/{cardId}"
    const val COLLECTION = "collection"

    fun detailRoute(cardId: String) = "detail/$cardId"
}