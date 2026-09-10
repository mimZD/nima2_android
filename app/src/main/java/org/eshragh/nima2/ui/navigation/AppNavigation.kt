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
import org.eshragh.nima2.data.pref.UserPreferencesRepository
import org.eshragh.nima2.data.repository.AuthRepository
import org.eshragh.nima2.ui.home.HomeViewModel
import org.eshragh.nima2.ui.home.HomeScreen
import org.eshragh.nima2.ui.duedates.UpcomingDueDatesScreen
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
fun AppNavigation(homeViewModel: HomeViewModel) {
    val context = LocalContext.current
    val navController = rememberNavController() // Move outside of 'when' to prevent state loss
    
    val userPreferencesRepository = remember { UserPreferencesRepository(context) }
    val authRepository = remember { AuthRepository(userPreferencesRepository) }

    val authTokenState by userPreferencesRepository.authToken.collectAsState(initial = "LOADING")

    when (val token = authTokenState) {
        "LOADING" -> {
            SplashScreen()
        }
        null, "" -> {
            NavHost(navController = navController, startDestination = Destinations.LOGIN) {
                composable(Destinations.LOGIN) {
                    val loginViewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory(authRepository, userPreferencesRepository))
                    LoginScreen(viewModel = loginViewModel, onLoginSuccess = {})
                }
            }
        }
        else -> {
            NavHost(navController = navController, startDestination = Destinations.HOME) {
                composable(Destinations.HOME) {
                    HomeScreen(
                        viewModel = homeViewModel,
                        onOpenDueDates = { navController.navigate(Destinations.DUE_DATES) },
                        onOpenSettings = { navController.navigate(Destinations.SETTINGS) },
                        onLogout = { homeViewModel.logout {} }
                    )
                }
                composable(Destinations.DUE_DATES) {
                    UpcomingDueDatesScreen(viewModel = homeViewModel, onBack = { navController.popBackStack() })
                }
                composable(Destinations.SETTINGS) {
                    SettingsScreen(viewModel = homeViewModel, onBack = { navController.popBackStack() })
                }
            }
        }
    }
}
