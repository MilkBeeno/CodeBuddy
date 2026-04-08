package com.milk.codebuddy.base.ui.navigation

import kotlinx.serialization.Serializable

/**
 * 路由定义
 * 技术栈规范：
 * - 类型安全定义：所有页面路径必须定义为带有 @Serializable 注解的 Object 或 Data Class
 * - 导航图拆分：根据业务模块将导航逻辑拆分为不同的扩展函数
 * - 常量命名规范：所有 const val 必须使用 SCREAMING_SNAKE_CASE
 */
@Serializable
object Splash

@Serializable
object Login

@Serializable
object Main

@Serializable
object AddTransaction
