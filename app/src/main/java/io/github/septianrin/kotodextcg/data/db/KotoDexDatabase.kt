package io.github.septianrin.kotodextcg.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.github.septianrin.kotodextcg.data.model.TcgCard


@Database(entities = [TcgCard::class], version = 2, exportSchema = false)
@TypeConverters(DatabaseConverters::class)
abstract class KotoDexDatabase : RoomDatabase() {
    abstract fun tcgCardDao(): TcgCardDao
}