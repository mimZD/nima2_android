package org.eshragh.nima2.data.repository

import kotlinx.coroutines.flow.Flow
import org.eshragh.nima2.data.local.CardDao
import org.eshragh.nima2.data.local.OfflineCard
import org.eshragh.nima2.data.local.SyncStatus
import org.eshragh.nima2.data.pref.UserPreferencesRepository
import org.eshragh.nima2.data.remote.RetrofitClient
import org.eshragh.nima2.data.remote.model.CreateCardRequest
import org.eshragh.nima2.data.remote.model.PlankaBoard
import org.eshragh.nima2.data.remote.model.PlankaList
import org.eshragh.nima2.data.remote.model.PlankaProject

data class ProjectsAndBoardsResult(
    val projects: List<PlankaProject>,
    val boards: List<PlankaBoard>
)

class CardRepository(
    private val cardDao: CardDao,
    private val userPreferencesRepository: UserPreferencesRepository
) {

    val offlineCards: Flow<List<OfflineCard>> = cardDao.getAllCards()

    suspend fun addOfflineCard(title: String, listId: String, listName: String?): Long {
        val card = OfflineCard(
            title = title.trim(),
            listId = listId,
            listName = listName,
            status = SyncStatus.PENDING
        )
        return cardDao.insertCard(card)
    }

    suspend fun deleteOfflineCard(card: OfflineCard) {
        cardDao.deleteCard(card)
    }

    suspend fun clearSyncedCards() {
        cardDao.clearSyncedCards()
    }

    suspend fun fetchProjectsAndBoards(serverUrl: String, token: String): Result<ProjectsAndBoardsResult> {
        return try {
            val api = RetrofitClient.getApi(serverUrl)
            val authHeader = "Bearer $token"
            val response = api.getProjects(authHeader)

            if (response.isSuccessful) {
                val body = response.body()
                val projects = body?.items ?: emptyList()
                val included = body?.included
                val boards = included?.boards ?: emptyList()

                Result.success(ProjectsAndBoardsResult(projects, boards))
            } else {
                Result.failure(Exception("خطا در دریافت اطلاعات: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("خطا در ارتباط شبکه: ${e.localizedMessage}"))
        }
    }

    suspend fun fetchListsForBoard(serverUrl: String, token: String, boardId: String): Result<List<PlankaList>> {
        return try {
            val api = RetrofitClient.getApi(serverUrl)
            val authHeader = "Bearer $token"
            val response = api.getBoardDetails(boardId, authHeader)

            if (response.isSuccessful) {
                val lists = response.body()?.included?.lists ?: emptyList()
                val activeLists = lists.filter {
                    !it.name.isNullOrBlank() && it.type != "archive" && it.type != "trash"
                }
                Result.success(activeLists)
            } else {
                Result.failure(Exception("خطا در دریافت لیست‌های بورد: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("خطا در شبکه: ${e.localizedMessage}"))
        }
    }

    suspend fun uploadPendingCards(serverUrl: String, token: String): Int {
        val pendingCards = cardDao.getCardsByStatus(SyncStatus.PENDING) +
                cardDao.getCardsByStatus(SyncStatus.FAILED)

        var uploadedCount = 0
        val api = RetrofitClient.getApi(serverUrl)
        val authHeader = "Bearer $token"

        for (card in pendingCards) {
            val uploadingCard = card.copy(status = SyncStatus.UPLOADING, errorMessage = null)
            cardDao.updateCard(uploadingCard)

            try {
                val response = api.createCard(
                    listId = card.listId,
                    authHeader = authHeader,
                    request = CreateCardRequest(name = card.title, type = "story")
                )

                if (response.isSuccessful && response.body()?.item != null) {
                    cardDao.updateCard(card.copy(status = SyncStatus.SYNCED, errorMessage = null))
                    uploadedCount++
                } else {
                    val error = "خطا (${response.code()}): ${response.errorBody()?.string() ?: "نامشخص"}"
                    cardDao.updateCard(card.copy(status = SyncStatus.FAILED, errorMessage = error))
                }
            } catch (e: Exception) {
                val error = "خطای اتصال: ${e.localizedMessage}"
                cardDao.updateCard(card.copy(status = SyncStatus.FAILED, errorMessage = error))
            }
        }

        return uploadedCount
    }
}
