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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.milk.codebuddy.base.ui.theme.AppTheme

/**
 * 邮箱登录页 —— 对应设计稿 "Log in - Email" (node: 110:4530)
 *
 * Figma 规格：
 * - 页面：414×896 白底 #ffffff，VERTICAL layout，pad=H16
 * - 顶部：关闭按钮 40×40 圆形 #e3e6e8
 * - 标题："Sign in with Email" fs=24 fw=700 #333333
 * - 邮箱输入框：h=56 r=12 border=1px #000000
 * - 密码输入框：h=56 r=12 border=1px #000000，含 eye 图标
 * - Sign In 按钮：h=48 圆角 full #333333 白字 fs=18 fw=590
 * - "or" 分隔线
 * - 第三方登录：Google / Apple ID / Phone Number，各 h=48 圆角 full #e3e6e8
 * - 底部：Don't have an account? Sign up for free（蓝色 #1f72e8）
 * - Forgot Password? #666666 居中
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
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
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
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── 标题 ─────────────────────────────────────────────────────
            Text(
                text = "Sign in with Email",
                color = Color(0xFF333333),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 32.sp,
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
                // 邮箱输入框
                EmailInputField(
                    email = email,
                    onEmailChange = { email = it }
                )

                // 密码输入框
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
                    color = Color(0xFFE0E0E0),
                    thickness = 1.dp
                )
                Text(
                    text = "or",
                    color = Color(0xFF333333),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFE0E0E0),
                    thickness = 1.dp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── 第三方登录按钮组 ──────────────────────────────────────────
            // Figma: Frame 27 [VERTICAL] gap=12
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Sign in with Google
                ThirdPartyLoginButton(
                    text = "Sign in with Google",
                    leadingContent = {
                        Text(
                            text = "G",
                            color = Color(0xFFEA4335),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    onClick = onSignInWithGoogle
                )
                // Sign in with Apple ID
                ThirdPartyLoginButton(
                    text = "Sign in with Apple ID",
                    leadingContent = {
                        Text(text = "", color = Color(0xFF333333), fontSize = 16.sp)
                    },
                    onClick = onSignInWithApple
                )
                // Sign in with Phone Number
                ThirdPartyLoginButton(
                    text = "Sign in with Phone Number",
                    leadingContent = {
                        Text(text = "☎", color = Color(0xFF333333), fontSize = 16.sp)
                    },
                    onClick = onSignInWithPhone
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── 注册跳转 ─────────────────────────────────────────────────
            // Figma: Frame 1014 [HORIZONTAL] gap=4
            // "Don't have an account yet?" #333333  "Sign up for free" #1f72e8
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account yet?",
                    color = Color(0xFF333333),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Sign up for free",
                    color = Color(0xFF1F72E8),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.clickable(onClick = onSignUp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Forgot Password ───────────────────────────────────────────
            Text(
                text = "Forgot Password?",
                color = Color(0xFF666666),
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
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
 * Figma: 文本选中 382×56 [HORIZONTAL] gap=16 pad=T4R16B4L16 r=12 border=1px #000000
 */
@Composable
private fun EmailInputField(
    email: String,
    onEmailChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .border(1.dp, Color(0xFF000000), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "Email",
                color = Color(0xFF999999),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal
            )
            BasicTextField(
                value = email,
                onValueChange = onEmailChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    color = Color(0xFF333333),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal
                ),
                cursorBrush = SolidColor(Color(0xFF333333)),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Box {
                        if (email.isEmpty()) {
                            Text(
                                text = "Enter email address",
                                color = Color(0xFFCCCCCC),
                                fontSize = 16.sp
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
 * Figma: 文本选中 382×56 [HORIZONTAL] gap=8 pad=T4R16B4L16 r=12 border=1px #000000
 *        右侧：显示 INSTANCE 20×20
 */
@Composable
private fun EmailPasswordField(
    password: String,
    onPasswordChange: (String) -> Unit,
    visible: Boolean,
    onVisibilityToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .border(1.dp, Color(0xFF000000), RoundedCornerShape(12.dp))
            .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "Password",
                color = Color(0xFF999999),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal
            )
            BasicTextField(
                value = password,
                onValueChange = onPasswordChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    color = Color(0xFF333333),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal
                ),
                cursorBrush = SolidColor(Color(0xFF333333)),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Box {
                        if (password.isEmpty()) {
                            Text(
                                text = "Enter password",
                                color = Color(0xFFCCCCCC),
                                fontSize = 16.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }

        // Eye 图标 20×20
        Text(
            text = if (visible) "👁" else "👁‍🗨",
            fontSize = 18.sp,
            color = Color(0xFF666666),
            modifier = Modifier
                .size(20.dp)
                .clickable(onClick = onVisibilityToggle)
        )
    }
}

/**
 * 邮箱登录的 Sign In 按钮
 * Figma: 按钮 382×48 圆角 full bg=#333333 文字白 fs=18 fw=590
 */
@Composable
private fun EmailSignInButton(
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(48.dp)
            .clip(CircleShape)
            .clickable(enabled = !isLoading, onClick = onClick),
        color = Color(0xFF333333),
        shape = CircleShape
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Sign In",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight(590)
                )
            }
        }
    }
}

/**
 * 第三方登录通用按钮
 * Figma: 按钮 382×48 圆角 full bg=#e3e6e8 gap=4 pad=H16V4 文字 fs=18 fw=590 #333333
 */
@Composable
private fun ThirdPartyLoginButton(
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
