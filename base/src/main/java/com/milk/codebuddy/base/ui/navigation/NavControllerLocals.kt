package com.milk.codebuddy.base.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController

/**
 * 用于在任意 Composable 中访问 NavHostController 的 Local 提供者
 */
val LocalNavController = staticCompositionLocalOf<NavHostController> {
    error("No NavController provided")
}

/**
 * 在作用域内提供 NavHostController
 */
@Composable
fun ProvideNavHostController(
    navController: NavHostController,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalNavController provides navController) {
        content()
    }
}
