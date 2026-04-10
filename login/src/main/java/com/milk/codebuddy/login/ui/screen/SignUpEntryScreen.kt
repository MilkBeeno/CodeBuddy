package com.milk.codebuddy.login.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.milk.codebuddy.base.ui.theme.AppTheme
import com.milk.codebuddy.base.ui.theme.LocalAppColors
import com.milk.codebuddy.base.ui.theme.LocalTypography
import com.milk.codebuddy.resource.R

/**
 * 注册入口页 —— 对应设计稿 "Sign up - Unified Login"
 *
 * 布局结构：
 * - 顶部区域：地区选择按钮（Singapore）
 * - 内容区：FollowMe Logo + 标题 + 副标题
 * - 按钮区：Sign up with email / Google / Apple ID
 * - 分隔线：or
 * - 底部：Already have an account? Sign in
 * - 隐私协议文案
 */
@Composable
fun SignUpEntryScreen(
    onNavigateToSignUpEmail: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    onSignUpWithGoogle: () -> Unit = {},
    onSignUpWithApple: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LocalAppColors.current.primaryBackgroundColor)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // 顶部：地区选择
            SignUpRegionSelector(
                region = "Singapore",
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )

            // Logo + 标题区域
            SignUpHeader(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 32.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // 注册按钮列表
            SignUpButtonGroup(
                onSignUpWithEmail = onNavigateToSignUpEmail,
                onSignUpWithGoogle = onSignUpWithGoogle,
                onSignUpWithApple = onSignUpWithApple,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // "or" 分隔线
            SignUpDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 已有账号 Sign in
            SignUpLoginLink(
                onNavigateToSignIn = onNavigateToSignIn,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 隐私协议
            SignUpPrivacyText(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SignUpRegionSelector(
    region: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(LocalAppColors.current.auxiliaryBackgroundColor)
            .clickable { }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // 国旗占位（实际项目中替换为真实 Flag 图片）
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(Color(0xFF007B3E))
        )
        Text(
            text = region,
            style = LocalTypography.current.labelLarge,
            color = LocalAppColors.current.secondaryTextColor
        )
        // 下拉箭头
        Text(
            text = "▾",
            style = LocalTypography.current.labelSmall,
            color = LocalAppColors.current.secondaryTextColor
        )
    }
}

@Composable
private fun SignUpHeader(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start
    ) {
        // FollowMe Logo 占位（实际项目中使用 Image(painterResource(...))）
        Box(
            modifier = Modifier
                .size(width = 140.dp, height = 40.dp)
                .background(
                    color = LocalAppColors.current.auxiliaryBackgroundColor,
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "FollowMe",
                style = LocalTypography.current.titleLarge,
                color = LocalAppColors.current.primaryTextColor,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Start for free. \nConnect with millions of real traders worldwide.",
            style = LocalTypography.current.headlineSmall,
            color = LocalAppColors.current.primaryTextColor
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Connect, share, and improve together",
            style = LocalTypography.current.bodyLarge,
            color = LocalAppColors.current.auxiliaryTextColor
        )
    }
}

@Composable
private fun SignUpButtonGroup(
    onSignUpWithEmail: () -> Unit,
    onSignUpWithGoogle: () -> Unit,
    onSignUpWithApple: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Sign up with email
        SignUpOptionButton(
            text = "Sign up with email",
            onClick = onSignUpWithEmail,
            leadingIconSlot = {
                // email 图标占位
                Text(text = "✉", color = LocalAppColors.current.primaryTextColor)
            }
        )

        // Sign up with Google
        SignUpOptionButton(
            text = "Sign up with Google",
            onClick = onSignUpWithGoogle,
            leadingIconSlot = {
                Text(text = "G", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold)
            }
        )

        // Sign up with Apple ID
        SignUpOptionButton(
            text = "Sign up with Apple ID",
            onClick = onSignUpWithApple,
            leadingIconSlot = {
                Text(text = "", color = LocalAppColors.current.primaryTextColor)
            }
        )
    }
}

@Composable
private fun SignUpOptionButton(
    text: String,
    onClick: () -> Unit,
    leadingIconSlot: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = CircleShape,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = LocalAppColors.current.auxiliaryBackgroundColor,
            contentColor = LocalAppColors.current.primaryTextColor
        ),
        border = null
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            leadingIconSlot()
            Text(
                text = text,
                style = LocalTypography.current.titleMedium,
                color = LocalAppColors.current.primaryTextColor
            )
        }
    }
}

@Composable
private fun SignUpDivider(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = LocalAppColors.current.auxiliaryBackgroundColor
        )
        Text(
            text = "or",
            style = LocalTypography.current.bodyMedium,
            color = LocalAppColors.current.auxiliaryTextColor
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = LocalAppColors.current.auxiliaryBackgroundColor
        )
    }
}

@Composable
private fun SignUpLoginLink(
    onNavigateToSignIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Already have an account? ",
            style = LocalTypography.current.bodyLarge,
            color = LocalAppColors.current.primaryTextColor
        )
        Text(
            text = "Sign in",
            style = LocalTypography.current.titleMedium,
            color = LocalAppColors.current.primaryTextColor,
            modifier = Modifier.clickable(onClick = onNavigateToSignIn)
        )
    }
}

@Composable
private fun SignUpPrivacyText(modifier: Modifier = Modifier) {
    Text(
        text = "By proceeding, you agree to the Terms and Conditions and Privacy Policy",
        style = LocalTypography.current.bodySmall,
        color = LocalAppColors.current.auxiliaryTextColor,
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode"
)
@Composable
private fun SignUpEntryScreenPreview() {
    AppTheme {
        SignUpEntryScreen(
            onNavigateToSignUpEmail = {},
            onNavigateToSignIn = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}
