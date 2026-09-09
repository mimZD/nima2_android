package org.eshragh.nima2.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "cached_projects")
data class CachedProjectEntity(
    @PrimaryKey val id: String,
    val name: String
)

@Entity(tableName = "cached_boards")
data class CachedBoardEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val name: String
)

@Entity(tableName = "cached_lists")
data class CachedListEntity(
    @PrimaryKey val id: String,
    val boardId: String,
    val name: String
)

@Entity(tableName = "cached_labels")
data class CachedLabelEntity(
    @PrimaryKey val id: String,
    val boardId: String,
    val name: String,
    val color: String
)

@Entity(tableName = "cached_full_list_cards")
data class CachedFullListCardEntity(
    @PrimaryKey val id: String,
    val listId: String,
    val name: String,
    val dueDate: String? = null,
    val attachmentCount: Int = 0,
    val labelNames: String? = null,
    val labelColors: String? = null,
    val position: Double = 0.0
)

@Dao
interface MetadataDao {
    @Query("SELECT * FROM cached_projects")
    suspend fun getCachedProjects(): List<CachedProjectEntity>

    @Query("SELECT * FROM cached_boards")
    suspend fun getCachedBoards(): List<CachedBoardEntity>

    @Query("SELECT * FROM cached_lists WHERE boardId = :boardId")
    suspend fun getCachedListsForBoard(boardId: String): List<CachedListEntity>

    @Query("SELECT * FROM cached_labels WHERE boardId = :boardId")
    suspend fun getCachedLabelsForBoard(boardId: String): List<CachedLabelEntity>

    @Query("SELECT * FROM cached_full_list_cards WHERE listId = :listId ORDER BY position ASC")
    fun getFullListCards(listId: String): kotlinx.coroutines.flow.Flow<List<CachedFullListCardEntity>>

    @Query("SELECT * FROM cached_full_list_cards WHERE listId = :listId ORDER BY position ASC")
    suspend fun getFullListCardsSync(listId: String): List<CachedFullListCardEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjects(projects: List<CachedProjectEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBoards(boards: List<CachedBoardEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLists(lists: List<CachedListEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLabels(labels: List<CachedLabelEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFullListCards(cards: List<CachedFullListCardEntity>): List<Long>

    @Query("DELETE FROM cached_full_list_cards WHERE listId = :listId")
    suspend fun clearFullListCards(listId: String): Int
}
