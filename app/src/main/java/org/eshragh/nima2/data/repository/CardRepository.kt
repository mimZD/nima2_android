package org.eshragh.nima2.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.eshragh.nima2.data.local.CachedBoardEntity
import org.eshragh.nima2.data.local.CachedFullListCardEntity
import org.eshragh.nima2.data.local.CachedLabelEntity
import org.eshragh.nima2.data.local.CachedListEntity
import org.eshragh.nima2.data.local.CachedProjectEntity
import org.eshragh.nima2.data.local.CardDao
import org.eshragh.nima2.data.local.MetadataDao
import org.eshragh.nima2.data.local.OfflineCard
import org.eshragh.nima2.data.local.ServerKartablCardEntity
import org.eshragh.nima2.data.local.ServerKartablDao
import org.eshragh.nima2.data.local.SyncStatus
import org.eshragh.nima2.data.pref.UserPreferencesRepository
import org.eshragh.nima2.data.remote.RetrofitClient
import org.eshragh.nima2.data.remote.model.AddCardLabelRequest
import org.eshragh.nima2.data.remote.model.CreateCardRequest
import org.eshragh.nima2.data.remote.model.CreateLabelRequest
import org.eshragh.nima2.data.remote.model.UpdateCardRequest
import org.eshragh.nima2.data.remote.model.PlankaAttachment
import org.eshragh.nima2.data.remote.model.PlankaBoard
import org.eshragh.nima2.data.remote.model.PlankaCard
import org.eshragh.nima2.data.remote.model.PlankaCardLabel
import org.eshragh.nima2.data.remote.model.PlankaLabel
import org.eshragh.nima2.data.remote.model.PlankaList
import org.eshragh.nima2.data.remote.model.PlankaProject

data class ProjectsAndBoardsResult(
    val projects: List<PlankaProject>,
    val boards: List<PlankaBoard>
)

data class BoardDetailsContent(
    val lists: List<PlankaList>,
    val labels: List<PlankaLabel>
)

data class ServerKartablCard(
    val id: String,
    val name: String,
    val projectId: String,
    val projectName: String,
    val boardId: String,
    val boardName: String,
    val listId: String,
    val listName: String,
    val dueDate: String? = null,
    val labels: List<PlankaLabel> = emptyList(),
    val attachmentCount: Int = 0
)

data class ServerKartablResult(
    val cards: List<ServerKartablCard>,
    val boardListsMap: Map<String, List<PlankaList>>,
    val boardLabelsMap: Map<String, List<PlankaLabel>>,
    val boardAttachmentsMap: Map<String, List<PlankaAttachment>>
)

