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
import org.eshragh.nima2.data.pref.UserPreferencesRepository
import org.eshragh.nima2.data.remote.model.PlankaBoard
import org.eshragh.nima2.data.remote.model.PlankaLabel
import org.eshragh.nima2.data.remote.model.PlankaList
import org.eshragh.nima2.data.remote.model.PlankaProject
import org.eshragh.nima2.data.repository.AuthRepository
import org.eshragh.nima2.data.repository.CardRepository
import org.eshragh.nima2.data.repository.ServerKartablCard

class HomeViewModel(
    private val cardRepository: CardRepository,
    private val authRepository: AuthRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val offlineCards: StateFlow<List<OfflineCard>> = cardRepository.offlineCards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All Projects and Boards loaded from server
    var allProjects by mutableStateOf<List<PlankaProject>>(
        try {
            kotlinx.coroutines.runBlocking(kotlinx.coroutines.Dispatchers.IO) {
                cardRepository.getCachedProjectsAndBoardsSync().projects
            }
        } catch (_: Exception) { emptyList() }
    )
        private set

    var allBoards by mutableStateOf<List<PlankaBoard>>(
        try {
            kotlinx.coroutines.runBlocking(kotlinx.coroutines.Dispatchers.IO) {
                cardRepository.getCachedProjectsAndBoardsSync().boards
            }
        } catch (_: Exception) { emptyList() }
    )
        private set

    // Active Selections
    var selectedProject by mutableStateOf<PlankaProject?>(null)
        private set

    var selectedBoard by mutableStateOf<PlankaBoard?>(null)
        private set

    var selectedList by mutableStateOf<PlankaList?>(null)
        private set

    // Lists and Labels available for the currently selected board
    var availableLists by mutableStateOf<List<PlankaList>>(emptyList())
        private set

    var availableLabels by mutableStateOf<List<PlankaLabel>>(emptyList())
        private set

    var serverKartablCards by mutableStateOf<List<ServerKartablCard>>(
        try {
            kotlinx.coroutines.runBlocking(kotlinx.coroutines.Dispatchers.IO) {
                cardRepository.getCachedServerCardsSync()
            }
        } catch (_: Exception) {
            emptyList()
        }
    )
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

    // Filtered boards for currently selected project
    val availableBoards: List<PlankaBoard>
        get() = selectedProject?.let { proj ->
            allBoards.filter { it.projectId == proj.id }
        } ?: allBoards

    // UI Picker Dialog States
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

    // Attachments State
    var selectedAttachmentUris by mutableStateOf<List<android.net.Uri>>(emptyList())
        private set

    var currentCardAttachments by mutableStateOf<List<org.eshragh.nima2.data.remote.model.PlankaAttachment>>(emptyList())
        private set

    // View List Tab State
    var kartablTab by mutableStateOf(0) // 0: Due Dates, 1: View List

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

    val serverCardToEdit: ServerKartablCard?
        get() = serverCardToEditId?.let { id ->
            val listToSearch = if (kartablTab == 0) (serverKartablCards + viewListCards) else (viewListCards + serverKartablCards)
            listToSearch.find { it.id == id }
        }

    val serverCardToDelete: ServerKartablCard?
        get() = serverCardToDeleteId?.let { id ->
            val listToSearch = if (kartablTab == 0) (serverKartablCards + viewListCards) else (viewListCards + serverKartablCards)
            listToSearch.find { it.id == id }
        }

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

    var offlineCardsState by mutableStateOf<List<OfflineCard>>(
        try {
            kotlinx.coroutines.runBlocking(kotlinx.coroutines.Dispatchers.IO) {
                cardRepository.getAllCardsSync()
            }
        } catch (_: Exception) {
            emptyList()
        }
    )
        private set

    var userMessage by mutableStateOf<String?>(null)
        private set

    init {
        viewModelScope.launch {
            cardRepository.cachedServerCards.collect { cached ->
                android.util.Log.d("NIMA2_KARTABL_DEBUG", "HomeViewModel collected cachedServerCards count: ${cached.size}")
                serverKartablCards = cached
            }
        }
        viewModelScope.launch {
            cardRepository.offlineCards.collect { cards ->
                offlineCardsState = cards
                hasLoadedCards = true
            }
        }
        // Observe and load View List from cache
        viewModelScope.launch {
            userPreferencesRepository.viewListSelectedTarget.collect { saved ->
                if (viewListSelectedList == null && saved.listId != null) {
                    cardRepository.getFullListCards(
                        listId = saved.listId,
                        projectId = saved.projectId ?: "",
                        projectName = saved.projectName ?: "",
                        boardId = saved.boardId ?: "",
                        boardName = saved.boardName ?: "",
                        listName = saved.listName ?: ""
                    ).collect { cached ->
                        if (viewListCards.isEmpty()) {
                            viewListCards = cached
                        }
                    }
                }
            }
        }
        // Observe default tab preference
        viewModelScope.launch {
            userPreferencesRepository.defaultKartablTab.collect { tab ->
                defaultKartablTabPreference = tab
                // Only set kartablTab if it's the first load
                if (!hasRestoredTab) {
                    kartablTab = tab
                    hasRestoredTab = true
                }
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.rightSwipeAction.collect { rightSwipeActionPreference = it }
        }
        viewModelScope.launch {
            userPreferencesRepository.leftSwipeAction.collect { leftSwipeActionPreference = it }
        }
        viewModelScope.launch {
            userPreferencesRepository.quickMoveTarget.collect { saved ->
                quickMoveSelectedProject = allProjects.find { it.id == saved.projectId }
                    ?: if (saved.projectId != null) PlankaProject(saved.projectId, saved.projectName ?: "پروژه") else null
                quickMoveSelectedBoard = allBoards.find { it.id == saved.boardId }
                    ?: if (saved.boardId != null) PlankaBoard(saved.boardId, saved.projectId, saved.boardName ?: "بورد") else null
                quickMoveSelectedList = if (saved.listId != null) PlankaList(saved.listId, saved.boardId ?: "", saved.listName) else null
            }
        }
        loadInitialDataAndRestoreSelections()
        loadViewListRestoreSelections()
    }
    
    private var hasRestoredTab = false

    private fun loadViewListRestoreSelections() {
        viewModelScope.launch {
            val saved = userPreferencesRepository.viewListSelectedTarget.firstOrNull() ?: return@launch
            val serverUrl = userPreferencesRepository.serverUrl.firstOrNull() ?: return@launch
            val token = userPreferencesRepository.authToken.firstOrNull() ?: return@launch

            // Try to find in allProjects/allBoards once they are loaded, or reconstruct from saved data
            viewListSelectedProject = allProjects.find { it.id == saved.projectId }
                ?: if (saved.projectId != null) PlankaProject(saved.projectId, saved.projectName ?: "پروژه") else null
            
            viewListSelectedBoard = allBoards.find { it.id == saved.boardId }
                ?: if (saved.boardId != null) PlankaBoard(saved.boardId, saved.projectId, saved.boardName ?: "بورد") else null

            if (saved.listId != null) {
                viewListSelectedList = boardListsMap[saved.boardId]?.find { it.id == saved.listId }
                        ?: PlankaList(saved.listId, saved.boardId ?: "", saved.listName)
                
                // If we don't have lists for this board yet, fetch them
                if (saved.boardId != null && !boardListsMap.containsKey(saved.boardId)) {
                    fetchListsForBoardId(saved.boardId)
                }
                
                loadFullListCards(saved.listId)
            }
        }
    }

    fun loadFullListCards(listId: String) {
        val proj = viewListSelectedProject ?: return
        val board = viewListSelectedBoard ?: return
        val listName = viewListSelectedList?.name ?: "لیست"
        
        viewModelScope.launch {
            val serverUrl = userPreferencesRepository.serverUrl.firstOrNull() ?: return@launch
            val token = userPreferencesRepository.authToken.firstOrNull() ?: return@launch

            isFetchingViewList = true
            val res = cardRepository.fetchFullListCards(
                serverUrl = serverUrl,
                token = token,
                projectId = proj.id,
                projectName = proj.name,
                boardId = board.id,
                boardName = board.name,
                listId = listId,
                listName = listName
            )
            isFetchingViewList = false

            res.fold(
                onSuccess = { cards ->
                    viewListCards = cards
                },
                onFailure = { err ->
                    android.util.Log.e("NIMA2_DEBUG", "Failed to fetch full list: ${err.localizedMessage}")
                }
            )
        }
    }

    fun selectViewListProject(project: PlankaProject) {
        viewListSelectedProject = project
        viewListSelectedBoard = null
        viewListSelectedList = null
        viewListCards = emptyList()
        persistViewListSelection(project, null, null)
    }

    fun selectViewListBoard(board: PlankaBoard) {
        viewListSelectedBoard = board
        viewListSelectedList = null
        viewListCards = emptyList()
        persistViewListSelection(viewListSelectedProject, board, null)
        fetchListsForBoardId(board.id)
    }

    fun selectViewList(list: PlankaList) {
        viewListSelectedList = list
        persistViewListSelection(viewListSelectedProject, viewListSelectedBoard, list)
        loadFullListCards(list.id)
    }

    fun saveDefaultKartablTab(tabIndex: Int) {
        viewModelScope.launch {
            userPreferencesRepository.saveDefaultKartablTab(tabIndex)
        }
    }

    fun saveSwipeActions(right: Int, left: Int) {
        viewModelScope.launch {
            userPreferencesRepository.saveSwipeActions(right, left)
        }
    }

    fun saveQuickMoveTarget(proj: PlankaProject?, board: PlankaBoard?, list: PlankaList?) {
        viewModelScope.launch {
            userPreferencesRepository.saveQuickMoveTarget(
                projectId = proj?.id,
                projectName = proj?.name,
                boardId = board?.id,
                boardName = board?.name,
                listId = list?.id,
                listName = list?.name
            )
        }
    }

    private fun persistViewListSelection(proj: PlankaProject?, board: PlankaBoard?, list: PlankaList?) {
        viewModelScope.launch {
            userPreferencesRepository.saveViewListSelectedTarget(
                projectId = proj?.id,
                projectName = proj?.name,
                boardId = board?.id,
                boardName = board?.name,
                listId = list?.id,
                listName = list?.name
            )
        }
    }

    fun onCardTitleChange(title: String) {
        cardTitle = title
    }

    fun loadInitialDataAndRestoreSelections() {
        viewModelScope.launch {
            val serverUrl = userPreferencesRepository.serverUrl.firstOrNull() ?: return@launch
            val token = userPreferencesRepository.authToken.firstOrNull() ?: return@launch

            isFetchingData = true
            val res = cardRepository.fetchProjectsAndBoards(serverUrl, token)
            isFetchingData = false

            res.fold(
                onSuccess = { data ->
                    allProjects = data.projects
                    allBoards = data.boards

                    // Restore saved selections from DataStore if available
                    val saved = userPreferencesRepository.selectedTarget.firstOrNull()

                    val proj = data.projects.find { it.id == saved?.projectId } ?: data.projects.firstOrNull()
                    selectedProject = proj

                    val boardsForProj = proj?.let { p -> data.boards.filter { it.projectId == p.id } } ?: data.boards
                    val board = boardsForProj.find { it.id == saved?.boardId } ?: boardsForProj.firstOrNull()
                    selectedBoard = board

                    if (board != null) {
                        loadListsForBoard(serverUrl, token, board.id, saved?.listId)
                    }

                    // Load global server Kartabl cards across all boards
                    loadServerKartablCards()
                },
                onFailure = { err ->
                    userMessage = "خطا در دریافت پروژه‌ها: ${err.localizedMessage}"
                }
            )
        }
    }

    fun fetchListsForBoardId(boardId: String) {
        viewModelScope.launch {
            val serverUrl = userPreferencesRepository.serverUrl.firstOrNull() ?: return@launch
            val token = userPreferencesRepository.authToken.firstOrNull() ?: return@launch

            val res = cardRepository.fetchBoardDetailsContent(serverUrl, token, boardId)
            res.fold(
                onSuccess = { content ->
                    val updatedListsMap = boardListsMap.toMutableMap()
                    updatedListsMap[boardId] = content.lists
                    boardListsMap = updatedListsMap

                    val updatedLabelsMap = boardLabelsMap.toMutableMap()
                    updatedLabelsMap[boardId] = content.labels
                    boardLabelsMap = updatedLabelsMap
                },
                onFailure = { err ->
                    android.util.Log.e("NIMA2_DEBUG", "Failed to fetch lists for board $boardId: ${err.localizedMessage}")
                }
            )
        }
    }

    private suspend fun loadListsForBoard(serverUrl: String, token: String, boardId: String, preferredListId: String? = null) {
        isFetchingLists = true
        val res = cardRepository.fetchBoardDetailsContent(serverUrl, token, boardId)
        isFetchingLists = false

        res.fold(
            onSuccess = { content ->
                availableLists = content.lists
                availableLabels = content.labels

                val updatedListsMap = boardListsMap.toMutableMap()
                updatedListsMap[boardId] = content.lists
                boardListsMap = updatedListsMap

                val updatedLabelsMap = boardLabelsMap.toMutableMap()
                updatedLabelsMap[boardId] = content.labels
                boardLabelsMap = updatedLabelsMap

                val list = content.lists.find { it.id == preferredListId } ?: content.lists.firstOrNull()
                selectedList = list
                persistSelection(selectedProject, selectedBoard, list)
            },
            onFailure = { err ->
                availableLists = emptyList()
                availableLabels = emptyList()
                selectedList = null
                userMessage = "خطا در دریافت لیست‌های بورد: ${err.localizedMessage}"
            }
        )
    }

    fun selectProject(project: PlankaProject) {
        selectedProject = project
        showProjectPicker = false

        // Filter boards for new project and select first
        val matchingBoards = allBoards.filter { it.projectId == project.id }
        val board = matchingBoards.firstOrNull()
        selectedBoard = board
        selectedList = null
        availableLists = emptyList()

        if (board != null) {
            viewModelScope.launch {
                val serverUrl = userPreferencesRepository.serverUrl.firstOrNull() ?: return@launch
                val token = userPreferencesRepository.authToken.firstOrNull() ?: return@launch
                loadListsForBoard(serverUrl, token, board.id)
            }
        } else {
            persistSelection(project, null, null)
        }
    }

    fun selectBoard(board: PlankaBoard) {
        selectedBoard = board
        showBoardPicker = false
        selectedList = null
        availableLists = emptyList()

        viewModelScope.launch {
            val serverUrl = userPreferencesRepository.serverUrl.firstOrNull() ?: return@launch
            val token = userPreferencesRepository.authToken.firstOrNull() ?: return@launch
            loadListsForBoard(serverUrl, token, board.id)
        }
    }

    fun selectList(list: PlankaList) {
        selectedList = list
        showListPicker = false
        persistSelection(selectedProject, selectedBoard, list)
    }

    private fun persistSelection(proj: PlankaProject?, board: PlankaBoard?, list: PlankaList?) {
        viewModelScope.launch {
            userPreferencesRepository.saveSelectedTarget(
                projectId = proj?.id,
                projectName = proj?.name,
                boardId = board?.id,
                boardName = board?.name,
                listId = list?.id,
                listName = list?.name
            )
        }
    }

    fun toggleLabelSelection(labelId: String) {
        selectedLabelIds = if (selectedLabelIds.contains(labelId)) {
            selectedLabelIds - labelId
        } else {
            selectedLabelIds + labelId
        }
    }

    fun clearSelectedLabels() {
        selectedLabelIds = emptySet()
    }

    fun addSelectedAttachments(uris: List<android.net.Uri>) {
        selectedAttachmentUris = selectedAttachmentUris + uris
    }

    fun removeSelectedAttachment(uri: android.net.Uri) {
        selectedAttachmentUris = selectedAttachmentUris - uri
    }

    val multilineLines: List<String>
        get() = cardTitle.trim().split("\n").map { it.trim() }.filter { it.isNotEmpty() }

    fun addCard() {
        val title = cardTitle.trim()
        if (title.isEmpty()) {
            userMessage = "لطفاً عنوان کارت را وارد کنید"
            return
        }

        val targetList = selectedList
        if (targetList == null) {
            userMessage = "لطفاً ابتدا پروژه، بورد و لیست مقصد را انتخاب کنید"
            return
        }

        if (multilineLines.size > 1) {
            showMultilinePromptDialog = true
        } else {
            saveOfflineCardsBatch(splitPerLine = false)
        }
    }

    fun saveOfflineCardsBatch(splitPerLine: Boolean) {
        val targetList = selectedList ?: return
        val fullPathName = "${selectedProject?.name ?: "پروژه"} ← ${selectedBoard?.name ?: "بورد"} ← ${targetList.name ?: "لیست"}"

        val labelIdsStr = if (selectedLabelIds.isNotEmpty()) selectedLabelIds.joinToString(",") else null
        val selectedLabelsList = availableLabels.filter { selectedLabelIds.contains(it.id) }
        val labelNamesStr = if (selectedLabelsList.isNotEmpty()) {
            selectedLabelsList.map { it.name ?: it.color ?: "برچسب" }.joinToString(",")
        } else null
        val labelColorsStr = if (selectedLabelsList.isNotEmpty()) {
            selectedLabelsList.map { it.color ?: "blue-xchange" }.joinToString(",")
        } else null

        val linesToSave = if (splitPerLine) multilineLines else listOf(cardTitle.trim())

        viewModelScope.launch {
            // Process attachments: copy them to local storage
            val localPaths = selectedAttachmentUris.mapNotNull { uri ->
                cardRepository.saveLocalFile(uri)
            }
            val localPathsStr = if (localPaths.isNotEmpty()) localPaths.joinToString(",") else null
            val attachmentCount = localPaths.size

            for (line in linesToSave) {
                cardRepository.addOfflineCard(
                    title = line,
                    listId = targetList.id,
                    listName = fullPathName,
                    labelIds = labelIdsStr,
                    labelNames = labelNamesStr,
                    labelColors = labelColorsStr,
                    dueDate = selectedDueDateISO,
                    localAttachmentPaths = localPathsStr,
                    attachmentCount = attachmentCount
                )
            }
            cardTitle = ""
            selectedLabelIds = emptySet()
            selectedDueDateISO = null
            selectedAttachmentUris = emptyList()
            showAddCardDialog = false
            showMultilinePromptDialog = false
            userMessage = if (linesToSave.size > 1) {
                "${linesToSave.size} کارت با موفقیت به صورت آفلاین ذخیره شدند"
            } else {
                "کارت با موفقیت به صورت آفلاین ذخیره شد"
            }
        }
    }

    fun uploadCards() {
        cardRepository.triggerSync()
        userMessage = "فرآیند آپلود در پس‌زمینه سیستم آغاز شد"
    }

    fun loadServerKartablCards() {
        viewModelScope.launch {
            val serverUrl = userPreferencesRepository.serverUrl.firstOrNull() ?: return@launch
            val token = userPreferencesRepository.authToken.firstOrNull() ?: return@launch

            if (allProjects.isEmpty() || allBoards.isEmpty()) {
                val projRes = cardRepository.fetchProjectsAndBoards(serverUrl, token)
                projRes.onSuccess { data ->
                    allProjects = data.projects
                    allBoards = data.boards
                }
            }

            if (allProjects.isEmpty() || allBoards.isEmpty()) return@launch

            isFetchingKartabl = true
            val res = cardRepository.fetchAllServerKartablCards(serverUrl, token, allProjects, allBoards)
            isFetchingKartabl = false

            res.fold(
                onSuccess = { result ->
                    if (result.cards.isNotEmpty()) {
                        serverKartablCards = result.cards
                    }
                    boardListsMap = result.boardListsMap
                    boardLabelsMap = result.boardLabelsMap
                    boardAttachmentsMap = result.boardAttachmentsMap
                },
                onFailure = { err ->
                    android.util.Log.d("NIMA2_DEBUG", "Kartabl offline fallback active: ${err.localizedMessage}")
                }
            )
        }
    }

    fun deleteCard(card: OfflineCard) {
        viewModelScope.launch {
            cardRepository.deleteOfflineCard(card)
        }
    }

    fun deleteServerCard(card: ServerKartablCard) {
        viewModelScope.launch {
            val serverUrl = userPreferencesRepository.serverUrl.firstOrNull() ?: return@launch
            val token = userPreferencesRepository.authToken.firstOrNull() ?: return@launch

            val res = cardRepository.deleteServerCard(serverUrl, token, card.id)
            serverCardToDeleteId = null

            res.fold(
                onSuccess = {
                    userMessage = "کارت با موفقیت از سرور حذف شد"
                    serverKartablCards = serverKartablCards.filter { it.id != card.id }
                    viewListCards = viewListCards.filter { it.id != card.id }
                },
                onFailure = { err ->
                    userMessage = "خطا در حذف کارت از سرور: ${err.localizedMessage}"
                }
            )
        }
    }

    fun updateServerCard(
        card: ServerKartablCard,
        newTitle: String,
        newListId: String? = null,
        newListName: String? = null,
        newBoardId: String? = null,
        newBoardName: String? = null,
        newProjectId: String? = null,
        newProjectName: String? = null,
        newDueDateISO: String?,
        newLabelIds: Set<String> = emptySet()
    ) {
        android.util.Log.d("NIMA2_EDIT_DEBUG", "=== START updateServerCard ===")
        android.util.Log.d("NIMA2_EDIT_DEBUG", "Original Card -> id: ${card.id}, title: '${card.name}', proj: '${card.projectName}' (${card.projectId}), board: '${card.boardName}' (${card.boardId}), list: '${card.listName}' (${card.listId})")
        android.util.Log.d("NIMA2_EDIT_DEBUG", "New Values     -> title: '$newTitle', newProj: '$newProjectName' ($newProjectId), newBoard: '$newBoardName' ($newBoardId), newList: '$newListName' ($newListId)")

        viewModelScope.launch {
            val serverUrl = userPreferencesRepository.serverUrl.firstOrNull() ?: return@launch
            val token = userPreferencesRepository.authToken.firstOrNull() ?: return@launch

            val res = cardRepository.updateServerCard(
                serverUrl = serverUrl,
                token = token,
                cardId = card.id,
                newTitle = newTitle,
                newBoardId = newBoardId,
                newListId = newListId,
                newDueDateISO = newDueDateISO
            )
            serverCardToEditId = null

            res.fold(
                onSuccess = { updatedPlankaCard ->
                    android.util.Log.d("NIMA2_EDIT_DEBUG", "API PATCH SUCCESS -> Planka returned item.id: ${updatedPlankaCard.id}, item.boardId: ${updatedPlankaCard.boardId}, item.listId: ${updatedPlankaCard.listId}")

                    userMessage = "کارت با موفقیت روی سرور به‌روزرسانی شد"

                    // Attach newly selected labels on Planka server if any
                    val currentLabelIds = card.labels.map { it.id }.toSet()
                    val labelsToAdd = newLabelIds - currentLabelIds
                    if (labelsToAdd.isNotEmpty()) {
                        val api = org.eshragh.nima2.data.remote.RetrofitClient.getApi(serverUrl)
                        val authHeader = "Bearer $token"
                        for (labelId in labelsToAdd) {
                            try {
                                api.addCardLabel(card.id, authHeader, org.eshragh.nima2.data.remote.model.AddCardLabelRequest(labelId))
                            } catch (_: Exception) {}
                        }
                    }

                    val targetBoardId = newBoardId ?: updatedPlankaCard.boardId ?: card.boardId
                    val availableForBoard = boardLabelsMap[targetBoardId] ?: emptyList()
                    val updatedLabels = availableForBoard.filter { newLabelIds.contains(it.id) }

                    val boardLists = boardListsMap[targetBoardId] ?: emptyList()
                    val updatedListName = boardLists.find { it.id == updatedPlankaCard.listId }?.name ?: newListName ?: card.listName

                    android.util.Log.d("NIMA2_EDIT_DEBUG", "In-memory card update -> cardId: ${card.id}, newListId: ${updatedPlankaCard.listId}, newListName: $updatedListName, targetBoardId: $targetBoardId")

                    val updateMapper: (ServerKartablCard) -> ServerKartablCard = { current ->
                        if (current.id == card.id) {
                            current.copy(
                                name = updatedPlankaCard.name,
                                projectId = newProjectId ?: current.projectId,
                                projectName = newProjectName ?: current.projectName,
                                boardId = targetBoardId,
                                boardName = newBoardName ?: current.boardName,
                                listId = updatedPlankaCard.listId,
                                listName = updatedListName,
                                dueDate = updatedPlankaCard.dueDate,
                                labels = updatedLabels
                            )
                        } else current
                    }

                    serverKartablCards = serverKartablCards.map(updateMapper).filter { !it.dueDate.isNullOrBlank() }
                    viewListCards = viewListCards.map(updateMapper).filter { it.listId == viewListSelectedList?.id }

                    android.util.Log.d("NIMA2_EDIT_DEBUG", "=== END updateServerCard ===")
                },
                onFailure = { err ->
                    android.util.Log.e("NIMA2_EDIT_DEBUG", "API PATCH FAILED -> ${err.localizedMessage}")
                    userMessage = "خطا در به‌روزرسانی کارت روی سرور: ${err.localizedMessage}"
                }
            )
        }
    }

    fun updateCardFull(
        card: OfflineCard,
        newTitle: String,
        newListId: String? = null,
        newListName: String? = null,
        newLabelIds: Set<String>,
        newDueDateISO: String?
    ) {
        val trimmed = newTitle.trim()
        if (trimmed.isEmpty()) {
            userMessage = "عنوان کارت نمی‌تواند خالی باشد"
            return
        }

        val labelIdsStr = if (newLabelIds.isNotEmpty()) newLabelIds.joinToString(",") else null
        val selectedLabelsList = availableLabels.filter { newLabelIds.contains(it.id) }
        val labelNamesStr = if (selectedLabelsList.isNotEmpty()) {
            selectedLabelsList.map { it.name ?: it.color ?: "برچسب" }.joinToString(",")
        } else null
        val labelColorsStr = if (selectedLabelsList.isNotEmpty()) {
            selectedLabelsList.map { it.color ?: "blue-xchange" }.joinToString(",")
        } else null

        viewModelScope.launch {
            cardRepository.updateCardFull(
                card = card,
                newTitle = trimmed,
                newListId = newListId,
                newListName = newListName,
                labelIds = labelIdsStr,
                labelNames = labelNamesStr,
                labelColors = labelColorsStr,
                dueDate = newDueDateISO
            )
            cardToEdit = null
            userMessage = "کارت با موفقیت به‌روزرسانی شد"
        }
    }

    fun addServerAttachment(cardId: String, uri: android.net.Uri) {
        viewModelScope.launch {
            val serverUrl = userPreferencesRepository.serverUrl.firstOrNull() ?: return@launch
            val token = userPreferencesRepository.authToken.firstOrNull() ?: return@launch
            
            val localPath = cardRepository.saveLocalFile(uri) ?: return@launch
            val res = cardRepository.uploadServerAttachment(serverUrl, token, cardId, localPath)
            
            res.fold(
                onSuccess = {
                    userMessage = "فایل با موفقیت آپلود شد"
                    // If we are in view list or kartabl, we might want to refresh the count manually
                    // or just reload the whole list.
                    loadServerKartablCards()
                    viewListSelectedList?.let { loadFullListCards(it.id) }
                },
                onFailure = { err ->
                    userMessage = "خطا در آپلود: ${err.localizedMessage}"
                }
            )
        }
    }

    fun deleteServerAttachment(attachmentId: String) {
        viewModelScope.launch {
            val serverUrl = userPreferencesRepository.serverUrl.firstOrNull() ?: return@launch
            val token = userPreferencesRepository.authToken.firstOrNull() ?: return@launch
            
            val res = cardRepository.deleteServerAttachment(serverUrl, token, attachmentId)
            res.fold(
                onSuccess = {
                    userMessage = "فایل حذف شد"
                    loadServerKartablCards()
                    viewListSelectedList?.let { loadFullListCards(it.id) }
                },
                onFailure = { err ->
                    userMessage = "خطا در حذف فایل: ${err.localizedMessage}"
                }
            )
        }
    }

    fun clearSynced() {
        viewModelScope.launch {
            cardRepository.clearSyncedCards()
            userMessage = "کارت‌های آپلود شده پاک شدند"
        }
    }

    fun dismissMessage() {
        userMessage = null
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onLoggedOut()
        }
    }

    fun quickMoveServerCard(card: ServerKartablCard) {
        viewModelScope.launch {
            val serverUrl = userPreferencesRepository.serverUrl.firstOrNull() ?: return@launch
            val token = userPreferencesRepository.authToken.firstOrNull() ?: return@launch
            val target = userPreferencesRepository.quickMoveTarget.firstOrNull() ?: return@launch

            if (target.listId == null) {
                userMessage = "لیست مقصد برای انتقال سریع تنظیم نشده است"
                return@launch
            }

            val res = cardRepository.updateServerCard(
                serverUrl = serverUrl,
                token = token,
                cardId = card.id,
                newTitle = card.name,
                newBoardId = target.boardId,
                newListId = target.listId,
                newDueDateISO = card.dueDate
            )

            res.fold(
                onSuccess = { updatedPlankaCard ->
                    userMessage = "کارت با موفقیت به '${target.listName}' منتقل شد"
                    
                    // In-memory update
                    val updateMapper: (ServerKartablCard) -> ServerKartablCard = { current ->
                        if (current.id == card.id) {
                            current.copy(
                                projectId = target.projectId ?: current.projectId,
                                projectName = target.projectName ?: current.projectName,
                                boardId = target.boardId ?: current.boardId,
                                boardName = target.boardName ?: current.boardName,
                                listId = updatedPlankaCard.listId,
                                listName = target.listName ?: current.listName
                            )
                        } else current
                    }

                    serverKartablCards = serverKartablCards.map(updateMapper).filter { !it.dueDate.isNullOrBlank() }
                    viewListCards = viewListCards.map(updateMapper).filter { it.listId == viewListSelectedList?.id }
                },
                onFailure = { err ->
                    userMessage = "خطا در انتقال سریع: ${err.localizedMessage}"
                }
            )
        }
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
