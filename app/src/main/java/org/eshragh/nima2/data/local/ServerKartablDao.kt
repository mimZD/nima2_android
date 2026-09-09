package org.eshragh.nima2.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerKartablDao {
    @Query("SELECT * FROM cached_server_kartabl_cards")
    fun getAllCachedCards(): Flow<List<ServerKartablCardEntity>>

    @Query("SELECT * FROM cached_server_kartabl_cards")
    suspend fun getAllCachedCardsSync(): List<ServerKartablCardEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCards(cards: List<ServerKartablCardEntity>): List<Long>

    @Query("DELETE FROM cached_server_kartabl_cards")
    suspend fun clearAll(): Int

    @Query("DELETE FROM cached_server_kartabl_cards WHERE id = :id")
    suspend fun deleteCardById(id: String): Int
}
