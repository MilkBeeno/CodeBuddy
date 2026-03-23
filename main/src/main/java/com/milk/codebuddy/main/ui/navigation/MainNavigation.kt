package com.milk.codebuddy.main.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.milk.codebuddy.base.ui.navigation.Screen
import com.milk.codebuddy.main.ui.screen.AddTransactionScreen
import com.milk.codebuddy.main.ui.screen.MainScreen

/**
 * 注册 Main Screen 路由
 */
fun NavGraphBuilder.mainScreen() {
    composable(Screen.Main.route) {
        MainScreen(
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * 注册 AddTransaction Screen 路由
 */
fun NavGraphBuilder.addTransactionScreen(
    onBack: () -> Unit
) {
    composable(Screen.AddTransaction.route) {
        AddTransactionScreen(
            onBack = onBack,
            modifier = Modifier.fillMaxSize()
        )
    }
}

