package com.milk.codebuddy.login.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.milk.codebuddy.base.ui.theme.AppTheme
import com.milk.codebuddy.base.ui.theme.LocalAppColors
import com.milk.codebuddy.base.ui.theme.LocalTypography

/**
 * 邮箱登录页 —— 对应设计稿 "Log in - Email" (node: 110:4530)
 *
 * Figma 规格：
 * - 页面：414×896 白底，VERTICAL layout，pad=H16
 * - 顶部：关闭按钮 40×40 圆形 auxiliaryBackgroundColor
 * - 标题："Sign in with Email" headlineSmall
 * - 邮箱输入框：h=56 r=12 border=1px primaryTextColor
 * - 密码输入框：h=56 r=12 border=1px primaryTextColor，含 eye 图标
 * - Sign In 按钮：h=48 圆角 full primaryTextColor 白字 titleLarge
 * - "or" 分隔线
 * - 第三方登录：Google / Apple ID / Phone Number，各 h=48 圆角 full auxiliaryBackgroundColor
 * - 底部：Don't have an account? Sign up for free（accentColor）
 * - Forgot Password? secondaryTextColor 居中
 *
 * 注意：email / password / passwordVisible 状态由上层 ViewModel 管理（此处暂为 preview-only 组件，
 * 接入 ViewModel 时应将状态提升并删除 remember 块）。
 */
@Composable
fun LoginEmailScreen(
    onClose: () -> Unit = {},
    onSignIn: () -> Unit = {},
    onSignInWithGoogle: () -> Unit = {},
    onSignInWithApple: () -> Unit = {},
    onSignInWithPhone: () -> Unit = {},
    onSignUp: () -> Unit = {},
    onForgotPassword: () -> Unit = {},
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    // TODO: 接入真实 ViewModel 后将这三个状态提升到 ViewModel
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val colors = LocalAppColors.current
    val typography = LocalTypography.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.primaryBackgroundColor)
            .imePadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // ── 顶部关闭按钮 ─────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.auxiliaryBackgroundColor)
                        .clickable(onClick = onClose),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✕",
                        color = colors.primaryTextColor,
                        style = typography.titleSmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── 标题 ─────────────────────────────────────────────────────
            Text(
                text = "Sign in with Email",
                color = colors.primaryTextColor,
                style = typography.headlineSmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── 邮箱 + 密码输入框 ─────────────────────────────────────────
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                EmailInputField(email = email, onEmailChange = { email = it })
                EmailPasswordField(
                    password = password,
                    onPasswordChange = { password = it },
                    visible = passwordVisible,
                    onVisibilityToggle = { passwordVisible = !passwordVisible }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Sign In 按钮 ──────────────────────────────────────────────
            EmailSignInButton(
                isLoading = isLoading,
                onClick = onSignIn,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── "or" 分隔线 ───────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = colors.dividerColor,
                    thickness = 1.dp
                )
                Text(
                    text = "or",
                    color = colors.primaryTextColor,
                    style = typography.titleMedium
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = colors.dividerColor,
                    thickness = 1.dp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── 第三方登录按钮组 ──────────────────────────────────────────
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ThirdPartyLoginButton(
                    text = "Sign in with Google",
                    leadingContent = {
                        Text(
                            text = "G",
                            color = colors.googleRed,
                            style = typography.titleSmall
                        )
                    },
                    onClick = onSignInWithGoogle
                )
                ThirdPartyLoginButton(
                    text = "Sign in with Apple ID",
                    leadingContent = {
                        Text(text = "", color = colors.primaryTextColor, style = typography.titleSmall)
                    },
                    onClick = onSignInWithApple
                )
                ThirdPartyLoginButton(
                    text = "Sign in with Phone Number",
                    leadingContent = {
                        Text(text = "☎", color = colors.primaryTextColor, style = typography.titleSmall)
                    },
                    onClick = onSignInWithPhone
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── 注册跳转 ─────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account yet?",
                    color = colors.primaryTextColor,
                    style = typography.bodyLarge
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Sign up for free",
                    color = colors.accentColor,
                    style = typography.bodyLarge,
                    modifier = Modifier.clickable(onClick = onSignUp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Forgot Password ───────────────────────────────────────────
            Text(
                text = "Forgot Password?",
                color = colors.secondaryTextColor,
                style = typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onForgotPassword)
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

/**
 * 邮箱输入框
 * Figma: 382×56 [HORIZONTAL] r=12 border=1px primaryTextColor
 */
@Composable
private fun EmailInputField(
    email: String,
    onEmailChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val typography = LocalTypography.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .border(1.dp, colors.primaryTextColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(text = "Email", color = colors.auxiliaryTextColor, style = typography.labelMedium)
            BasicTextField(
                value = email,
                onValueChange = onEmailChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = typography.bodyLarge.copy(color = colors.primaryTextColor),
                cursorBrush = SolidColor(colors.primaryTextColor),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Box {
                        if (email.isEmpty()) {
                            Text(
                                text = "Enter email address",
                                color = colors.hintTextColor,
                                style = typography.bodyLarge
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}

/**
 * 密码输入框（含显示/隐藏按钮）
 * Figma: 382×56 [HORIZONTAL] r=12 border=1px primaryTextColor
 */
@Composable
private fun EmailPasswordField(
    password: String,
    onPasswordChange: (String) -> Unit,
    visible: Boolean,
    onVisibilityToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val typography = LocalTypography.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .border(1.dp, colors.primaryTextColor, RoundedCornerShape(12.dp))
            .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = "Password", color = colors.auxiliaryTextColor, style = typography.labelMedium)
            BasicTextField(
                value = password,
                onValueChange = onPasswordChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = typography.bodyLarge.copy(color = colors.primaryTextColor),
                cursorBrush = SolidColor(colors.primaryTextColor),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Box {
                        if (password.isEmpty()) {
                            Text(
                                text = "Enter password",
                                color = colors.hintTextColor,
                                style = typography.bodyLarge
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
        // Eye 图标
        Text(
            text = if (visible) "👁" else "👁‍🗨",
            style = typography.titleMedium,
            color = colors.secondaryTextColor,
            modifier = Modifier
                .size(20.dp)
                .clickable(onClick = onVisibilityToggle)
        )
    }
}

/**
 * 邮箱登录的 Sign In 按钮
 * Figma: 382×48 圆角 full bg=primaryTextColor 文字 primaryBackgroundColor
 */
@Composable
private fun EmailSignInButton(
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val typography = LocalTypography.current
    Surface(
        modifier = modifier
            .height(48.dp)
            .clip(CircleShape)
            .clickable(enabled = !isLoading, onClick = onClick),
        color = colors.primaryTextColor,
        shape = CircleShape
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = colors.primaryBackgroundColor,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Sign In",
                    color = colors.primaryBackgroundColor,
                    style = typography.titleLarge
                )
            }
        }
    }
}

/**
 * 第三方登录通用按钮
 * Figma: 382×48 圆角 full bg=auxiliaryBackgroundColor 文字 primaryTextColor
 */
@Composable
private fun ThirdPartyLoginButton(
    text: String,
    leadingContent: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val typography = LocalTypography.current
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        color = colors.auxiliaryBackgroundColor,
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
                color = colors.primaryTextColor,
                style = typography.titleLarge
            )
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// Preview
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Preview(showBackground = true, name = "Login Email - Light")
@Preview(
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
    name = "Login Email - Dark"
)
@Composable
private fun LoginEmailScreenPreview() {
    AppTheme {
        LoginEmailScreen()
    }
}
