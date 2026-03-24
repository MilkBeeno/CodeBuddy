package com.milk.codebuddy.login.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.milk.codebuddy.base.ui.navigation.LocalNavController
import com.milk.codebuddy.base.ui.navigation.Screen
import com.milk.codebuddy.login.ui.screen.SplashScreen

/**
 * 注册 Splash Screen 路由
 * 检查用户会话状态，决定跳转到首页还是登录页
 */
fun NavGraphBuilder.splashScreen() {
    composable(Screen.Splash.route) {
        val controller = LocalNavController.current
        SplashScreen(
            modifier = Modifier.fillMaxSize(),
            onNavigateToLogin = {
                controller.navigate(Screen.Login.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            },
            onNavigateToMain = {
                // 如果用户已登录，直接跳转到首页
                controller.navigate(Screen.Main.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
        )
    }
}
