package com.milk.codebuddy.base.ui.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Main : Screen("main")
    data object AddTransaction : Screen("add_transaction")
}
