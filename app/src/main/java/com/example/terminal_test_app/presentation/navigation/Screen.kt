package com.example.terminal_test_app.presentation.navigation

sealed class Screen(
    val route: String,
    val label: String
) {
    object Checkout : Screen("checkout", "Checkout")
    object Scan : Screen("scan", "Scan")
    object NetworkTest : Screen("networkTest", "NetworkTest")
    object Settings : Screen("settings", "Settings")
}