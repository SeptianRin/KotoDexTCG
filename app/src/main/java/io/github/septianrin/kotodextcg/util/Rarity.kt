package io.github.septianrin.kotodextcg.util

enum class Rarity(val key: String) {
    Secret("Rare Secret"),
    Rainbow("Rare Rainbow"),
    VMAX("Rare Holo VMAX"),
    VSTAR("Rare Holo VSTAR"),
    Holo("Rare Holo"),
    Amazing("Rare Holo Amazing"),
    GX("Rare Holo GX"),
    EX("Rare Holo EX"),
    V("Rare Holo V"),
    Rare("Rare"),
    Uncommon("Uncommon"),
    Common("Common");

    fun isHighestTierCard(): Boolean= listOf(
        Holo,
        Secret,
        Rainbow,
        VMAX,
        VSTAR
    ).contains(this)

    fun isHighTierCard(): Boolean= listOf(
        Amazing,
        GX,
        EX,
        V
    ).contains(this)

    fun isRareCard() = this == Rare

    fun isUncommonCard() = this == Uncommon

    companion object {
        fun parseRarity(rarity: String): Rarity {
            return entries.firstOrNull { it.key.equals(rarity, ignoreCase = true) } ?: Common
        }
    }
}