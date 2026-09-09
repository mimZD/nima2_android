package org.eshragh.nima2.data.pref

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

data class SelectedTarget(
    val projectId: String? = null,
    val projectName: String? = null,
    val boardId: String? = null,
    val boardName: String? = null,
    val listId: String? = null,
    val listName: String? = null
)

class UserPreferencesRepository(private val context: Context) {

    companion object {
        private val SERVER_URL = stringPreferencesKey("server_url")
        private val AUTH_TOKEN = stringPreferencesKey("auth_token")

        private val SELECTED_PROJECT_ID = stringPreferencesKey("selected_project_id")
        private val SELECTED_PROJECT_NAME = stringPreferencesKey("selected_project_name")
        private val SELECTED_BOARD_ID = stringPreferencesKey("selected_board_id")
        private val SELECTED_BOARD_NAME = stringPreferencesKey("selected_board_name")
        private val SELECTED_LIST_ID = stringPreferencesKey("selected_list_id")
        private val SELECTED_LIST_NAME = stringPreferencesKey("selected_list_name")

        private val VIEW_LIST_PROJECT_ID = stringPreferencesKey("view_list_project_id")
        private val VIEW_LIST_PROJECT_NAME = stringPreferencesKey("view_list_project_name")
        private val VIEW_LIST_BOARD_ID = stringPreferencesKey("view_list_board_id")
        private val VIEW_LIST_BOARD_NAME = stringPreferencesKey("view_list_board_name")
        private val VIEW_LIST_LIST_ID = stringPreferencesKey("view_list_list_id")
        private val VIEW_LIST_LIST_NAME = stringPreferencesKey("view_list_list_name")

        private val DEFAULT_KARTABL_TAB = intPreferencesKey("default_kartabl_tab")

        private val RIGHT_SWIPE_ACTION = intPreferencesKey("right_swipe_action")
        private val LEFT_SWIPE_ACTION = intPreferencesKey("left_swipe_action")

        private val QUICK_MOVE_PROJECT_ID = stringPreferencesKey("quick_move_project_id")
        private val QUICK_MOVE_PROJECT_NAME = stringPreferencesKey("quick_move_project_name")
        private val QUICK_MOVE_BOARD_ID = stringPreferencesKey("quick_move_board_id")
        private val QUICK_MOVE_BOARD_NAME = stringPreferencesKey("quick_move_board_name")
        private val QUICK_MOVE_LIST_ID = stringPreferencesKey("quick_move_list_id")
        private val QUICK_MOVE_LIST_NAME = stringPreferencesKey("quick_move_list_name")

        const val DEFAULT_SERVER_URL = "https://nima2.eshragh.org"
    }

