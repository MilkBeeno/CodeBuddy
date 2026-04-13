package com.milk.codebuddy.login.ui.screen

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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.milk.codebuddy.base.ui.theme.LocalAppColors
import com.milk.codebuddy.base.ui.theme.LocalTypography
import com.milk.codebuddy.login.ui.components.CountdownButton
import com.milk.codebuddy.resource.R
import kotlinx.coroutines.delay

private const val SPLASH_COUNTDOWN_SECONDS = 6
private const val SPLASH_TICK_DELAY_MS = 1000L

/**
 * 启动页
 * 检查用户会话状态，决定跳转到首页还是登录页
 */
@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToMain: () -> Unit,
    modifier: Modifier = Modifier
) {
    var remainingSeconds by rememberSaveable { mutableIntStateOf(SPLASH_COUNTDOWN_SECONDS) }
    val density = LocalDensity.current
    val statusBarHeight = WindowInsets.statusBars.getTop(density)

    LaunchedEffect(remainingSeconds) {
        if (remainingSeconds > 0) {
            delay(SPLASH_TICK_DELAY_MS)
            remainingSeconds--
        } else {
            // 倒计时结束，检查用户会话状态
            // 实际项目中应该检查 SessionManager 判断用户是否已登录
            onNavigateToLogin()
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.login_splash_app_name),
                style = LocalTypography.current.displaySmall,
                color = LocalAppColors.current.primaryTextColor
            )

            Text(
                text = stringResource(R.string.login_splash_slogan),
                style = LocalTypography.current.bodyLarge,
                color = LocalAppColors.current.auxiliaryTextColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        CountdownButton(
            remainingSeconds = remainingSeconds,
            onSkip = onNavigateToLogin,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(
                    top = with(density) { statusBarHeight.toDp() } + 16.dp,
                    end = 16.dp
                )
        )
    }
}

/**
 * 启动页预览
 */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Light Mode")
@androidx.compose.ui.tooling.preview.Preview(
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode"
)
@Composable
fun SplashScreenPreview() {
    com.milk.codebuddy.base.ui.theme.AppTheme {
        SplashScreen(
            onNavigateToLogin = {},
            onNavigateToMain = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}
