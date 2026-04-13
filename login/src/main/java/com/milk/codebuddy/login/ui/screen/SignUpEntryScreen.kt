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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.milk.codebuddy.base.ui.theme.AppTheme
import com.milk.codebuddy.base.ui.theme.LocalAppColors
import com.milk.codebuddy.base.ui.theme.LocalTypography

/**
 * 注册入口页 —— 对应设计稿 "Sign up - Unified Login" (node: 110:3659)
 *
 * Figma 规格：
 * - 页面尺寸：375×812，白色背景 #ffffff
 * - 顶部工具栏：关闭按钮（40×40 圆形 #e3e6e8）+ 地区选择按钮（圆角 full #e3e6e8）
 * - 标题：fs=24 fw=700 #333333，副标题：fs=16 fw=400 #666666
 * - 注册按钮：高度 48dp，圆角 full，背景 #e3e6e8，文字 fs=18 fw=590 #333333
 * - 分隔线："or" 文字 fs=18 #666666，两侧线条
 * - 底部跳转：普通文字 + 蓝色链接 #1f72e8
 * - 隐私协议：fs=14 #666666 居中
 */
@Composable
fun SignUpEntryScreen(
    onClose: () -> Unit = {},
    onRegionClick: () -> Unit = {},
    onSignUpWithEmail: () -> Unit = {},
    onSignUpWithGoogle: () -> Unit = {},
    onSignUpWithApple: () -> Unit = {},
    onNavigateToSignIn: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ── 顶部工具栏 ──────────────────────────────────────────────────
            SignUpTopBar(
                onClose = onClose,
                onRegionClick = onRegionClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp, bottom = 12.dp)
            )

            // ── 标题区域 ────────────────────────────────────────────────────
            SignUpHeader(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── 注册按钮组 ──────────────────────────────────────────────────
            SignUpButtonGroup(
                onSignUpWithEmail = onSignUpWithEmail,
                onSignUpWithGoogle = onSignUpWithGoogle,
                onSignUpWithApple = onSignUpWithApple,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── "or" 分隔线 ─────────────────────────────────────────────────
            SignUpOrDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── 已有账号跳转 ────────────────────────────────────────────────
            SignInLink(
                onNavigateToSignIn = onNavigateToSignIn,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── 隐私协议 ────────────────────────────────────────────────────
            SignUpPrivacyText(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// 子组件
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

/**
 * 顶部工具栏：左侧关闭按钮 + 右侧地区选择
 * Figma: 编组 5 / 343×64 [HORIZONTAL] pad=T12R0B12L0
 */
@Composable
private fun SignUpTopBar(
    onClose: () -> Unit,
    onRegionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 关闭按钮 40×40 圆形 #e3e6e8
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFE3E6E8))
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✕",
                color = Color(0xFF333333),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // 地区选择按钮 h=32 圆角 full #e3e6e8 gap=4 pad=H8V4
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .background(Color(0xFFE3E6E8))
                .clickable(onClick = onRegionClick)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 国旗 20×20 圆形占位（实际替换为真实国旗图片）
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF007B3E))
            )
            Text(
                text = "Singapore",
                color = Color(0xFF333333),
                fontSize = 14.sp,
                fontWeight = FontWeight(510)
            )
            // 下拉箭头
            Text(
                text = "▾",
                color = Color(0xFF333333),
                fontSize = 12.sp
            )
        }
    }
}

/**
 * 标题区域
 * Figma: Frame 29 [VERTICAL] gap=4
 * - 主标题：fs=24 fw=700 #333333
 * - 副标题：fs=16 fw=400 #666666
 */
@Composable
private fun SignUpHeader(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Start for free.\nConnect with millions of real traders worldwide.",
            color = Color(0xFF333333),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 32.sp
        )
        Text(
            text = "Connect, share, and improve together",
            color = Color(0xFF666666),
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 24.sp
        )
    }
}

/**
 * 注册按钮组
 * Figma: Frame 27 [VERTICAL] gap=12
 * 按钮规格：h=48 圆角 full #e3e6e8 gap=4 pad=H16V4 文字 fs=18 fw=590
 */
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
        SignUpSocialButton(
            text = "Sign up with email",
            leadingContent = {
                // 邮件图标占位
                Text(text = "✉", color = Color(0xFF231815), fontSize = 16.sp)
            },
            onClick = onSignUpWithEmail
        )
        SignUpSocialButton(
            text = "Sign up with Google",
            leadingContent = {
                // Google 图标占位（实际使用 painterResource）
                Text(
                    text = "G",
                    color = Color(0xFFEA4335),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            onClick = onSignUpWithGoogle
        )
        SignUpSocialButton(
            text = "Sign up with Apple ID",
            leadingContent = {
                // Apple 图标占位
                Text(text = "", color = Color(0xFF333333), fontSize = 16.sp)
            },
            onClick = onSignUpWithApple
        )
    }
}

/**
 * 通用社交登录按钮
 * 规格：fillMaxWidth h=48 圆角 full #e3e6e8 gap=4 pad=H16V4
 */
@Composable
private fun SignUpSocialButton(
    text: String,
    leadingContent: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        color = Color(0xFFE3E6E8),
        shape = CircleShape
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                leadingContent()
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = Color(0xFF333333),
                fontSize = 18.sp,
                fontWeight = FontWeight(590)
            )
        }
    }
}

/**
 * "or" 分隔线
 * Figma: Frame 8 [HORIZONTAL] gap=10，两侧 RECTANGLE 1px #000000（实际用灰色）
 */
@Composable
private fun SignUpOrDivider(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = Color(0xFFE0E0E0),
            thickness = 1.dp
        )
        Text(
            text = "or",
            color = Color(0xFF666666),
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = Color(0xFFE0E0E0),
            thickness = 1.dp
        )
    }
}

/**
 * 已有账号跳转行
 * Figma: Frame 1014 [HORIZONTAL] gap=4
 * - "Already have an account?" #333333 fs=16
 * - "Sign in" #1f72e8 fs=16
 */
@Composable
private fun SignInLink(
    onNavigateToSignIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Already have an account?",
            color = Color(0xFF333333),
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "Sign in",
            color = Color(0xFF1F72E8),
            fontSize = 16.sp,
            fontWeight = FontWeight(510),
            modifier = Modifier.clickable(onClick = onNavigateToSignIn)
        )
    }
}

/**
 * 隐私协议文案
 * Figma: 隐私协议 INSTANCE fs=14 #666666 居中
 */
@Composable
private fun SignUpPrivacyText(modifier: Modifier = Modifier) {
    Text(
        text = "By proceeding, you agree to the Terms and Conditions and Privacy Policy",
        color = Color(0xFF666666),
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        textAlign = TextAlign.Center,
        lineHeight = 20.sp,
        modifier = modifier
    )
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// Preview
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Preview(showBackground = true, name = "Sign Up Entry - Light")
@Preview(
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
    name = "Sign Up Entry - Dark"
)
@Composable
private fun SignUpEntryScreenPreview() {
    AppTheme {
        SignUpEntryScreen()
    }
}
