package org.eshragh.nima2.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.eshragh.nima2.data.local.OfflineCard
import org.eshragh.nima2.data.local.SyncStatus
import org.eshragh.nima2.data.pref.UserPreferencesRepository
import org.eshragh.nima2.data.remote.model.PlankaBoard
import org.eshragh.nima2.data.remote.model.PlankaLabel
import org.eshragh.nima2.data.remote.model.PlankaList
import org.eshragh.nima2.data.remote.model.PlankaProject
import org.eshragh.nima2.data.repository.AuthRepository
import org.eshragh.nima2.data.repository.CardRepository
import org.eshragh.nima2.data.repository.ServerKartablCard
import org.eshragh.nima2.util.ShareManager

class HomeViewModel(
    val cardRepository: CardRepository,
    private val authRepository: AuthRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val offlineCards: StateFlow<List<OfflineCard>> = cardRepository.offlineCards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var allProjects by mutableStateOf<List<PlankaProject>>(emptyList())
        private set
    var allBoards by mutableStateOf<List<PlankaBoard>>(emptyList())
        private set
    var selectedProject by mutableStateOf<PlankaProject?>(null)
        private set
    var selectedBoard by mutableStateOf<PlankaBoard?>(null)
        private set
    var selectedList by mutableStateOf<PlankaList?>(null)
        private set
    var availableLists by mutableStateOf<List<PlankaList>>(emptyList())
        private set
    var availableLabels by mutableStateOf<List<PlankaLabel>>(emptyList())
        private set
    var serverKartablCards by mutableStateOf<List<ServerKartablCard>>(emptyList())
        private set
    var cachedFullListCards by mutableStateOf<List<ServerKartablCard>>(emptyList())
        private set
    var boardLabelsMap by mutableStateOf<Map<String, List<PlankaLabel>>>(emptyMap())
        private set
    var boardListsMap by mutableStateOf<Map<String, List<PlankaList>>>(emptyMap())
        private set
    var boardAttachmentsMap by mutableStateOf<Map<String, List<org.eshragh.nima2.data.remote.model.PlankaAttachment>>>(emptyMap())
        private set
    var isFetchingKartabl by mutableStateOf(false)
        private set
    var selectedLabelIds by mutableStateOf<Set<String>>(emptySet())
        private set

    val availableBoards: List<PlankaBoard> get() = selectedProject?.let { proj -> allBoards.filter { it.projectId == proj.id } } ?: allBoards

    var showProjectPicker by mutableStateOf(false)
    var showBoardPicker by mutableStateOf(false)
    var showListPicker by mutableStateOf(false)
    var showAddCardDialog by mutableStateOf(false)
    var showMultilinePromptDialog by mutableStateOf(false)
    var showDatePickerDialog by mutableStateOf(false)
    var showSettingsDialog by mutableStateOf(false)
    var selectedDueDateISO by mutableStateOf<String?>(null)
    var cardToEdit by mutableStateOf<OfflineCard?>(null)
    var cardToDelete by mutableStateOf<OfflineCard?>(null)
    var serverCardToEditId by mutableStateOf<String?>(null)
    var serverCardToDeleteId by mutableStateOf<String?>(null)
    var selectedAttachmentUris by mutableStateOf<List<android.net.Uri>>(emptyList())
        private set
    var currentCardAttachments by mutableStateOf<List<org.eshragh.nima2.data.remote.model.PlankaAttachment>>(emptyList())
        private set
    var kartablTab by mutableStateOf(0) 
    var defaultKartablTabPreference by mutableStateOf(0)
        private set
    var rightSwipeActionPreference by mutableStateOf(0)
        private set
    var leftSwipeActionPreference by mutableStateOf(1)
        private set
    var quickMoveSelectedProject by mutableStateOf<PlankaProject?>(null)
    var quickMoveSelectedBoard by mutableStateOf<PlankaBoard?>(null)
    var quickMoveSelectedList by mutableStateOf<PlankaList?>(null)
    var viewListSelectedProject by mutableStateOf<PlankaProject?>(null)
        private set
    var viewListSelectedBoard by mutableStateOf<PlankaBoard?>(null)
        private set
    var viewListSelectedList by mutableStateOf<PlankaList?>(null)
        private set
    var viewListCards by mutableStateOf<List<ServerKartablCard>>(emptyList())
        private set
    var isFetchingViewList by mutableStateOf(false)
        private set
    val serverCardToEdit: ServerKartablCard? get() = serverCardToEditId?.let { id -> (serverKartablCards + viewListCards).find { it.id == id } }
    val serverCardToDelete: ServerKartablCard? get() = serverCardToDeleteId?.let { id -> (serverKartablCards + viewListCards).find { it.id == id } }

    var cardTitle by mutableStateOf("")
        private set
    var isFetchingData by mutableStateOf(false)
        private set
    var isFetchingLists by mutableStateOf(false)
        private set
    var isUploading by mutableStateOf(false)
        private set
    var hasLoadedCards by mutableStateOf(true)
        private set
    var offlineCardsState by mutableStateOf<List<OfflineCard>>(emptyList())
        private set

    var searchQuery by mutableStateOf("")
    var lastSyncTimeState by mutableStateOf<String?>(null)
        private set

    // App Update State
    val appUpdateManager = org.eshragh.nima2.util.AppUpdateManager(cardRepository.context)
    var updateData by mutableStateOf<org.eshragh.nima2.data.remote.model.UpdateData?>(null)
        private set
    var updateDownloadState by mutableStateOf<org.eshragh.nima2.util.UpdateDownloadState>(org.eshragh.nima2.util.UpdateDownloadState.Idle)
        private set
    var showUpdateDialog by mutableStateOf(false)
    var isCheckingUpdateState by mutableStateOf(false)
        private set

    var userMessage by mutableStateOf<String?>(null)
        private set

    init {
        android.util.Log.d("NIMA2_SHARE", "HomeViewModel initialized: instance " + hashCode())
        
        viewModelScope.launch {
            userPreferencesRepository.lastSyncTime.collect { time ->
                lastSyncTimeState = time
            }
        }
        
        viewModelScope.launch {
            try {
                android.util.Log.d("NIMA2_DEBUG", "Starting initial cache load...")
                val data = cardRepository.getCachedProjectsAndBoardsSync()
                allProjects = data.projects; allBoards = data.boards
                
                boardListsMap = cardRepository.getCachedListsGroupedByBoard()
                boardLabelsMap = cardRepository.getCachedLabelsGroupedByBoard()
                cachedFullListCards = cardRepository.getAllCachedFullListCardsSync()
                
                android.util.Log.d("NIMA2_DEBUG", "Initial cache loaded: Projects=" + allProjects.size + ", Boards=" + allBoards.size + ", BoardsWithLists=" + boardListsMap.size + ", FullCacheCards=" + cachedFullListCards.size)

                loadInitialDataAndRestoreSelections()
                loadViewListRestoreSelections()
            } catch (e: Exception) {
                android.util.Log.e("NIMA2_DEBUG", "Error loading initial cache: " + e.message)
                loadInitialDataAndRestoreSelections()
                loadViewListRestoreSelections()
            }
        }
        
        viewModelScope.launch { 
            cardRepository.cachedServerCards.collect { 
                android.util.Log.d("NIMA2_DEBUG", "Flow emission: cachedServerCards updated, size = " + it.size)
                serverKartablCards = it 
            } 
        }
        
        viewModelScope.launch { cardRepository.offlineCards.collect { offlineCardsState = it } }
        
        viewModelScope.launch {
            userPreferencesRepository.viewListSelectedTarget.collect { saved ->
                if (viewListSelectedList == null && saved.listId != null) {
                    android.util.Log.d("NIMA2_DEBUG", "Observing viewListSelectedTarget, loading cards for " + saved.listId)
                    cardRepository.getFullListCards(saved.listId, saved.projectId ?: "", saved.projectName ?: "", saved.boardId ?: "", saved.boardName ?: "", saved.listName ?: "").collect { if (viewListCards.isEmpty()) viewListCards = it }
                }
            }
        }
        viewModelScope.launch { userPreferencesRepository.defaultKartablTab.collect { defaultKartablTabPreference = it; if (!hasRestoredTab) { kartablTab = it; hasRestoredTab = true } } }
        viewModelScope.launch { userPreferencesRepository.rightSwipeAction.collect { rightSwipeActionPreference = it } }
        viewModelScope.launch { userPreferencesRepository.leftSwipeAction.collect { leftSwipeActionPreference = it } }
        viewModelScope.launch {
            userPreferencesRepository.quickMoveTarget.collect { saved ->
                quickMoveSelectedProject = allProjects.find { it.id == saved.projectId } ?: if (saved.projectId != null) PlankaProject(saved.projectId, saved.projectName ?: "پروژه") else null
                quickMoveSelectedBoard = allBoards.find { it.id == saved.boardId } ?: if (saved.boardId != null) PlankaBoard(saved.boardId, saved.projectId, saved.boardName ?: "بورد") else null
                quickMoveSelectedList = if (saved.listId != null) PlankaList(saved.listId, saved.boardId ?: "", saved.listName) else null
            }
        }
        
        viewModelScope.launch {
            ShareManager.pendingShare.collect { data ->
                if (data != null) {
                    android.util.Log.d("NIMA2_SHARE", "Collecting shared data on instance " + hashCode())
                    handleIncomingShare(data.text, data.uris)
                    ShareManager.consumeShareData()
                }
            }
        }

        // Automatic silent update check on startup
        checkForUpdate(isManualCheck = false)
    }
    
    private var hasRestoredTab = false

    fun checkForUpdate(isManualCheck: Boolean = false, onUpToDate: (() -> Unit)? = null) {
        viewModelScope.launch {
            if (isManualCheck) isCheckingUpdateState = true
            val res = appUpdateManager.checkUpdate()
            if (isManualCheck) isCheckingUpdateState = false

            res.fold(
                onSuccess = { data ->
                    if (data != null && data.updateAvailable && data.latestVersion != null) {
                        updateData = data
                        updateDownloadState = org.eshragh.nima2.util.UpdateDownloadState.Idle
                        showUpdateDialog = true
                    } else if (isManualCheck) {
                        if (onUpToDate != null) {
                            onUpToDate()
                        } else {
                            userMessage = "شما از آخرین نسخه برنامه استفاده می‌کنید."
                        }
                    }
                },
                onFailure = { err ->
                    if (isManualCheck) {
                        userMessage = "خطا در بررسی به‌روزرسانی: " + err.localizedMessage
                    }
                }
            )
        }
    }

    fun startUpdateDownload() {
        val versionInfo = updateData?.latestVersion ?: return
        viewModelScope.launch {
            appUpdateManager.downloadAndPrepareApk(versionInfo) { state ->
                updateDownloadState = state
            }
        }
    }

    fun installUpdateApk(file: java.io.File) {
        appUpdateManager.installApk(file)
    }

    fun openInstallPermissionSettings() {
        appUpdateManager.openInstallPermissionSettings()
    }

    fun dismissUpdateDialog() {
        showUpdateDialog = false
    }

    fun handleIncomingShare(text: String?, uris: List<android.net.Uri>?) {
        if (text != null && cardTitle.isEmpty()) cardTitle = text
        if (uris != null) {
            val existing = selectedAttachmentUris.toSet()
            selectedAttachmentUris = selectedAttachmentUris + uris.filter { !existing.contains(it) }
        }
        showAddCardDialog = true
    }

    fun consumeIncomingShare() {
        ShareManager.consumeShareData()
    }

    private fun loadViewListRestoreSelections() {
        viewModelScope.launch {
            val saved = userPreferencesRepository.viewListSelectedTarget.firstOrNull() ?: return@launch
            val serverUrl = userPreferencesRepository.serverUrl.firstOrNull() ?: return@launch
            val token = userPreferencesRepository.authToken.firstOrNull() ?: return@launch
            
            android.util.Log.d("NIMA2_DEBUG", "Restoring View List Selection: Board=" + saved.boardId + ", List=" + saved.listId)

            viewListSelectedProject = allProjects.find { it.id == saved.projectId } ?: if (saved.projectId != null) PlankaProject(saved.projectId, saved.projectName ?: "پروژه") else null
            viewListSelectedBoard = allBoards.find { it.id == saved.boardId } ?: if (saved.boardId != null) PlankaBoard(saved.boardId, saved.projectId, saved.boardName ?: "بورد") else null

            if (saved.boardId != null) {
                if (!boardListsMap.containsKey(saved.boardId)) {
                    android.util.Log.d("NIMA2_DEBUG", "Lists not in map for board " + saved.boardId + ", fetching...")
                    fetchListsForBoardId(saved.boardId)
                }
                
                if (saved.listId != null) {
                    viewListSelectedList = boardListsMap[saved.boardId]?.find { it.id == saved.listId } ?: PlankaList(saved.listId, saved.boardId ?: "", saved.listName)
                    loadFullListCards(saved.listId)
                }
            }
        }
    }

    fun loadFullListCards(listId: String) {
        val proj = viewListSelectedProject ?: return; val board = viewListSelectedBoard ?: return; val listName = viewListSelectedList?.name ?: "لیست"
        viewModelScope.launch {
            val serverUrl = userPreferencesRepository.serverUrl.firstOrNull() ?: return@launch
            val token = userPreferencesRepository.authToken.firstOrNull() ?: return@launch
            android.util.Log.d("NIMA2_DEBUG", "Loading cards for list: " + listId + " (" + listName + ")")
            isFetchingViewList = true
            val res = cardRepository.fetchFullListCards(serverUrl, token, proj.id, proj.name, board.id, board.name, listId, listName)
            isFetchingViewList = false
            res.fold(onSuccess = { result ->
                android.util.Log.d("NIMA2_DEBUG", "Successfully loaded " + result.cards.size + " cards for list: " + listId)
                viewListCards = result.cards
                val updatedMap = boardAttachmentsMap.toMutableMap()
                updatedMap[board.id] = result.attachments
                boardAttachmentsMap = updatedMap
                updateSyncTime()
                
                viewModelScope.launch {
                    cachedFullListCards = cardRepository.getAllCachedFullListCardsSync()
                }
            }, onFailure = { e ->
                android.util.Log.e("NIMA2_DEBUG", "Failed to load cards for list " + listId + ": " + e.message)
            })
        }
    }

    fun selectViewListProject(project: PlankaProject) { viewListSelectedProject = project; viewListSelectedBoard = null; viewListSelectedList = null; viewListCards = emptyList(); persistViewListSelection(project, null, null) }
    fun selectViewListBoard(board: PlankaBoard) { viewListSelectedBoard = board; viewListSelectedList = null; viewListCards = emptyList(); persistViewListSelection(viewListSelectedProject, board, null); fetchListsForBoardId(board.id) }
    fun selectViewList(list: PlankaList) { viewListSelectedList = list; persistViewListSelection(viewListSelectedProject, viewListSelectedBoard, list); loadFullListCards(list.id) }
    fun saveDefaultKartablTab(tabIndex: Int) { viewModelScope.launch { userPreferencesRepository.saveDefaultKartablTab(tabIndex) } }
    fun saveSwipeActions(right: Int, left: Int) { viewModelScope.launch { userPreferencesRepository.saveSwipeActions(right, left) } }
    fun saveQuickMoveTarget(proj: PlankaProject?, board: PlankaBoard?, list: PlankaList?) { viewModelScope.launch { userPreferencesRepository.saveQuickMoveTarget(proj?.id, proj?.name, board?.id, board?.name, list?.id, list?.name) } }
    private fun persistViewListSelection(proj: PlankaProject?, board: PlankaBoard?, list: PlankaList?) { viewModelScope.launch { userPreferencesRepository.saveViewListSelectedTarget(proj?.id, proj?.name, board?.id, board?.name, list?.id, list?.name) } }
    fun onCardTitleChange(title: String) { cardTitle = title }

    fun loadInitialDataAndRestoreSelections() {
        viewModelScope.launch {
            val serverUrl = userPreferencesRepository.serverUrl.firstOrNull() ?: return@launch
            val token = userPreferencesRepository.authToken.firstOrNull() ?: return@launch
            isFetchingData = true; val res = cardRepository.fetchProjectsAndBoards(serverUrl, token); isFetchingData = false
            res.fold(onSuccess = { data ->
                allProjects = data.projects; allBoards = data.boards
                val saved = userPreferencesRepository.selectedTarget.firstOrNull()
                val proj = data.projects.find { it.id == saved?.projectId } ?: data.projects.firstOrNull()
                selectedProject = proj
                val boardsForProj = proj?.let { p -> data.boards.filter { it.projectId == p.id } } ?: data.boards
                val board = boardsForProj.find { it.id == saved?.boardId } ?: boardsForProj.firstOrNull()
                selectedBoard = board
                if (board != null) loadListsForBoard(serverUrl, token, board.id, saved?.listId)
                loadServerKartablCards()
            }, onFailure = { err -> userMessage = "خطا در دریافت پروژه‌ها: " + err.localizedMessage })
        }
    }

    fun fetchListsForBoardId(boardId: String) {
        viewModelScope.launch {
            val serverUrl = userPreferencesRepository.serverUrl.firstOrNull() ?: return@launch
            val token = userPreferencesRepository.authToken.firstOrNull() ?: return@launch
            android.util.Log.d("NIMA2_DEBUG", "Fetching lists for board: " + boardId)
            val res = cardRepository.fetchBoardDetailsContent(serverUrl, token, boardId)
            res.fold(onSuccess = { content ->
                android.util.Log.d("NIMA2_DEBUG", "Successfully fetched " + content.lists.size + " lists for board: " + boardId)
                val updatedListsMap = boardListsMap.toMutableMap(); updatedListsMap[boardId] = content.lists; boardListsMap = updatedListsMap
                val updatedLabelsMap = boardLabelsMap.toMutableMap(); updatedLabelsMap[boardId] = content.labels; boardLabelsMap = updatedLabelsMap
            }, onFailure = { e ->
                android.util.Log.e("NIMA2_DEBUG", "Failed to fetch lists for board " + boardId + ": " + e.message)
            })
        }
    }

    private suspend fun loadListsForBoard(serverUrl: String, token: String, boardId: String, preferredListId: String? = null) {
        isFetchingLists = true; val res = cardRepository.fetchBoardDetailsContent(serverUrl, token, boardId); isFetchingLists = false
        res.fold(onSuccess = { content ->
            availableLists = content.lists; availableLabels = content.labels
            val updatedListsMap = boardListsMap.toMutableMap(); updatedListsMap[boardId] = content.lists; boardListsMap = updatedListsMap
            val updatedLabelsMap = boardLabelsMap.toMutableMap(); updatedLabelsMap[boardId] = content.labels; boardLabelsMap = updatedLabelsMap
            val list = content.lists.find { it.id == preferredListId } ?: content.lists.firstOrNull()
            selectedList = list; persistSelection(selectedProject, selectedBoard, list)
        }, onFailure = { err -> availableLists = emptyList(); availableLabels = emptyList(); selectedList = null; userMessage = "خطا در دریافت لیست‌های بورد: " + err.localizedMessage })
    }

    fun selectProject(project: PlankaProject) {
        selectedProject = project; showProjectPicker = false; val board = allBoards.filter { it.projectId == project.id }.firstOrNull(); selectedBoard = board; selectedList = null; availableLists = emptyList()
        if (board != null) { viewModelScope.launch { val serverUrl = userPreferencesRepository.serverUrl.firstOrNull() ?: return@launch; val token = userPreferencesRepository.authToken.firstOrNull() ?: return@launch; loadListsForBoard(serverUrl, token, board.id) } } else persistSelection(project, null, null)
    }

    fun selectBoard(board: PlankaBoard) {
        selectedBoard = board; showBoardPicker = false; selectedList = null; availableLists = emptyList()
        viewModelScope.launch { val serverUrl = userPreferencesRepository.serverUrl.firstOrNull() ?: return@launch; val token = userPreferencesRepository.authToken.firstOrNull() ?: return@launch; loadListsForBoard(serverUrl, token, board.id) }
    }

    fun selectList(list: PlankaList) { selectedList = list; showListPicker = false; persistSelection(selectedProject, selectedBoard, list) }
    private fun persistSelection(proj: PlankaProject?, board: PlankaBoard?, list: PlankaList?) { viewModelScope.launch { userPreferencesRepository.saveSelectedTarget(proj?.id, proj?.name, board?.id, board?.name, list?.id, list?.name) } }
    fun toggleLabelSelection(labelId: String) { selectedLabelIds = if (selectedLabelIds.contains(labelId)) selectedLabelIds - labelId else selectedLabelIds + labelId }
    fun clearSelectedLabels() { selectedLabelIds = emptySet() }
    fun addSelectedAttachments(uris: List<android.net.Uri>) { selectedAttachmentUris = selectedAttachmentUris + uris }
    fun removeSelectedAttachment(uri: android.net.Uri) { selectedAttachmentUris = selectedAttachmentUris - uri }

    fun openServerFile(url: String, fileName: String, context: android.content.Context) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { android.widget.Toast.makeText(context, "در حال آماده‌سازی فایل...", android.widget.Toast.LENGTH_SHORT).show() }
                val fullUrl = cardRepository.getFullUrl(url) ?: return@launch
                val file = cardRepository.downloadFileToTemp(fullUrl, fileName) ?: return@launch
                val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply { setDataAndType(uri, context.contentResolver.getType(uri) ?: "*/*"); addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION); addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK) }
                context.startActivity(intent)
            } catch (_: Exception) {}
        }
    }

    val multilineLines: List<String> get() = cardTitle.trim().split("\n").map { it.trim() }.filter { it.isNotEmpty() }

    fun addCard() {
        if (cardTitle.trim().isEmpty()) { userMessage = "لطفاً عنوان کارت را وارد کنید"; return }
        if (selectedList == null) { userMessage = "لطفاً ابتدا پروژه، بورد و لیست مقصد را انتخاب کنید"; return }
        if (multilineLines.size > 1) showMultilinePromptDialog = true else saveOfflineCardsBatch(splitPerLine = false)
    }

    fun saveOfflineCardsBatch(splitPerLine: Boolean) {
        val targetList = selectedList ?: return; val projName = selectedProject?.name ?: "پروژه"; val boardName = selectedBoard?.name ?: "بورد"; val listName = targetList.name ?: "لیست"; val fullPathName = projName + " ← " + boardName + " ← " + listName
        val labelIdsStr = if (selectedLabelIds.isNotEmpty()) selectedLabelIds.joinToString(",") else null
        val selectedLabelsList = availableLabels.filter { selectedLabelIds.contains(it.id) }
        val labelNamesStr = if (selectedLabelsList.isNotEmpty()) selectedLabelsList.map { it.name ?: it.color ?: "برچسب" }.joinToString(",") else null
        val labelColorsStr = if (selectedLabelsList.isNotEmpty()) selectedLabelsList.map { it.color ?: "blue-xchange" }.joinToString(",") else null
        val linesToSave = if (splitPerLine) multilineLines else listOf(cardTitle.trim())
        viewModelScope.launch {
            val localPaths = selectedAttachmentUris.mapNotNull { cardRepository.saveLocalFile(it) }; val localPathsStr = if (localPaths.isNotEmpty()) localPaths.joinToString(",") else null
            linesToSave.forEachIndexed { index, line -> val paths = if (index == 0) localPathsStr else null; val count = if (index == 0) localPaths.size else 0; cardRepository.addOfflineCard(line, targetList.id, fullPathName, labelIdsStr, labelNamesStr, labelColorsStr, selectedDueDateISO, paths, count) }
            cardTitle = ""; selectedLabelIds = emptySet(); selectedDueDateISO = null; selectedAttachmentUris = emptyList(); showAddCardDialog = false; showMultilinePromptDialog = false; userMessage = if (linesToSave.size > 1) linesToSave.size.toString() + " کارت ذخیره شد" else "کارت ذخیره شد"
        }
    }

    fun uploadCards() { cardRepository.triggerSync(); userMessage = "فرآیند آپلود آغاز شد" }
    fun loadServerKartablCards() {
        viewModelScope.launch {
            val serverUrl = userPreferencesRepository.serverUrl.firstOrNull() ?: return@launch
            val token = userPreferencesRepository.authToken.firstOrNull() ?: return@launch
            isFetchingKartabl = true
            val res = cardRepository.fetchAllServerKartablCards(serverUrl, token, allProjects, allBoards)
            isFetchingKartabl = false
            res.fold(
                onSuccess = { result ->
                    serverKartablCards = result.cards
                    boardListsMap = result.boardListsMap
                    boardLabelsMap = result.boardLabelsMap
                    boardAttachmentsMap = result.boardAttachmentsMap
                    updateSyncTime()
                    
                    viewModelScope.launch {
                        cachedFullListCards = cardRepository.getAllCachedFullListCardsSync()
                    }
                },
                onFailure = {}
            )
        }
    }

    private fun updateSyncTime() {
        val now = java.util.Calendar.getInstance()
        val timeStr = String.format("%02d:%02d", now.get(java.util.Calendar.HOUR_OF_DAY), now.get(java.util.Calendar.MINUTE))
        viewModelScope.launch {
            userPreferencesRepository.saveLastSyncTime(timeStr)
        }
    }
    fun deleteCard(card: OfflineCard) { viewModelScope.launch { cardRepository.deleteOfflineCard(card) } }
    fun deleteServerCard(card: ServerKartablCard) { viewModelScope.launch { val serverUrl = userPreferencesRepository.serverUrl.firstOrNull() ?: return@launch; val token = userPreferencesRepository.authToken.firstOrNull() ?: return@launch; val res = cardRepository.deleteServerCard(serverUrl, token, card.id); serverCardToDeleteId = null; res.fold(onSuccess = { userMessage = "کارت حذف شد"; serverKartablCards = serverKartablCards.filter { it.id != card.id }; viewListCards = viewListCards.filter { it.id != card.id } }, onFailure = {}) } }
    fun updateServerCard(card: ServerKartablCard, newTitle: String, newListId: String? = null, newListName: String? = null, newBoardId: String? = null, newBoardName: String? = null, newProjectId: String? = null, newProjectName: String? = null, newDueDateISO: String?, newLabelIds: Set<String> = emptySet()) { viewModelScope.launch { val serverUrl = userPreferencesRepository.serverUrl.firstOrNull() ?: return@launch; val token = userPreferencesRepository.authToken.firstOrNull() ?: return@launch; val res = cardRepository.updateServerCard(serverUrl, token, card.id, newTitle, newBoardId, newListId, newDueDateISO); serverCardToEditId = null; res.fold(onSuccess = { updated -> userMessage = "کارت به‌روزرسانی شد"; val updateMapper: (ServerKartablCard) -> ServerKartablCard = { current -> if (current.id == card.id) current.copy(name = updated.name, projectId = newProjectId ?: current.projectId, projectName = newProjectName ?: current.projectName, boardId = newBoardId ?: current.boardId, boardName = newBoardName ?: current.boardName, listId = updated.listId, listName = newListName ?: current.listName, dueDate = updated.dueDate) else current }; serverKartablCards = serverKartablCards.map(updateMapper); viewListCards = viewListCards.map(updateMapper).filter { it.listId == viewListSelectedList?.id } }, onFailure = {}) } }
    fun updateCardFull(card: OfflineCard, newTitle: String, newListId: String? = null, newListName: String? = null, newLabelIds: Set<String>, newDueDateISO: String?) { val trimmed = newTitle.trim(); if (trimmed.isEmpty()) { userMessage = "عنوان خالی نباشد"; return }; val labelIdsStr = if (newLabelIds.isNotEmpty()) newLabelIds.joinToString(",") else null; val selectedLabelsList = availableLabels.filter { newLabelIds.contains(it.id) }; val labelNamesStr = if (selectedLabelsList.isNotEmpty()) selectedLabelsList.map { it.name ?: it.color ?: "برچسب" }.joinToString(",") else null; val labelColorsStr = if (selectedLabelsList.isNotEmpty()) selectedLabelsList.map { it.color ?: "blue-xchange" }.joinToString(",") else null; viewModelScope.launch { cardRepository.updateCardFull(card, trimmed, newListId, newListName, labelIdsStr, labelNamesStr, labelColorsStr, newDueDateISO); cardToEdit = null; userMessage = "کارت به‌روزرسانی شد" } }
    fun addServerAttachment(cardId: String, uri: android.net.Uri) { viewModelScope.launch { val serverUrl = userPreferencesRepository.serverUrl.firstOrNull() ?: return@launch; val token = userPreferencesRepository.authToken.firstOrNull() ?: return@launch; val localPath = cardRepository.saveLocalFile(uri) ?: return@launch; val res = cardRepository.uploadServerAttachment(serverUrl, token, cardId, localPath); res.fold(onSuccess = { userMessage = "فایل آپلود شد"; loadServerKartablCards(); viewListSelectedList?.let { loadFullListCards(it.id) } }, onFailure = {}) } }
    fun deleteServerAttachment(attachmentId: String) { viewModelScope.launch { val serverUrl = userPreferencesRepository.serverUrl.firstOrNull() ?: return@launch; val token = userPreferencesRepository.authToken.firstOrNull() ?: return@launch; val res = cardRepository.deleteServerAttachment(serverUrl, token, attachmentId); res.fold(onSuccess = { userMessage = "فایل حذف شد"; loadServerKartablCards(); viewListSelectedList?.let { loadFullListCards(it.id) } }, onFailure = {}) } }
    fun clearSynced() { viewModelScope.launch { cardRepository.clearSyncedCards(); userMessage = "پاکسازی شد" } }
    fun dismissMessage() { userMessage = null }
    fun logout(onLoggedOut: () -> Unit) { viewModelScope.launch { authRepository.logout(); onLoggedOut() } }
    fun quickMoveServerCard(card: ServerKartablCard) { viewModelScope.launch { val serverUrl = userPreferencesRepository.serverUrl.firstOrNull() ?: return@launch; val token = userPreferencesRepository.authToken.firstOrNull() ?: return@launch; val target = userPreferencesRepository.quickMoveTarget.firstOrNull() ?: return@launch; if (target.listId == null) { userMessage = "لیست مقصد تنظیم نشده"; return@launch }; val res = cardRepository.updateServerCard(serverUrl, token, card.id, card.name, target.boardId, target.listId, card.dueDate); res.fold(onSuccess = { updated -> userMessage = "کارت منتقل شد"; val updateMapper: (ServerKartablCard) -> ServerKartablCard = { current -> if (current.id == card.id) current.copy(projectId = target.projectId ?: current.projectId, projectName = target.projectName ?: current.projectName, boardId = target.boardId ?: current.boardId, boardName = target.boardName ?: current.boardName, listId = updated.listId, listName = target.listName ?: current.listName) else current }; serverKartablCards = serverKartablCards.map(updateMapper); viewListCards = viewListCards.map(updateMapper).filter { it.listId == viewListSelectedList?.id } }, onFailure = {}) } }

    override fun onCleared() {
        super.onCleared()
        android.util.Log.d("NIMA2_SHARE", "HomeViewModel onCleared: instance " + hashCode())
    }

    class Factory(
        private val cardRepository: CardRepository,
        private val authRepository: AuthRepository,
        private val userPreferencesRepository: UserPreferencesRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(cardRepository, authRepository, userPreferencesRepository) as T
        }
    }
}
