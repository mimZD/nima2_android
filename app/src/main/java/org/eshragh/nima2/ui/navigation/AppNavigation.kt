package org.eshragh.nima2.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.eshragh.nima2.data.local.AppDatabase
import org.eshragh.nima2.data.pref.UserPreferencesRepository
import org.eshragh.nima2.data.repository.AuthRepository
import org.eshragh.nima2.data.repository.CardRepository
import org.eshragh.nima2.ui.duedates.UpcomingDueDatesScreen
import org.eshragh.nima2.ui.home.HomeScreen
import org.eshragh.nima2.ui.home.HomeViewModel
import org.eshragh.nima2.ui.login.LoginScreen
import org.eshragh.nima2.ui.login.LoginViewModel
import org.eshragh.nima2.ui.settings.SettingsScreen
import org.eshragh.nima2.ui.splash.SplashScreen

object Destinations {
    const val LOGIN = "login"
    const val HOME = "home"
    const val DUE_DATES = "due_dates"
    const val SETTINGS = "settings"
}

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val app = context.applicationContext as org.eshragh.nima2.NimaApp
    
    val userPreferencesRepository = remember { UserPreferencesRepository(context) }
    val authRepository = remember { AuthRepository(userPreferencesRepository) }
    val cardRepository = remember { app.cardRepository }

    val authTokenState by userPreferencesRepository.authToken.collectAsState(initial = "LOADING")

    when (val token = authTokenState) {
        "LOADING" -> {
            SplashScreen()
        }
        null, "" -> {
            val navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = Destinations.LOGIN
            ) {
                composable(Destinations.LOGIN) {
                    val loginViewModel: LoginViewModel = viewModel(
                        factory = LoginViewModel.Factory(authRepository, userPreferencesRepository)
                    )
                    LoginScreen(
                        viewModel = loginViewModel,
                        onLoginSuccess = {
                            // Saving token automatically updates authTokenState to HOME
                        }
                    )
                }
            }
        }
        else -> {
            val navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = Destinations.HOME
            ) {
                composable(Destinations.HOME) {
                    val homeViewModel: HomeViewModel = viewModel(
                        factory = HomeViewModel.Factory(cardRepository, authRepository, userPreferencesRepository)
                    )
                    HomeScreen(
                        viewModel = homeViewModel,
                        onOpenDueDates = {
                            navController.navigate(Destinations.DUE_DATES)
                        },
                        onOpenSettings = {
                            navController.navigate(Destinations.SETTINGS)
                        },
                        onLogout = {
                            homeViewModel.logout {}
                        }
                    )
                }
                composable(Destinations.DUE_DATES) {
                    val homeViewModel: HomeViewModel = viewModel(
                        factory = HomeViewModel.Factory(cardRepository, authRepository, userPreferencesRepository)
                    )
                    UpcomingDueDatesScreen(
                        viewModel = homeViewModel,
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }
                composable(Destinations.SETTINGS) {
                    val homeViewModel: HomeViewModel = viewModel(
                        factory = HomeViewModel.Factory(cardRepository, authRepository, userPreferencesRepository)
                    )
                    SettingsScreen(
                        viewModel = homeViewModel,
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}
