package com.milk.codebuddy.login.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.milk.codebuddy.base.ui.navigation.LocalNavController
import com.milk.codebuddy.base.ui.navigation.Routes
import com.milk.codebuddy.login.ui.screen.SplashScreen

/**
 * 注册 Splash Screen 路由
 * 检查用户会话状态，决定跳转到首页还是登录页
 * 
 * 技术栈规范：
 * - 导航图拆分：根据业务模块将导航逻辑拆分为不同的扩展函数
 * - 路由拦截逻辑：生成带 LaunchedEffect 的导航监听器，用于处理"未登录自动跳转"等全局拦截场景
 * - 返回栈管理：利用 popUpTo 和 inclusive 正确处理登录跳转，防止物理返回键导致的页面错乱
 * - 单例跳转：导航动作必须配置 launchSingleTop = true
 */
fun NavGraphBuilder.splashScreen() {
    composable(Routes.Splash) {
        val controller = LocalNavController.current
        SplashScreen(
            modifier = Modifier.fillMaxSize(),
            onNavigateToLogin = {
                controller.navigate(Routes.Login) {
                    popUpTo(Routes.Splash) { inclusive = true }
                    launchSingleTop = true
                }
            },
            onNavigateToMain = {
                // 如果用户已登录，直接跳转到首页
                controller.navigate(Routes.Main) {
                    popUpTo(Routes.Splash) { inclusive = true }
                    launchSingleTop = true
                }
            }
        )
    }
}
