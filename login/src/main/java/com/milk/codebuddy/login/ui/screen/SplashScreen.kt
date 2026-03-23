package com.milk.codebuddy.login.ui.screen

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.milk.codebuddy.base.ui.theme.Color_6200EE
import com.milk.codebuddy.base.ui.theme.Color_BB86FC
import com.milk.codebuddy.base.ui.theme.LocalAppColors
import com.milk.codebuddy.login.ui.components.CountdownButton

@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    var remainingSeconds by remember { mutableStateOf(6) }
    val density = LocalDensity.current
    val statusBarHeight = WindowInsets.statusBars.getTop(density)

    // 倒计时逻辑
    LaunchedEffect(remainingSeconds) {
        if (remainingSeconds > 0) {
            kotlinx.coroutines.delay(1000) // 每秒更新
            remainingSeconds--
        } else {
            onNavigateToHome()
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Logo 或应用名称
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "CodeBuddy",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSystemInDarkTheme()) Color_BB86FC else Color_6200EE
            )

            Text(
                text = "股票投资，智胜未来",
                fontSize = 16.sp,
                color = LocalAppColors.current.auxiliaryTextColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        // 倒计时按钮（添加状态栏 padding）
        CountdownButton(
            remainingSeconds = remainingSeconds,
            onSkip = onNavigateToHome,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(
                    top = with(density) { statusBarHeight.toDp() } + 16.dp,
                    end = 16.dp
                )
        )
    }
}
