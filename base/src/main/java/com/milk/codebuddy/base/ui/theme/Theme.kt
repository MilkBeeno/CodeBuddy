package com.milk.codebuddy.base.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.WindowInsetsCompat

// 本地主题颜色
val LocalAppColors = staticCompositionLocalOf<AppColors> {
    error("No AppColors provided")
}

// 亮色主题应用颜色
internal val LightAppColors = AppColors(
    primaryTextColor = Color_000000,
    secondlyTextColor = Color_666666,
    auxiliaryTextColor = Color_999999,
    primaryBackgroundColor = Color_FFFFFF,
    secondlyBackgroundColor = Color_F5F5F5,
    auxiliaryBackgroundColor = Color_E0E0E0
)

// 暗色主题应用颜色
internal val DarkAppColors = AppColors(
    primaryTextColor = Color_FFFFFF,
    secondlyTextColor = Color_999999,
    auxiliaryTextColor = Color_666666,
    primaryBackgroundColor = Color_121212,
    secondlyBackgroundColor = Color_1E1E1E,
    auxiliaryBackgroundColor = Color_2D2D2D
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val appColors = if (darkTheme) DarkAppColors else LightAppColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as Activity).window

        // 设置状态栏背景色（使用 WindowInsetsControllerCompat）
        window.statusBarColor = appColors.primaryBackgroundColor.toArgb()

        // 根据主题设置状态栏图标颜色
        val insetsController = WindowCompat.getInsetsController(window, view)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            insetsController.isAppearanceLightStatusBars = !darkTheme
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
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
