package com.milk.codebuddy.base.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 本地主题颜色
val LocalAppColors = staticCompositionLocalOf<AppColors> {
    error("No AppColors provided")
}

// 亮色主题应用颜色
internal val LightAppColors = AppColors(
    primaryTextColor = Color_333333,
    secondaryTextColor = Color_666666,
    auxiliaryTextColor = Color_999999,
    hintTextColor = Color_CCCCCC,
    primaryBackgroundColor = Color_FFFFFF,
    secondaryBackgroundColor = Color_F5F5F5,
    auxiliaryBackgroundColor = Color_E3E6E8,
    accentColor = Color_1F72E8,
    dividerColor = Color_E0E0E0,
    googleRed = Color_EA4335,
    successColor = Color_4CAF50
)

// 暗色主题应用颜色
internal val DarkAppColors = AppColors(
    primaryTextColor = Color_FFFFFF,
    secondaryTextColor = Color_999999,
    auxiliaryTextColor = Color_666666,
    hintTextColor = Color_999999,
    primaryBackgroundColor = Color_121212,
    secondaryBackgroundColor = Color_1E1E1E,
    auxiliaryBackgroundColor = Color_2D2D2D,
    accentColor = Color_1F72E8,
    dividerColor = Color_2D2D2D,
    googleRed = Color_EA4335,
    successColor = Color_4CAF50
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val appColors = if (darkTheme) DarkAppColors else LightAppColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // 让内容延伸到系统栏区域，由 WindowInsets 自行处理内边距
            WindowCompat.setDecorFitsSystemWindows(window, false)
            // 根据主题调整状态栏和导航栏图标颜色
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(
        LocalAppColors provides appColors,
        LocalTypography provides Typography
    ) {
        content()
    }
}
