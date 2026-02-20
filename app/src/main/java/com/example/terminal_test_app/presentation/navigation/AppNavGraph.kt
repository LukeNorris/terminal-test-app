package com.example.terminal_test_app.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.terminal_test_app.presentation.checkout.CheckoutScreen
import com.example.terminal_test_app.presentation.endpointTester.NetworkTestScreen
import com.example.terminal_test_app.presentation.scan.ScanScreen
import com.example.terminal_test_app.presentation.settings.SettingsRoute

@Composable
fun AppNavGraph(
    navController: NavHostController,
    padding: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Checkout.route,
        modifier = Modifier.padding(padding)
    ) {
        composable(Screen.Checkout.route) {
            CheckoutScreen(
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        composable(Screen.Scan.route) {
            ScanScreen()
        }
        composable(Screen.NetworkTest.route) {
            NetworkTestScreen()
        }
        composable(Screen.Settings.route) {
            SettingsRoute()
        }
    }
}