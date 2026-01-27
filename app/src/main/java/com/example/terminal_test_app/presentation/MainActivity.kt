package com.example.terminal_test_app.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Scaffold
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.terminal_test_app.platform.scan.QrDebugScreen
import com.example.terminal_test_app.presentation.navigation.AppNavGraph
import com.example.terminal_test_app.presentation.navigation.Screen
import com.example.terminal_test_app.ui.theme.TerminaltestappTheme
import com.example.terminal_test_app.presentation.navigation.BottomBar
import com.example.terminal_test_app.presentation.ui.components.AppTopBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TerminaltestappTheme {

                val navController = rememberNavController()
                val currentDestination = navController.currentBackStackEntryAsState().value?.destination
                val currentRoute = currentDestination?.route

                val title = when (currentRoute) {
                    Screen.Checkout.route -> "Checkout"
                    Screen.Scan.route -> "Scan"
                    Screen.NetworkTest.route -> "NetworkTest"
                    Screen.Settings.route -> "Settings"
                    else -> "Test App"
                }

                val showBottomBar =
                    currentRoute == Screen.Checkout.route ||
                            currentRoute == Screen.Scan.route ||
                            currentRoute == Screen.NetworkTest.route ||
                            currentRoute == Screen.Settings.route

                Scaffold(
                    topBar = {
                        AppTopBar(title = title, showLogo = false, onNavBack = null)
                    },
                    bottomBar = {
                        if (showBottomBar) BottomBar(navController)
                    }
                ) { padding ->
                    AppNavGraph(
                        navController = navController,
                        padding = padding
                    )
                }
            }
        }
    }
}
