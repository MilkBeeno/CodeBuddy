package com.milk.codebuddy.base.ui.navigation

/**
 * 路由常量
 * 便于在 Navigation Compose 中使用
 * 
 * 技术栈规范：
 * - 类型安全定义：所有页面路径定义为常量
 * - 导航图拆分：根据业务模块将导航逻辑拆分为不同的扩展函数
 * - 常量命名规范：所有 const val 必须使用 SCREAMING_SNAKE_CASE
 */
object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val MAIN = "main"
    const val ADD_TRANSACTION = "add_transaction"
}
