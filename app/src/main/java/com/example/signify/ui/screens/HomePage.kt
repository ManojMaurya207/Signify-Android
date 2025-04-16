package com.example.signify.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FrontHand
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.signify.ui.main_app_comp.Sign2Text
import com.example.signify.ui.main_app_comp.Text2Sign
import com.example.signify.viewmodel.AuthViewModel

@Composable
fun HomePage(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    snackbarHostState: SnackbarHostState
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Text to Sign" to Icons.Default.TextFields, "Sign to Text" to Icons.Default.FrontHand)

    Scaffold(
        topBar = {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, (title, icon) ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal) },
                        icon = { Icon(imageVector = icon, contentDescription = title) }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .animateContentSize()
        ) {
            when (selectedTabIndex) {
                0 -> Text2Sign(modifier, snackbarHostState)
                1 -> Sign2Text(modifier, snackbarHostState)
            }
        }
    }
}
