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
import org.eshragh.nima2.data.remote.model.PlankaList
import org.eshragh.nima2.data.remote.model.PlankaProject
import org.eshragh.nima2.data.repository.AuthRepository
import org.eshragh.nima2.data.repository.CardRepository

class HomeViewModel(
    private val cardRepository: CardRepository,
    private val authRepository: AuthRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val offlineCards: StateFlow<List<OfflineCard>> = cardRepository.offlineCards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All Projects and Boards loaded from server
    var allProjects by mutableStateOf<List<PlankaProject>>(emptyList())
        private set

    var allBoards by mutableStateOf<List<PlankaBoard>>(emptyList())
        private set

    // Active Selections
    var selectedProject by mutableStateOf<PlankaProject?>(null)
        private set

    var selectedBoard by mutableStateOf<PlankaBoard?>(null)
        private set

    var selectedList by mutableStateOf<PlankaList?>(null)
        private set

    // Lists available for the currently selected board
    var availableLists by mutableStateOf<List<PlankaList>>(emptyList())
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

    var cardTitle by mutableStateOf("")
        private set

    var isFetchingData by mutableStateOf(false)
        private set

    var isFetchingLists by mutableStateOf(false)
        private set

    var isUploading by mutableStateOf(false)
        private set

    var userMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadInitialDataAndRestoreSelections()
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
                },
                onFailure = { err ->
                    userMessage = "خطا در دریافت پروژه‌ها: ${err.localizedMessage}"
                }
            )
        }
    }

    private suspend fun loadListsForBoard(serverUrl: String, token: String, boardId: String, preferredListId: String? = null) {
        isFetchingLists = true
        val res = cardRepository.fetchListsForBoard(serverUrl, token, boardId)
        isFetchingLists = false

        res.fold(
            onSuccess = { lists ->
                availableLists = lists
                val list = lists.find { it.id == preferredListId } ?: lists.firstOrNull()
                selectedList = list
                persistSelection(selectedProject, selectedBoard, list)
            },
            onFailure = { err ->
                availableLists = emptyList()
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

        val fullPathName = "${selectedProject?.name ?: "پروژه"} > ${selectedBoard?.name ?: "بورد"} > ${targetList.name ?: "لیست"}"

        viewModelScope.launch {
            cardRepository.addOfflineCard(
                title = title,
                listId = targetList.id,
                listName = fullPathName
            )
            cardTitle = ""
            userMessage = "کارت با موفقیت به صورت آفلاین ذخیره شد"
        }
    }

    fun uploadCards() {
        viewModelScope.launch {
            val serverUrl = userPreferencesRepository.serverUrl.firstOrNull()
            val token = userPreferencesRepository.authToken.firstOrNull()

            if (serverUrl.isNullOrBlank() || token.isNullOrBlank()) {
                userMessage = "اطلاعات لاگین یافت نشد. مجددا وارد شوید"
                return@launch
            }

            isUploading = true
            val count = cardRepository.uploadPendingCards(serverUrl, token)
            isUploading = false

            userMessage = if (count > 0) {
                "$count کارت با موفقیت آپلود شد"
            } else {
                "کارتی برای آپلود وجود ندارد یا خطایی رخ داده است"
            }
        }
    }

    fun deleteCard(card: OfflineCard) {
        viewModelScope.launch {
            cardRepository.deleteOfflineCard(card)
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