class CardRepository(
    private val context: android.content.Context,
    private val cardDao: CardDao,
    private val serverKartablDao: ServerKartablDao,
    private val metadataDao: MetadataDao,
    private val userPreferencesRepository: UserPreferencesRepository
) {

    fun getUserPreferencesRepository() = userPreferencesRepository

    suspend fun getPendingCardsSync(): List<OfflineCard> {
        return cardDao.getCardsByStatus(SyncStatus.PENDING) + cardDao.getCardsByStatus(SyncStatus.FAILED)
    }

    suspend fun updateCardStatus(card: OfflineCard, status: SyncStatus, error: String? = null) {
        val updated = card.copy(status = status, errorMessage = error)
        cardDao.updateCard(updated)
    }

    fun triggerSync() {
        val constraints = androidx.work.Constraints.Builder()
            .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
            .build()
        
        val syncRequest = androidx.work.OneTimeWorkRequestBuilder<org.eshragh.nima2.data.work.SyncWorker>()
            .setConstraints(constraints)
            .build()
            
        androidx.work.WorkManager.getInstance(context).enqueueUniqueWork(
            "SyncCards",
            androidx.work.ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }

    suspend fun saveLocalFile(uri: android.net.Uri): String? {
        return try {
            val contentResolver = context.contentResolver
            val fileName = "att_${System.currentTimeMillis()}_" + (getFileName(uri) ?: "file")
            val file = java.io.File(context.filesDir, "attachments")
            if (!file.exists()) file.mkdirs()
            
            val targetFile = java.io.File(file, fileName)
            contentResolver.openInputStream(uri)?.use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            targetFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    private fun getFileName(uri: android.net.Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (index != -1) result = it.getString(index)
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) result = result?.substring(cut + 1)
        }
        return result
    }

    val offlineCards: Flow<List<OfflineCard>> = cardDao.getAllCards()

    suspend fun getAllCardsSync(): List<OfflineCard> = cardDao.getAllCardsSync()

    val cachedServerCards: Flow<List<ServerKartablCard>> = serverKartablDao.getAllCachedCards().map { entities ->
        android.util.Log.d("NIMA2_KARTABL_DEBUG", "Room DB emitted cached entities count: ${entities.size}")
        entities.map { entity ->
            val assignedLabels = if (!entity.labelNames.isNullOrBlank()) {
                val names = entity.labelNames.split(",")
                val colors = entity.labelColors?.split(",") ?: emptyList()
                names.mapIndexed { idx, name ->
                    PlankaLabel(id = "cached_$idx", boardId = entity.boardId, name = name, color = colors.getOrNull(idx))
                }
            } else emptyList()

            ServerKartablCard(
                id = entity.id,
                name = entity.name,
                projectId = entity.projectId,
                projectName = entity.projectName,
                boardId = entity.boardId,
                boardName = entity.boardName,
                listId = entity.listId,
                listName = entity.listName,
                dueDate = entity.dueDate,
                labels = assignedLabels,
                attachmentCount = entity.attachmentCount
            )
        }
    }

    suspend fun getCachedServerCardsSync(): List<ServerKartablCard> {
        return serverKartablDao.getAllCachedCardsSync().map { entity ->
            val assignedLabels = if (!entity.labelNames.isNullOrBlank()) {
                val names = entity.labelNames.split(",")
                val colors = entity.labelColors?.split(",") ?: emptyList()
                names.mapIndexed { idx, name ->
                    PlankaLabel(id = "cached_$idx", boardId = entity.boardId, name = name, color = colors.getOrNull(idx))
                }
            } else emptyList()

            ServerKartablCard(
                id = entity.id,
                name = entity.name,
                projectId = entity.projectId,
                projectName = entity.projectName,
                boardId = entity.boardId,
                boardName = entity.boardName,
                listId = entity.listId,
                listName = entity.listName,
                dueDate = entity.dueDate,
                labels = assignedLabels,
                attachmentCount = entity.attachmentCount
            )
        }
    }

    fun getFullListCards(
        listId: String,
        projectId: String = "",
        projectName: String = "",
        boardId: String = "",
        boardName: String = "",
        listName: String = ""
    ): Flow<List<ServerKartablCard>> {
        return metadataDao.getFullListCards(listId).map { entities ->
            entities.map { entity ->
                val assignedLabels = if (!entity.labelNames.isNullOrBlank()) {
                    val names = entity.labelNames.split(",")
                    val colors = entity.labelColors?.split(",") ?: emptyList()
                    names.mapIndexed { idx, name ->
                        PlankaLabel(id = "cached_$idx", boardId = boardId, name = name, color = colors.getOrNull(idx))
                    }
                } else emptyList()

                ServerKartablCard(
                    id = entity.id,
                    name = entity.name,
                    projectId = projectId,
                    projectName = projectName,
                    boardId = boardId,
                    boardName = boardName,
                    listId = entity.listId,
                    listName = listName,
                    dueDate = entity.dueDate,
                    labels = assignedLabels,
                    attachmentCount = entity.attachmentCount
                )
            }
        }
    }

    suspend fun getFullListCardsSync(
        listId: String,
        projectId: String = "",
        projectName: String = "",
        boardId: String = "",
        boardName: String = "",
        listName: String = ""
    ): List<ServerKartablCard> {
        return metadataDao.getFullListCardsSync(listId).map { entity ->
            val assignedLabels = if (!entity.labelNames.isNullOrBlank()) {
                val names = entity.labelNames.split(",")
                val colors = entity.labelColors?.split(",") ?: emptyList()
                names.mapIndexed { idx, name ->
                    PlankaLabel(id = "cached_$idx", boardId = "", name = name, color = colors.getOrNull(idx))
                }
            } else emptyList()

            ServerKartablCard(
                id = entity.id,
                name = entity.name,
                projectId = projectId,
                projectName = projectName,
                boardId = boardId,
                boardName = boardName,
                listId = entity.listId,
                listName = listName,
                dueDate = entity.dueDate,
                labels = assignedLabels,
                attachmentCount = entity.attachmentCount
            )
        }
    }

    suspend fun fetchFullListCards(
        serverUrl: String,
        token: String,
        projectId: String,
        projectName: String,
        boardId: String,
        boardName: String,
        listId: String,
        listName: String
    ): Result<List<ServerKartablCard>> {
        return try {
            val api = RetrofitClient.getApi(serverUrl)
            val authHeader = "Bearer $token"
            val response = api.getBoardDetails(boardId, authHeader)

            if (response.isSuccessful) {
                val included = response.body()?.included
                val cards = included?.cards?.filter { it.listId == listId } ?: emptyList()
                val labels = included?.labels ?: emptyList()
                val cardLabels = included?.cardLabels ?: emptyList()
                val attachments = included?.attachments ?: emptyList()

                val labelMap = labels.associateBy { it.id }
                val attachmentMap = attachments.groupBy { it.cardId }
                val cardToLabelIdsMap = cardLabels.groupBy { it.cardId }

                val resultCards = cards.map { card ->
                    val cardLabelRelations = cardToLabelIdsMap[card.id] ?: emptyList()
                    val assignedLabels = cardLabelRelations.mapNotNull { labelMap[it.labelId] }
                    
                    val actualAttachmentCount = attachmentMap[card.id]?.size ?: 0

                    ServerKartablCard(
                        id = card.id,
                        name = card.name,
                        projectId = projectId,
                        projectName = projectName,
                        boardId = boardId,
                        boardName = boardName,
                        listId = listId,
                        listName = listName,
                        dueDate = card.dueDate,
                        labels = assignedLabels,
                        attachmentCount = if (actualAttachmentCount > 0) actualAttachmentCount else (card.attachmentsCount ?: 0)
                    )
                }

                // Cache them
                try {
                    metadataDao.clearFullListCards(listId)
                    val entities = resultCards.map { card ->
                        val labelNamesStr = card.labels.map { it.name ?: "" }.joinToString(",")
                        val labelColorsStr = card.labels.map { it.color ?: "" }.joinToString(",")
                        CachedFullListCardEntity(
                            id = card.id,
                            listId = listId,
                            name = card.name,
                            dueDate = card.dueDate,
                            attachmentCount = card.attachmentCount,
                            labelNames = if (labelNamesStr.isNotBlank()) labelNamesStr else null,
                            labelColors = if (labelColorsStr.isNotBlank()) labelColorsStr else null
                        )
                    }
                    metadataDao.insertFullListCards(entities)
                } catch (_: Exception) {}

                Result.success(resultCards)
            } else {
                Result.failure(Exception("خطا در دریافت کارت‌های لیست: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCachedProjectsAndBoardsSync(): ProjectsAndBoardsResult {
        val projects = metadataDao.getCachedProjects().map { PlankaProject(it.id, it.name) }
        val boards = metadataDao.getCachedBoards().map { PlankaBoard(it.id, it.projectId, it.name) }
        return ProjectsAndBoardsResult(projects, boards)
    }

    suspend fun addOfflineCard(
        title: String,
        listId: String,
        listName: String?,
        labelIds: String? = null,
        labelNames: String? = null,
        labelColors: String? = null,
        dueDate: String? = null,
        localAttachmentPaths: String? = null,
        attachmentCount: Int = 0
    ): Long {
        val card = OfflineCard(
            title = title.trim(),
            listId = listId,
            listName = listName,
            labelIds = labelIds,
            labelNames = labelNames,
            labelColors = labelColors,
            dueDate = dueDate,
            localAttachmentPaths = localAttachmentPaths,
            attachmentCount = attachmentCount,
            status = SyncStatus.PENDING
        )
        return cardDao.insertCard(card)
    }

    suspend fun deleteOfflineCard(card: OfflineCard) {
        cardDao.deleteCard(card)
    }

    suspend fun updateCardTitle(card: OfflineCard, newTitle: String) {
        val updated = card.copy(title = newTitle.trim())
        cardDao.updateCard(updated)
    }

    suspend fun updateCardFull(
        card: OfflineCard,
        newTitle: String,
        newListId: String? = null,
        newListName: String? = null,
        labelIds: String?,
        labelNames: String?,
        labelColors: String?,
        dueDate: String?
    ) {
        val updated = card.copy(
            title = newTitle.trim(),
            listId = newListId ?: card.listId,
            listName = newListName ?: card.listName,
            labelIds = labelIds,
            labelNames = labelNames,
            labelColors = labelColors,
            dueDate = dueDate
        )
        cardDao.updateCard(updated)
    }

    suspend fun clearSyncedCards() {
        cardDao.clearSyncedCards()
    }

    suspend fun deleteServerCard(serverUrl: String, token: String, cardId: String): Result<Boolean> {
        return try {
            val api = RetrofitClient.getApi(serverUrl)
            val authHeader = "Bearer $token"
            val response = api.deleteCard(cardId, authHeader)
            if (response.isSuccessful) {
                try {
                    serverKartablDao.deleteCardById(cardId)
                } catch (_: Exception) {}
                Result.success(true)
            } else {
                Result.failure(Exception("خطا در حذف کارت از سرور: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateServerCard(
        serverUrl: String,
        token: String,
        cardId: String,
        newTitle: String,
        newBoardId: String? = null,
        newListId: String? = null,
        newDueDateISO: String?
    ): Result<org.eshragh.nima2.data.remote.model.PlankaCard> {
        return try {
            val api = RetrofitClient.getApi(serverUrl)
            val authHeader = "Bearer $token"
            val response = api.updateCard(
                cardId = cardId,
                authHeader = authHeader,
                request = UpdateCardRequest(
                    name = newTitle,
                    boardId = newBoardId,
                    listId = newListId,
                    dueDate = if (newDueDateISO.isNullOrBlank()) null else newDueDateISO,
                    position = 65535.0
                )
            )
            val updatedCard = response.body()?.item
            if (response.isSuccessful && updatedCard != null) {
                Result.success(updatedCard)
            } else {
                Result.failure(Exception("خطا در به‌روزرسانی کارت روی سرور: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadServerAttachment(serverUrl: String, token: String, cardId: String, filePath: String): Result<Boolean> {
        return try {
            val api = RetrofitClient.getApi(serverUrl)
            val authHeader = "Bearer $token"
            val file = java.io.File(filePath)
            val mediaType = "application/octet-stream".toMediaTypeOrNull()
            val requestFile = file.asRequestBody(mediaType)
            
            // ORDER MATTERS: type and name must come BEFORE file part for some server parsers
            val parts = listOf(
                okhttp3.MultipartBody.Part.createFormData("type", "file"),
                okhttp3.MultipartBody.Part.createFormData("name", file.name),
                okhttp3.MultipartBody.Part.createFormData("file", file.name, requestFile)
            )

            val response = api.uploadAttachment(cardId, authHeader, parts)
            if (response.isSuccessful) Result.success(true)
            else Result.failure(Exception("خطا در آپلود فایل: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteServerAttachment(serverUrl: String, token: String, attachmentId: String): Result<Boolean> {
        return try {
            val api = RetrofitClient.getApi(serverUrl)
            val authHeader = "Bearer $token"
            val response = api.deleteAttachment(attachmentId, authHeader)
            if (response.isSuccessful) Result.success(true)
            else Result.failure(Exception("خطا در حذف فایل از سرور: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
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

                try {
                    metadataDao.insertProjects(projects.map { CachedProjectEntity(it.id, it.name) })
                    metadataDao.insertBoards(boards.map { CachedBoardEntity(it.id, it.projectId ?: "", it.name) })

                    // Pre-cache lists and labels for ALL boards in background
                    CoroutineScope(Dispatchers.IO).launch {
                        for (board in boards) {
                            try {
                                val boardRes = api.getBoardDetails(board.id, authHeader)
                                if (boardRes.isSuccessful) {
                                    val bIncluded = boardRes.body()?.included
                                    val lists = bIncluded?.lists ?: emptyList()
                                    val labels = bIncluded?.labels ?: emptyList()
                                    val activeLists = lists.filter {
                                        !it.name.isNullOrBlank() && it.type != "archive" && it.type != "trash"
                                    }
                                    metadataDao.insertLists(activeLists.map { CachedListEntity(it.id, it.boardId, it.name ?: "لیست") })
                                    metadataDao.insertLabels(labels.map { CachedLabelEntity(it.id, it.boardId, it.name ?: "برچسب", it.color ?: "blue-xchange") })
                                }
                            } catch (_: Exception) {}
                        }
                    }
                } catch (_: Exception) {}

                Result.success(ProjectsAndBoardsResult(projects, boards))
            } else {
                loadProjectsAndBoardsFromCache()
            }
        } catch (_: Exception) {
            loadProjectsAndBoardsFromCache()
        }
    }

    private suspend fun loadProjectsAndBoardsFromCache(): Result<ProjectsAndBoardsResult> {
        return try {
            val cachedProjects = metadataDao.getCachedProjects().map { PlankaProject(it.id, it.name) }
            val cachedBoards = metadataDao.getCachedBoards().map { PlankaBoard(it.id, it.projectId, it.name) }
            if (cachedProjects.isNotEmpty() || cachedBoards.isNotEmpty()) {
                Result.success(ProjectsAndBoardsResult(cachedProjects, cachedBoards))
            } else {
                Result.failure(Exception("هیچ اطلاعاتی به صورت آفلاین ذخیره نشده است"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchBoardDetailsContent(serverUrl: String, token: String, boardId: String): Result<BoardDetailsContent> {
        return try {
            val api = RetrofitClient.getApi(serverUrl)
            val authHeader = "Bearer $token"
            val response = api.getBoardDetails(boardId, authHeader)

            if (response.isSuccessful) {
                val included = response.body()?.included
                val lists = included?.lists ?: emptyList()
                val labels = included?.labels ?: emptyList()
                val activeLists = lists.filter {
                    !it.name.isNullOrBlank() && it.type != "archive" && it.type != "trash"
                }

                try {
                    metadataDao.insertLists(activeLists.map { CachedListEntity(it.id, it.boardId, it.name ?: "لیست") })
                    metadataDao.insertLabels(labels.map { CachedLabelEntity(it.id, it.boardId, it.name ?: "برچسب", it.color ?: "blue-xchange") })
                } catch (_: Exception) {}

                Result.success(BoardDetailsContent(activeLists, labels))
            } else {
                loadListsAndLabelsFromCache(boardId)
            }
        } catch (_: Exception) {
            loadListsAndLabelsFromCache(boardId)
        }
    }

    private suspend fun loadListsAndLabelsFromCache(boardId: String): Result<BoardDetailsContent> {
        return try {
            val cachedLists = metadataDao.getCachedListsForBoard(boardId).map { PlankaList(it.id, it.boardId, it.name) }
            val cachedLabels = metadataDao.getCachedLabelsForBoard(boardId).map { PlankaLabel(it.id, it.boardId, it.name, it.color) }
            Result.success(BoardDetailsContent(cachedLists, cachedLabels))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchAllServerKartablCards(
        serverUrl: String,
        token: String,
        projects: List<PlankaProject>,
        boards: List<PlankaBoard>
    ): Result<ServerKartablResult> {
        return try {
            val api = RetrofitClient.getApi(serverUrl)
            val authHeader = "Bearer $token"
            val kartablCards = mutableListOf<ServerKartablCard>()
            val boardListsMap = mutableMapOf<String, List<PlankaList>>()
            val boardLabelsMap = mutableMapOf<String, List<PlankaLabel>>()
            val boardAttachmentsMap = mutableMapOf<String, List<PlankaAttachment>>()

            val projectMap = projects.associateBy { it.id }

            for (board in boards) {
                try {
                    val response = api.getBoardDetails(board.id, authHeader)
                    if (response.isSuccessful) {
                        val included = response.body()?.included ?: continue
                        val cards = included.cards ?: emptyList()
                        val lists = included.lists ?: emptyList()
                        val labels = included.labels ?: emptyList()
                        val cardLabels = included.cardLabels ?: emptyList()
                        val attachments = included.attachments ?: emptyList()

                        val activeLists = lists.filter {
                            !it.name.isNullOrBlank() && it.type != "archive" && it.type != "trash"
                        }
                        boardListsMap[board.id] = activeLists
                        boardLabelsMap[board.id] = labels
                        boardAttachmentsMap[board.id] = attachments

                        val listMap = activeLists.associateBy { it.id }
                        val labelMap = labels.associateBy { it.id }
                        val projName = projectMap[board.projectId]?.name ?: "پروژه"

                        val cardToLabelIdsMap = cardLabels.groupBy { it.cardId }
                        val cardToAttachmentsMap = attachments.groupBy { it.cardId }

                        for (card in cards) {
                            if (!card.dueDate.isNullOrBlank() && card.isClosed != true) {
                                val list = listMap[card.listId]
                                val listName = list?.name ?: "لیست"

                                val cardLabelRelations = cardToLabelIdsMap[card.id] ?: emptyList()
                                val assignedLabels = cardLabelRelations.mapNotNull { labelMap[it.labelId] }
                                
                                val actualAttachmentCount = cardToAttachmentsMap[card.id]?.size ?: 0

                                kartablCards.add(
                                    ServerKartablCard(
                                        id = card.id,
                                        name = card.name,
                                        projectId = board.projectId ?: "",
                                        projectName = projName,
                                        boardId = board.id,
                                        boardName = board.name,
                                        listId = card.listId,
                                        listName = listName,
                                        dueDate = card.dueDate,
                                        labels = assignedLabels,
                                        attachmentCount = if (actualAttachmentCount > 0) actualAttachmentCount else (card.attachmentsCount ?: 0)
                                    )
                                )
                            }
                        }
                    }
                } catch (_: Exception) {
                    // Continue to next board if a single board fetch fails
                }
            }

            val result = ServerKartablResult(kartablCards, boardListsMap, boardLabelsMap, boardAttachmentsMap)
            try {
                if (kartablCards.isNotEmpty()) {
                    serverKartablDao.clearAll()
                    val entities = kartablCards.map { card ->
                        val labelNamesStr = card.labels.map { it.name ?: "" }.joinToString(",")
                        val labelColorsStr = card.labels.map { it.color ?: "" }.joinToString(",")
                        ServerKartablCardEntity(
                            id = card.id,
                            name = card.name,
                            projectId = card.projectId,
                            projectName = card.projectName,
                            boardId = card.boardId,
                            boardName = card.boardName,
                            listId = card.listId,
                            listName = card.listName,
                            dueDate = card.dueDate,
                            attachmentCount = card.attachmentCount,
                            labelNames = if (labelNamesStr.isNotBlank()) labelNamesStr else null,
                            labelColors = if (labelColorsStr.isNotBlank()) labelColorsStr else null
                        )
                    }
                    serverKartablDao.insertCards(entities)
                }
            } catch (_: Exception) {}

            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
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
                    request = CreateCardRequest(name = card.title, type = "project", dueDate = card.dueDate)
                )

                val createdCard = response.body()?.item
                if (response.isSuccessful && createdCard != null) {
                    // Attach labels to created card on Planka
                    val labelIdsList = card.labelIds?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
                    val labelNamesList = card.labelNames?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()

                    for ((idx, labelId) in labelIdsList.withIndex()) {
                        try {
                            val labelName = labelNamesList.getOrNull(idx) ?: "برچسب"
                            var targetLabelId = labelId

                            if (labelId.startsWith("default_")) {
                                // Default offline label -> Create label on Planka board first if boardId available
                                val color = when (labelId) {
                                    "default_red" -> "ruby"
                                    "default_yellow" -> "amber"
                                    "default_green" -> "shamrock"
                                    "default_purple" -> "violet"
                                    else -> "blue-xchange"
                                }
                                val boardId = createdCard.boardId ?: ""
                                if (boardId.isNotEmpty()) {
                                    val createLabelRes = api.createLabel(
                                        boardId = boardId,
                                        authHeader = authHeader,
                                        request = CreateLabelRequest(name = labelName, color = color)
                                    )
                                    if (createLabelRes.isSuccessful && createLabelRes.body()?.item != null) {
                                        targetLabelId = createLabelRes.body()!!.item!!.id
                                    }
                                }
                            }

                            api.addCardLabel(createdCard.id, authHeader, AddCardLabelRequest(targetLabelId))
                        } catch (_: Exception) {
                            // Non-fatal if a label attachment fails
                        }
                    }

                    val syncedCard = card.copy(status = SyncStatus.SYNCED, errorMessage = null)
                    cardDao.updateCard(syncedCard)
                    uploadedCount++
                    kotlinx.coroutines.delay(400)
                    cardDao.deleteCard(syncedCard)
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
