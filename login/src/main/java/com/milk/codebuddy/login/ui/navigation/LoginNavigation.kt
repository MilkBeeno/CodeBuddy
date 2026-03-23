package com.milk.codebuddy.login.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.milk.codebuddy.base.ui.navigation.LocalNavController
import com.milk.codebuddy.base.ui.navigation.Screen
import com.milk.codebuddy.login.ui.screen.LoginScreen

/**
 * 注册 Login Screen 路由
 */
fun NavGraphBuilder.loginScreen() {
    composable(Screen.Login.route) {
        val controller = LocalNavController.current
        LoginScreen(
            modifier = Modifier.fillMaxSize(),
            onLoginSuccess = {
                controller.navigate(Screen.Main.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }
        )
    }
}
