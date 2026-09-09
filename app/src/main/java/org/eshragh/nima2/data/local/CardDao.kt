package org.eshragh.nima2.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {
    @Query("SELECT * FROM offline_cards ORDER BY createdAt DESC")
    fun getAllCards(): Flow<List<OfflineCard>>

    @Query("SELECT * FROM offline_cards ORDER BY createdAt DESC")
    suspend fun getAllCardsSync(): List<OfflineCard>

    @Query("SELECT * FROM offline_cards WHERE status = :status ORDER BY createdAt ASC")
    suspend fun getCardsByStatus(status: SyncStatus): List<OfflineCard>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: OfflineCard): Long

    @Update
    suspend fun updateCard(card: OfflineCard): Int

    @Delete
    suspend fun deleteCard(card: OfflineCard): Int

    @Query("DELETE FROM offline_cards WHERE status = 'SYNCED'")
    suspend fun clearSyncedCards(): Int
}
