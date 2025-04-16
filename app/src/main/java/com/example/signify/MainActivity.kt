package com.example.signify

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.signify.navigation.SignifyNavigation
import com.example.signify.ui.theme.SignifyTheme
import com.example.signify.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.identity.Identity


class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authViewModel = AuthViewModel(this, Identity.getSignInClient(this))

        enableEdgeToEdge()

        setContent {

            var isDarkTheme by rememberSaveable { mutableStateOf(true) }
            SignifyTheme(darkTheme = isDarkTheme)  {
                val snackbarHostState = remember { SnackbarHostState() }
                var isInternetAvailable = checkInternetConnectivity(this)

                LaunchedEffect(isInternetAvailable) {
                    if (!isInternetAvailable) {
                        snackbarHostState.showSnackbar("No internet connection.", duration = SnackbarDuration.Long)
                    }
                }

                Scaffold(snackbarHost = { SnackbarHost(snackbarHostState, modifier = Modifier.padding(bottom = 85.dp)) },
                    modifier = Modifier.fillMaxSize(),
                ) { innerPadding ->
                    SignifyNavigation(
                        modifier = Modifier.padding(innerPadding),
                        authViewModel = authViewModel,
                        snackbarHostState = snackbarHostState,
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = { isDarkTheme = !isDarkTheme }
                    )
                }
            }
        }
    }
}


fun checkInternetConnectivity(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}