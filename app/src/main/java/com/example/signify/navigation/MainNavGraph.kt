package com.example.signify.navigation

import LessonPage
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.signify.ui.screens.HomePage
import com.example.signify.ui.screens.LearnPage
import com.example.signify.ui.screens.SettingPage
import com.example.signify.viewmodel.AuthViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainNavGraph(
    modifier: Modifier,
    authNavController: NavController,
    authViewModel: AuthViewModel,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }


    Scaffold(
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding),

        ) {
            composable(BottomNavItem.Home.route) {
                HomePage(modifier, authViewModel, snackbarHostState)
            }
            composable(BottomNavItem.Learn.route) {
                LearnPage(modifier, authViewModel, snackbarHostState,navController)
            }
            composable(BottomNavItem.Setting.route) {
                SettingPage(
                    authNavController =authNavController,
                    authViewModel = authViewModel,
                    isDarkTheme=isDarkTheme,
                    onToggleTheme= onToggleTheme,
                )
            }


            // Lessons_Components
            composable("lessonPage/{lessonId}") { backStackEntry ->
                val lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""
                LessonPage(lessonId,navController)
            }


        }
    }
}
