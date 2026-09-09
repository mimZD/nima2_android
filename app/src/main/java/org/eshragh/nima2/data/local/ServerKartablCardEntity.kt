package org.eshragh.nima2.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_server_kartabl_cards")
data class ServerKartablCardEntity(
    @PrimaryKey val id: String,
    val name: String,
    val projectId: String,
    val projectName: String,
    val boardId: String,
    val boardName: String,
    val listId: String,
    val listName: String,
    val dueDate: String? = null,
    val attachmentCount: Int = 0,
    val labelNames: String? = null,
    val labelColors: String? = null
)
