package org.eshragh.nima2.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.first
import org.eshragh.nima2.data.local.AppDatabase
import org.eshragh.nima2.data.pref.UserPreferencesRepository
import org.eshragh.nima2.data.repository.AuthRepository
import org.eshragh.nima2.data.repository.CardRepository
import org.eshragh.nima2.ui.home.HomeScreen
import org.eshragh.nima2.ui.home.HomeViewModel
import org.eshragh.nima2.ui.login.LoginScreen
import org.eshragh.nima2.ui.login.LoginViewModel
import org.eshragh.nima2.ui.splash.SplashScreen

object Destinations {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val HOME = "home"
}

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val navController = rememberNavController()

    val database = remember { AppDatabase.getDatabase(context) }
    val userPreferencesRepository = remember { UserPreferencesRepository(context) }
    val authRepository = remember { AuthRepository(userPreferencesRepository) }
    val cardRepository = remember { CardRepository(database.cardDao(), userPreferencesRepository) }

    NavHost(
        navController = navController,
        startDestination = Destinations.SPLASH
    ) {
        composable(Destinations.SPLASH) {
            LaunchedEffect(Unit) {
                val token = userPreferencesRepository.authToken.first()
                val targetDestination = if (!token.isNullOrBlank()) Destinations.HOME else Destinations.LOGIN
                navController.navigate(targetDestination) {
                    popUpTo(Destinations.SPLASH) { inclusive = true }
                }
            }
            SplashScreen()
        }

        composable(Destinations.LOGIN) {
            val loginViewModel: LoginViewModel = viewModel(
                factory = LoginViewModel.Factory(authRepository, userPreferencesRepository)
            )
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate(Destinations.HOME) {
                        popUpTo(Destinations.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Destinations.HOME) {
            val homeViewModel: HomeViewModel = viewModel(
                factory = HomeViewModel.Factory(cardRepository, authRepository, userPreferencesRepository)
            )
            HomeScreen(
                viewModel = homeViewModel,
                onLogout = {
                    navController.navigate(Destinations.LOGIN) {
                        popUpTo(Destinations.HOME) { inclusive = true }
                    }
                }
            )
        }
    }
}
