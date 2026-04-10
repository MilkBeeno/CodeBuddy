package com.milk.codebuddy.login.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.milk.codebuddy.base.ui.navigation.ForgotPassword
import com.milk.codebuddy.base.ui.navigation.LocalNavController
import com.milk.codebuddy.base.ui.navigation.Login
import com.milk.codebuddy.base.ui.navigation.ResetPassword
import com.milk.codebuddy.login.ui.screen.ResetPasswordScreen

/**
 * 注册 ResetPassword Screen 路由
 *
 * 技术栈规范：
 * - 类型安全路由：使用 composable<ResetPassword> 保证类型安全，phone 参数由路由承载
 * - 返回栈管理：重置成功后，清除整个找回密码流程的栈，直接回到登录页
 * - 单例跳转：launchSingleTop = true 防止重复进入
 */
fun NavGraphBuilder.resetPasswordScreen() {
    composable<ResetPassword> {
        val controller = LocalNavController.current
        ResetPasswordScreen(
            modifier = Modifier.fillMaxSize(),
            onNavigateBack = {
                controller.popBackStack()
            },
            onNavigateToLogin = {
                controller.navigate(Login) {
                    // 重置成功后清除找回密码整个流程（ForgotPassword + ResetPassword），回到 Login
                    popUpTo(ForgotPassword) { inclusive = true }
                    launchSingleTop = true
                }
            }
        )
    }
}
