package com.milk.codebuddy.main.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.milk.codebuddy.base.ui.navigation.AddTransaction
import com.milk.codebuddy.base.ui.navigation.LocalNavController
import com.milk.codebuddy.base.ui.navigation.Main
import com.milk.codebuddy.main.ui.screen.AddTransactionScreen
import com.milk.codebuddy.main.ui.screen.MainScreen

/**
 * 注册 Main Screen 路由
 */
fun NavGraphBuilder.mainScreen() {
    composable<Main> {
        MainScreen(
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * 注册 AddTransaction Screen 路由
 *
 * 导航动作在 Navigation 层消费，不透传给上层调用方。
 */
fun NavGraphBuilder.addTransactionScreen() {
    composable<AddTransaction> {
        val navController = LocalNavController.current
        AddTransactionScreen(
            onBack = { navController.popBackStack() },
            modifier = Modifier.fillMaxSize()
        )
    }
}

