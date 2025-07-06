package io.github.septianrin.kotodextcg.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "collected_cards")
data class TcgCard(
    @PrimaryKey val id: String,
    val name: String,
    val hp: String?,
    val types: List<String>?, // Handled by TypeConverter
    val evolvesFrom: String?,
    @Embedded val images: CardImages?,
    val rarity: String?,
    val flavorText: String?,
    val attacks: List<Attack>?, // Handled by TypeConverter
    val weaknesses: List<Weakness>?, // Handled by TypeConverter
    val resistances: List<Resistance>?, // Handled by TypeConverter
    @Embedded val legalities: Legalities?,
    var count: Int = 1
)

// These sub-classes do not need to be entities themselves
data class Attack(
    val name: String,
    val cost: List<String>,
    val convertedEnergyCost: Int,
    val damage: String,
    val text: String
)

data class Weakness(
    val type: String,
    val value: String
)

data class Resistance(
    val type: String,
    val value: String
)

data class Legalities(
    val unlimited: String?,
    val expanded: String?
)

data class CardImages(
    val small: String?,
    val large: String?
)