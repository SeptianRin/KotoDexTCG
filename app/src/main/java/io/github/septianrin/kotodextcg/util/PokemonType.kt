package io.github.septianrin.kotodextcg.util

import androidx.compose.ui.graphics.Color

enum class PokemonType(val typeName: String, val color: Color) {
    GRASS("Grass", Color(0xFF4CAF50)),
    FIRE("Fire", Color(0xFFF44336)),
    WATER("Water", Color(0xFF2196F3)),
    LIGHTNING("Lightning", Color(0xFFFFEB3B)),
    PSYCHIC("Psychic", Color(0xFF9C27B0)),
    FIGHTING("Fighting", Color(0xFFE65100)),
    DARKNESS("Darkness", Color(0xFF212121)),
    METAL("Metal", Color(0xFF9E9E9E)),
    FAIRY("Fairy", Color(0xFFE91E63)),
    DRAGON("Dragon", Color(0xFFC51162)),
    COLORLESS("Colorless", Color(0xFFBDBDBD));

    companion object {
        fun fromString(type: String?): PokemonType {
            return entries.find { it.typeName.equals(type, ignoreCase = true) }
                ?: COLORLESS
        }
    }
}