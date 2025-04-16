package com.example.signify.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.signify.ui.screens.login.LoginPage
import com.example.signify.ui.screens.login.SignUpPage
import com.example.signify.ui.screens.login.OnBoardingPage
import com.example.signify.viewmodel.AuthState
import com.example.signify.viewmodel.AuthViewModel


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SignifyNavigation(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    snackbarHostState: SnackbarHostState,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
) {
    val navController = rememberNavController()
    // Determine the start destination based on the auth state
    val startDestination = if (authViewModel.authState.value == AuthState.Authenticated) "mainscreen" else "onboarding"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("onboarding") {
            OnBoardingPage(modifier,navController, authViewModel, snackbarHostState)
        }
        composable("login") {
            LoginPage(modifier, navController, authViewModel, snackbarHostState)
        }
        composable("signup") {
            SignUpPage(modifier, navController, authViewModel, snackbarHostState)
        }
        composable("mainscreen") {
            MainNavGraph(modifier, navController, authViewModel,isDarkTheme,onToggleTheme)
        }

    }
}
