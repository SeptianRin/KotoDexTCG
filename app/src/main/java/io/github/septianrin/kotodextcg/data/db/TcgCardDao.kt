package io.github.septianrin.kotodextcg.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.septianrin.kotodextcg.data.model.TcgCard
import kotlinx.coroutines.flow.Flow

@Dao
interface TcgCardDao {

    // Get all collected cards, ordered by name. Returns a Flow for reactive updates.
    @Query("SELECT * FROM collected_cards ORDER BY name ASC")
    fun getCollection(): Flow<List<TcgCard>>

    // Insert a card. If it already exists, do nothing.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCard(card: TcgCard)

    // Increment the count of an existing card.
    @Query("UPDATE collected_cards SET count = count + 1 WHERE id = :cardId")
    suspend fun incrementCardCount(cardId: String)

    // Check if a card exists in the collection.
    @Query("SELECT EXISTS(SELECT 1 FROM collected_cards WHERE id = :cardId LIMIT 1)")
    suspend fun cardExists(cardId: String): Boolean

    // Delete all cards from the collection.
    @Query("DELETE FROM collected_cards")
    suspend fun clearCollection()
}