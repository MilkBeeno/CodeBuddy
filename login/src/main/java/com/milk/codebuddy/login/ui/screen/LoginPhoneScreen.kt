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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
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
 * 手机号登录页 —— 对应设计稿 "Log in - Phone number" (node: 110:4124)
 *
 * Figma 规格：
 * - 页面：414×896 白底，VERTICAL layout
 * - 顶部：关闭按钮 40×40 圆形 auxiliaryBackgroundColor
 * - 标题："Sign in with Phone Number" headlineSmall
 * - 电话输入框：h=60 r=12 border=1px primaryTextColor，内含区号选择 + 电话号码输入
 * - 密码/验证码输入框：h=56 r=12 border=1px primaryTextColor
 * - Sign In 按钮：h=48 圆角 full primaryTextColor 白字 titleLarge
 * - 底部：Forgot Password? secondaryTextColor 居中
 */
@Composable
fun LoginPhoneScreen(
    onClose: () -> Unit = {},
    onRegionCodeClick: () -> Unit = {},
    onSignIn: () -> Unit = {},
    onForgotPassword: () -> Unit = {},
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    // TODO: 接入真实 ViewModel 后将这三个状态提升到 ViewModel
    var phone by remember { mutableStateOf("") }
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

            // ── 顶部关闭按钮 ──────────────────────────────────────────────
            LoginPhoneTopBar(onClose = onClose)

            Spacer(modifier = Modifier.height(16.dp))

            // ── 标题 ─────────────────────────────────────────────────────
            Text(
                text = "Sign in with Phone Number",
                color = colors.primaryTextColor,
                style = typography.headlineSmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── 输入区 ────────────────────────────────────────────────────
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PhoneNumberInput(
                    phone = phone,
                    onPhoneChange = { phone = it },
                    regionCode = "+65",
                    onRegionCodeClick = onRegionCodeClick
                )
                PasswordInput(
                    password = password,
                    onPasswordChange = { password = it },
                    visible = passwordVisible,
                    onVisibilityToggle = { passwordVisible = !passwordVisible }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Sign In 按钮 ──────────────────────────────────────────────
            SignInButton(
                isLoading = isLoading,
                onClick = onSignIn,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Forgot Password ───────────────────────────────────────────
            Text(
                text = "Forgot Password?",
                color = colors.secondaryTextColor,
                style = typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onForgotPassword),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

/**
 * 顶部关闭按钮 TopBar
 */
@Composable
private fun LoginPhoneTopBar(
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val typography = LocalTypography.current
    Row(
        modifier = modifier.fillMaxWidth(),
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
}

/**
 * 手机号输入框（含区号选择）
 * Figma: 382×60 [HORIZONTAL] r=12 border=1px primaryTextColor
 */
@Composable
private fun PhoneNumberInput(
    phone: String,
    onPhoneChange: (String) -> Unit,
    regionCode: String,
    onRegionCodeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val typography = LocalTypography.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .border(1.dp, colors.primaryTextColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(text = "Phone Number", color = colors.auxiliaryTextColor, style = typography.labelMedium)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 区号选择 pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(colors.auxiliaryBackgroundColor)
                        .clickable(onClick = onRegionCodeClick)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = regionCode,
                        color = colors.primaryTextColor,
                        style = typography.titleSmall
                    )
                }
                // 手机号输入
                BasicTextField(
                    value = phone,
                    onValueChange = onPhoneChange,
                    modifier = Modifier.weight(1f),
                    textStyle = typography.bodyLarge.copy(color = colors.primaryTextColor),
                    cursorBrush = SolidColor(colors.primaryTextColor),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        Box {
                            if (phone.isEmpty()) {
                                Text(
                                    text = "Enter phone number",
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
}

/**
 * 密码/验证码输入框
 * Figma: 382×56 [HORIZONTAL] r=12 border=1px primaryTextColor
 */
@Composable
private fun PasswordInput(
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
            .padding(start = 16.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BasicTextField(
            value = password,
            onValueChange = onPasswordChange,
            modifier = Modifier.weight(1f),
            textStyle = typography.bodyLarge.copy(color = colors.primaryTextColor),
            cursorBrush = SolidColor(colors.primaryTextColor),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box {
                    if (password.isEmpty()) {
                        Text(
                            text = "Enter password / verification code",
                            color = colors.hintTextColor,
                            style = typography.bodyLarge
                        )
                    }
                    innerTextField()
                }
            }
        )
        // 显示/隐藏密码
        Text(
            text = if (visible) "👁" else "👁‍🗨",
            style = typography.titleMedium,
            modifier = Modifier
                .size(20.dp)
                .clickable(onClick = onVisibilityToggle),
            color = colors.secondaryTextColor
        )
    }
}

/**
 * Sign In 按钮
 * Figma: 382×48 圆角 full bg=primaryTextColor 文字 primaryBackgroundColor
 */
@Composable
private fun SignInButton(
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
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
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

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// Preview
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Preview(showBackground = true, name = "Login Phone - Light")
@Preview(
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
    name = "Login Phone - Dark"
)
@Composable
private fun LoginPhoneScreenPreview() {
    AppTheme {
        LoginPhoneScreen()
    }
}
