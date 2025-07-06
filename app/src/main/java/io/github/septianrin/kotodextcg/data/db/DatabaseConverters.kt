package io.github.septianrin.kotodextcg.data.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.github.septianrin.kotodextcg.data.model.Attack
import io.github.septianrin.kotodextcg.data.model.Resistance
import io.github.septianrin.kotodextcg.data.model.Weakness

class DatabaseConverters {
    private val gson = Gson()

    // Converters for List<String> (for card types)
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return gson.fromJson(value, object : TypeToken<List<String>?>() {}.type)
    }

    // Converters for List<Attack>
    @TypeConverter
    fun fromAttackList(value: List<Attack>?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toAttackList(value: String?): List<Attack>? {
        return gson.fromJson(value, object : TypeToken<List<Attack>?>() {}.type)
    }

    // Converters for List<Weakness>
    @TypeConverter
    fun fromWeaknessList(value: List<Weakness>?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toWeaknessList(value: String?): List<Weakness>? {
        return gson.fromJson(value, object : TypeToken<List<Weakness>?>() {}.type)
    }

    // Converters for List<Resistance>
    @TypeConverter
    fun fromResistanceList(value: List<Resistance>?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toResistanceList(value: String?): List<Resistance>? {
        return gson.fromJson(value, object : TypeToken<List<Resistance>?>() {}.type)
    }
}