package org.eshragh.nima2.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.eshragh.nima2.data.local.CachedBoardEntity
import coil.ImageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.flow.firstOrNull
import okhttp3.OkHttpClient
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
import org.eshragh.nima2.data.remote.model.extractThumbnailUrl
import org.eshragh.nima2.data.remote.model.extractUrl

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

data class FullListResult(
    val cards: List<ServerKartablCard>,
    val attachments: List<PlankaAttachment>
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

    private var imageLoader: ImageLoader? = null

    fun getImageLoader(): ImageLoader {
        if (imageLoader == null) {
            val logging = okhttp3.logging.HttpLoggingInterceptor().apply {
                level = okhttp3.logging.HttpLoggingInterceptor.Level.HEADERS
            }
            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor { chain ->
                    val original = chain.request()
                    val token = kotlinx.coroutines.runBlocking(Dispatchers.IO) { 
                        userPreferencesRepository.authToken.firstOrNull() 
                    }
                    val requestBuilder = original.newBuilder()
                    if (token != null) {
                        val cleanToken = token.trim()
                        requestBuilder.header("Authorization", "Bearer $cleanToken")
                        requestBuilder.addHeader("Cookie", "accessToken=$cleanToken")
                        
                        val originalUrl = original.url.toString()
                        if (originalUrl.contains("/attachments/")) {
                            val separator = if (originalUrl.contains("?")) "&" else "?"
                            requestBuilder.url("$originalUrl${separator}accessToken=$cleanToken")
                        }
                    }
                    chain.proceed(requestBuilder.build())
                }
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build()

            imageLoader = ImageLoader.Builder(context)
                .okHttpClient(okHttpClient)
                .crossfade(true)
                .build()
        }
        return imageLoader!!
    }

    suspend fun getFullUrl(relativeUrl: String?): String? {
        if (relativeUrl == null) return null
        if (relativeUrl.startsWith("http")) return relativeUrl
        val baseUrl = userPreferencesRepository.serverUrl.firstOrNull() ?: return null
        return "${baseUrl.trimEnd('/')}${if (relativeUrl.startsWith("/")) "" else "/"}$relativeUrl"
    }

    fun getFullUrlSync(relativeUrl: String?): String? {
        if (relativeUrl == null) return null
        if (relativeUrl.startsWith("http")) return relativeUrl
        val baseUrl = kotlinx.coroutines.runBlocking { userPreferencesRepository.serverUrl.firstOrNull() } ?: return null
        val cleanBase = baseUrl.trimEnd('/')
        val cleanRelative = if (relativeUrl.startsWith("/")) relativeUrl else "/$relativeUrl"
        return "$cleanBase$cleanRelative"
    }

    suspend fun downloadFileToTemp(url: String, fileName: String): java.io.File? {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val token = userPreferencesRepository.authToken.firstOrNull()
                val logging = okhttp3.logging.HttpLoggingInterceptor().apply {
                    level = okhttp3.logging.HttpLoggingInterceptor.Level.HEADERS
                }
                
                val okHttpClient = OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .build()
                
                val cleanToken = token?.trim()
                val finalUrl = if (cleanToken != null && !url.contains("accessToken=")) {
                    val separator = if (url.contains("?")) "&" else "?"
                    "$url${separator}accessToken=$cleanToken"
                } else url

                val request = okhttp3.Request.Builder()
                    .url(finalUrl)
                    .apply { 
                        if (cleanToken != null) {
                            header("Authorization", "Bearer $cleanToken")
                            addHeader("Cookie", "accessToken=$cleanToken")
                        }
                    }
                    .build()
                
                okHttpClient.newCall(request).execute().use { response ->
                    android.util.Log.d("NIMA2_NETWORK", "Download Response: ${response.code}")
                    if (response.isSuccessful) {
                        val body = response.body ?: return@withContext null
                        val tempFile = java.io.File(context.cacheDir, "preview_$fileName")
                        body.byteStream().use { input ->
                            tempFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                        tempFile
                    } else {
                        val errorBody = response.body?.string()
                        android.util.Log.e("NIMA2_NETWORK", "Download Failed (${response.code}): $errorBody")
                        null
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("NIMA2_NETWORK", "Download Error: ${e.localizedMessage}")
                null
            }
        }
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
    ): Result<FullListResult> {
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

                Result.success(FullListResult(resultCards, attachments))
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
                            try {
                                if (card.isClosed != true) {
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
                            } catch (e: Exception) {
                                android.util.Log.e("NIMA2_NETWORK", "Error processing card ${card.id}: ${e.localizedMessage}")
                            }
                        }
                    } else {
                        android.util.Log.e("NIMA2_NETWORK", "Board details request failed: ${response.code()}")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("NIMA2_NETWORK", "Error fetching board ${board.id}: ${e.localizedMessage}")
                }
            }

            val result = ServerKartablResult(kartablCards, boardListsMap, boardLabelsMap, boardAttachmentsMap)
            try {
                serverKartablDao.clearAll()
                if (kartablCards.isNotEmpty()) {
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

        var totalUploadedCount = 0
        val api = RetrofitClient.getApi(serverUrl)
        val authHeader = "Bearer $token"

        for (initialCard in pendingCards) {
            // Keep track of the card state during the process to avoid status flickering
            var card = initialCard.copy(status = SyncStatus.UPLOADING)
            try {
                cardDao.updateCard(card)

                var currentRemoteCardId = card.remoteCardId
                
                // 1. Create card if not already created
                if (currentRemoteCardId == null) {
                    val response = api.createCard(
                        listId = card.listId,
                        authHeader = authHeader,
                        request = CreateCardRequest(name = card.title, type = "project", dueDate = card.dueDate)
                    )
                    val createdCard = response.body()?.item
                    if (response.isSuccessful && createdCard != null) {
                        currentRemoteCardId = createdCard.id
                        card = card.copy(remoteCardId = currentRemoteCardId)
                        cardDao.updateCard(card)
                        
                        // Attach labels
                        val labelIdsList = card.labelIds?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
                        for (labelId in labelIdsList) {
                            try {
                                var targetLabelId = labelId
                                if (labelId.startsWith("default_")) {
                                    val color = when (labelId) {
                                        "default_red" -> "ruby"; "default_yellow" -> "amber"
                                        "default_green" -> "shamrock"; "default_purple" -> "violet"
                                        else -> "blue-xchange"
                                    }
                                    val labelName = card.labelNames?.split(",")?.getOrNull(labelIdsList.indexOf(labelId)) ?: "برچسب"
                                    val createLabelRes = api.createLabel(createdCard.boardId ?: "", authHeader, CreateLabelRequest(labelName, color))
                                    if (createLabelRes.isSuccessful) targetLabelId = createLabelRes.body()?.item?.id ?: labelId
                                }
                                api.addCardLabel(currentRemoteCardId, authHeader, AddCardLabelRequest(targetLabelId))
                            } catch (_: Exception) {}
                        }
                    } else {
                        card = card.copy(status = SyncStatus.FAILED, errorMessage = "خطا در ساخت کارت (${response.code()})")
                        cardDao.updateCard(card)
                        continue
                    }
                }

                // 2. Upload attachments one by one
                if (currentRemoteCardId != null) {
                    val paths = card.localAttachmentPaths?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList()
                    val remainingPaths = paths.toMutableList()
                    
                    for (path in paths) {
                        val uploadRes = uploadServerAttachment(serverUrl, token, currentRemoteCardId, path)
                        if (uploadRes.isSuccess) {
                            remainingPaths.remove(path)
                            try { java.io.File(path).delete() } catch (_: Exception) {}
                            
                            // Update state and DB while maintaining UPLOADING status
                            card = card.copy(
                                localAttachmentPaths = remainingPaths.joinToString(","),
                                attachmentCount = remainingPaths.size
                            )
                            cardDao.updateCard(card)
                        } else {
                            android.util.Log.e("NIMA2_SYNC", "Failed to upload $path: ${uploadRes.exceptionOrNull()?.message}")
                        }
                    }

                    if (remainingPaths.isEmpty()) {
                        card = card.copy(status = SyncStatus.SYNCED)
                        cardDao.updateCard(card)
                        totalUploadedCount++
                        // Deletion is now handled by the UI after the success animation
                    } else {
                        card = card.copy(status = SyncStatus.FAILED, errorMessage = "برخی ضمیمه‌ها آپلود نشدند")
                        cardDao.updateCard(card)
                    }
                }
            } catch (e: Exception) {
                card = card.copy(status = SyncStatus.FAILED, errorMessage = e.localizedMessage)
                cardDao.updateCard(card)
            }
        }
        return totalUploadedCount
    }
}
