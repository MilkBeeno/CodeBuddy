package com.milk.codebuddy.login.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.milk.codebuddy.base.ui.navigation.ForgotPassword
import com.milk.codebuddy.base.ui.navigation.LocalNavController
import com.milk.codebuddy.base.ui.navigation.Login
import com.milk.codebuddy.base.ui.navigation.ResetPassword
import com.milk.codebuddy.login.ui.screen.ForgotPasswordScreen

/**
 * 注册 ForgotPassword Screen 路由
 *
 * 技术栈规范：
 * - 类型安全路由：使用 composable<ForgotPassword> 保证类型安全
 * - 返回栈管理：忘记密码页可直接 popBackStack 返回登录页
 * - 单例跳转：launchSingleTop = true 防止重复进入
 */
fun NavGraphBuilder.forgotPasswordScreen() {
    composable<ForgotPassword> {
        val controller = LocalNavController.current
        ForgotPasswordScreen(
            modifier = Modifier.fillMaxSize(),
            onNavigateBack = {
                controller.popBackStack()
            },
            onNavigateToResetPassword = { phone ->
                controller.navigate(ResetPassword(phone = phone)) {
                    launchSingleTop = true
                }
            }
        )
    }
}
