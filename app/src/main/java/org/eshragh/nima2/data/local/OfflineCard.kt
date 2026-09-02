package org.eshragh.nima2.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "offline_cards")
data class OfflineCard(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val listId: String,
    val listName: String? = null,
    val status: SyncStatus = SyncStatus.PENDING,
    val errorMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
