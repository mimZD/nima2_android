package org.eshragh.nima2.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.eshragh.nima2.data.pref.UserPreferencesRepository
import org.eshragh.nima2.data.repository.AuthRepository

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val serverUrl = UserPreferencesRepository.DEFAULT_SERVER_URL

    var username by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun onUsernameChange(name: String) {
        username = name
    }

    fun onPasswordChange(pass: String) {
        password = pass
    }

    fun login(onSuccess: () -> Unit) {
        if (username.isBlank() || password.isBlank()) {
            errorMessage = "نام کاربری و کلمه عبور را وارد کنید"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            val result = authRepository.login(serverUrl, username, password)
            isLoading = false
            result.fold(
                onSuccess = { onSuccess() },
                onFailure = { errorMessage = it.localizedMessage ?: "خطای ورود" }
            )
        }
    }

    class Factory(
        private val authRepository: AuthRepository,
        private val userPreferencesRepository: UserPreferencesRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LoginViewModel(authRepository, userPreferencesRepository) as T
        }
    }
}
