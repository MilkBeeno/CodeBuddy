package com.milk.codebuddy.login.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.milk.codebuddy.base.ui.navigation.ForgotPassword
import com.milk.codebuddy.base.ui.navigation.LocalNavController
import com.milk.codebuddy.base.ui.navigation.Login
import com.milk.codebuddy.base.ui.navigation.Main
import com.milk.codebuddy.base.ui.navigation.Register
import com.milk.codebuddy.login.ui.screen.LoginScreen
import com.milk.codebuddy.login.ui.viewmodel.LoginViewModel

/**
 * 注册 Login Screen 路由
 *
 * 技术栈规范：
 * - 导航图拆分：根据业务模块将导航逻辑拆分为不同的扩展函数
 * - 返回栈管理：利用 popUpTo 和 inclusive 正确处理登录跳转，防止物理返回键导致的页面错乱
 * - 单例跳转：导航动作必须配置 launchSingleTop = true
 */
fun NavGraphBuilder.loginScreen() {
    composable<Login> {
        val controller = LocalNavController.current
        LoginScreen(
            viewModel = hiltViewModel<LoginViewModel>(),
            modifier = Modifier.fillMaxSize(),
            onNavigateToMain = {
                controller.navigate(Main) {
                    popUpTo(Login) { inclusive = true }
                    launchSingleTop = true
                }
            },
            onNavigateToRegister = {
                controller.navigate(Register) {
                    launchSingleTop = true
                }
            },
            onNavigateToForgotPassword = {
                controller.navigate(ForgotPassword) {
                    launchSingleTop = true
                }
            }
        )
    }
}