    val serverUrl: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SERVER_URL] ?: DEFAULT_SERVER_URL
    }

    val authToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[AUTH_TOKEN]
    }

    val defaultKartablTab: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[DEFAULT_KARTABL_TAB] ?: 0
    }

    val rightSwipeAction: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[RIGHT_SWIPE_ACTION] ?: 0 // Default: Edit
    }

    val leftSwipeAction: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[LEFT_SWIPE_ACTION] ?: 1 // Default: Delete
    }

    val quickMoveTarget: Flow<SelectedTarget> = context.dataStore.data.map { preferences ->
        SelectedTarget(
            projectId = preferences[QUICK_MOVE_PROJECT_ID],
            projectName = preferences[QUICK_MOVE_PROJECT_NAME],
            boardId = preferences[QUICK_MOVE_BOARD_ID],
            boardName = preferences[QUICK_MOVE_BOARD_NAME],
            listId = preferences[QUICK_MOVE_LIST_ID],
            listName = preferences[QUICK_MOVE_LIST_NAME]
        )
    }

    val selectedTarget: Flow<SelectedTarget> = context.dataStore.data.map { preferences ->
        SelectedTarget(
            projectId = preferences[SELECTED_PROJECT_ID],
            projectName = preferences[SELECTED_PROJECT_NAME],
            boardId = preferences[SELECTED_BOARD_ID],
            boardName = preferences[SELECTED_BOARD_NAME],
            listId = preferences[SELECTED_LIST_ID],
            listName = preferences[SELECTED_LIST_NAME]
        )
    }

    val viewListSelectedTarget: Flow<SelectedTarget> = context.dataStore.data.map { preferences ->
        SelectedTarget(
            projectId = preferences[VIEW_LIST_PROJECT_ID],
            projectName = preferences[VIEW_LIST_PROJECT_NAME],
            boardId = preferences[VIEW_LIST_BOARD_ID],
            boardName = preferences[VIEW_LIST_BOARD_NAME],
            listId = preferences[VIEW_LIST_LIST_ID],
            listName = preferences[VIEW_LIST_LIST_NAME]
        )
    }

    suspend fun saveAuthData(serverUrl: String, token: String) {
        context.dataStore.edit { preferences ->
            preferences[SERVER_URL] = serverUrl.trimEnd('/')
            preferences[AUTH_TOKEN] = token
        }
    }

    suspend fun saveSelectedTarget(
        projectId: String?,
        projectName: String?,
        boardId: String?,
        boardName: String?,
        listId: String?,
        listName: String?
    ) {
        context.dataStore.edit { preferences ->
            if (projectId != null) preferences[SELECTED_PROJECT_ID] = projectId else preferences.remove(SELECTED_PROJECT_ID)
            if (projectName != null) preferences[SELECTED_PROJECT_NAME] = projectName else preferences.remove(SELECTED_PROJECT_NAME)
            if (boardId != null) preferences[SELECTED_BOARD_ID] = boardId else preferences.remove(SELECTED_BOARD_ID)
            if (boardName != null) preferences[SELECTED_BOARD_NAME] = boardName else preferences.remove(SELECTED_BOARD_NAME)
            if (listId != null) preferences[SELECTED_LIST_ID] = listId else preferences.remove(SELECTED_LIST_ID)
            if (listName != null) preferences[SELECTED_LIST_NAME] = listName else preferences.remove(SELECTED_LIST_NAME)
        }
    }

    suspend fun saveViewListSelectedTarget(
        projectId: String?,
        projectName: String?,
        boardId: String?,
        boardName: String?,
        listId: String?,
        listName: String?
    ) {
        context.dataStore.edit { preferences ->
            if (projectId != null) preferences[VIEW_LIST_PROJECT_ID] = projectId else preferences.remove(VIEW_LIST_PROJECT_ID)
            if (projectName != null) preferences[VIEW_LIST_PROJECT_NAME] = projectName else preferences.remove(VIEW_LIST_PROJECT_NAME)
            if (boardId != null) preferences[VIEW_LIST_BOARD_ID] = boardId else preferences.remove(VIEW_LIST_BOARD_ID)
            if (boardName != null) preferences[VIEW_LIST_BOARD_NAME] = boardName else preferences.remove(VIEW_LIST_BOARD_NAME)
            if (listId != null) preferences[VIEW_LIST_LIST_ID] = listId else preferences.remove(VIEW_LIST_LIST_ID)
            if (listName != null) preferences[VIEW_LIST_LIST_NAME] = listName else preferences.remove(VIEW_LIST_LIST_NAME)
        }
    }

    suspend fun saveDefaultKartablTab(tabIndex: Int) {
        context.dataStore.edit { preferences ->
            preferences[DEFAULT_KARTABL_TAB] = tabIndex
        }
    }

    suspend fun saveSwipeActions(right: Int, left: Int) {
        context.dataStore.edit { preferences ->
            preferences[RIGHT_SWIPE_ACTION] = right
            preferences[LEFT_SWIPE_ACTION] = left
        }
    }

    suspend fun saveQuickMoveTarget(
        projectId: String?,
        projectName: String?,
        boardId: String?,
        boardName: String?,
        listId: String?,
        listName: String?
    ) {
        context.dataStore.edit { preferences ->
            if (projectId != null) preferences[QUICK_MOVE_PROJECT_ID] = projectId else preferences.remove(QUICK_MOVE_PROJECT_ID)
            if (projectName != null) preferences[QUICK_MOVE_PROJECT_NAME] = projectName else preferences.remove(QUICK_MOVE_PROJECT_NAME)
            if (boardId != null) preferences[QUICK_MOVE_BOARD_ID] = boardId else preferences.remove(QUICK_MOVE_BOARD_ID)
            if (boardName != null) preferences[QUICK_MOVE_BOARD_NAME] = boardName else preferences.remove(QUICK_MOVE_BOARD_NAME)
            if (listId != null) preferences[QUICK_MOVE_LIST_ID] = listId else preferences.remove(QUICK_MOVE_LIST_ID)
            if (listName != null) preferences[QUICK_MOVE_LIST_NAME] = listName else preferences.remove(QUICK_MOVE_LIST_NAME)
        }
    }

    suspend fun clearAuth() {
        context.dataStore.edit { preferences ->
            preferences.remove(AUTH_TOKEN)
            preferences.remove(SELECTED_PROJECT_ID)
            preferences.remove(SELECTED_PROJECT_NAME)
            preferences.remove(SELECTED_BOARD_ID)
            preferences.remove(SELECTED_BOARD_NAME)
            preferences.remove(SELECTED_LIST_ID)
            preferences.remove(SELECTED_LIST_NAME)
            preferences.remove(VIEW_LIST_PROJECT_ID)
            preferences.remove(VIEW_LIST_PROJECT_NAME)
            preferences.remove(VIEW_LIST_BOARD_ID)
            preferences.remove(VIEW_LIST_BOARD_NAME)
            preferences.remove(VIEW_LIST_LIST_ID)
            preferences.remove(VIEW_LIST_LIST_NAME)
        }
    }
}
