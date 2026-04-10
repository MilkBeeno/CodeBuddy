package com.milk.codebuddy.login.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.milk.codebuddy.base.ui.navigation.ForgotPassword
import com.milk.codebuddy.base.ui.navigation.LocalNavController
import com.milk.codebuddy.base.ui.navigation.Login
import com.milk.codebuddy.base.ui.navigation.Register
import com.milk.codebuddy.login.ui.screen.RegisterScreen

/**
 * 注册 Register Screen 路由
 *
 * 技术栈规范：
 * - 类型安全路由：使用 composable<Register> 保证类型安全
 * - 返回栈管理：注册成功后 popBackStack 回到登录页
 * - 单例跳转：launchSingleTop = true 防止重复进入
 */
fun NavGraphBuilder.registerScreen() {
    composable<Register> {
        val controller = LocalNavController.current
        RegisterScreen(
            modifier = Modifier.fillMaxSize(),
            onNavigateToLogin = {
                controller.popBackStack()
            }
        )
    }
}
