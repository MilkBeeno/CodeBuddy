package com.milk.codebuddy.base.ui.navigation

import kotlinx.serialization.Serializable

/**
 * 类型安全路由定义
 * 所有页面路径使用 @Serializable 注解
 */
@Serializable
data object SplashRoute {
    const val route: String = "splash"
}

@Serializable
data object LoginRoute {
    const val route: String = "login"
}

@Serializable
data object MainRoute {
    const val route: String = "main"
}

@Serializable
data class AddTransactionRoute(
    val transactionId: String? = null
) {
    companion object {
        const val route: String = "add_transaction"
    }
}

/**
 * 路由常量（推荐使用）
 * 便于在 Navigation Compose 中使用
 */
object Routes {
    const val Splash = "splash"
    const val Login = "login"
    const val Main = "main"
    const val AddTransaction = "add_transaction"
}

/**
 * 兼容旧版 Screen 类（已废弃，建议迁移到 Routes）
 */
@Deprecated(
    message = "使用 Routes 常量替代",
    replaceWith = ReplaceWith("Routes.Splash")
)
sealed class Screen(val route: String) {
    @Deprecated("使用 Routes.Splash 替代")
    data object Splash : Screen(Routes.Splash)
    
    @Deprecated("使用 Routes.Login 替代")
    data object Login : Screen(Routes.Login)
    
    @Deprecated("使用 Routes.Main 替代")
    data object Main : Screen(Routes.Main)
    
    @Deprecated("使用 Routes.AddTransaction 替代")
    data object AddTransaction : Screen(Routes.AddTransaction)
}
