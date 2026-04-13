package com.milk.codebuddy.login.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.milk.codebuddy.base.ui.theme.AppTheme
import com.milk.codebuddy.base.ui.theme.LocalAppColors
import com.milk.codebuddy.base.ui.theme.LocalTypography

/**
 * 邮箱验证码页 —— 对应设计稿 "Sign up - 验证码"
 *
 * 布局结构：
 * - 顶部：返回箭头
 * - 标题：Verify Email Address
 * - 副标题：Send the code, check your inbox...
 * - Email 只读展示框
 * - Verification Code 输入框 + Send Code / 倒计时 / Resend 按钮
 * - Verify Email 主按钮
 */
@Composable
fun VerifyEmailScreen(
    email: String,
    verificationCode: String,
    isCodeSent: Boolean,
    isSendingCode: Boolean,
    isCountingDown: Boolean,
    countdownSeconds: Int,
    isVerifying: Boolean,
    canVerify: Boolean,
    errorMessage: String?,
    onCodeChange: (String) -> Unit,
    onSendCodeClick: () -> Unit,
    onVerifyClick: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LocalAppColors.current.primaryBackgroundColor)
            .statusBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // 返回按钮
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = LocalAppColors.current.primaryTextColor
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // 标题
                Text(
                    text = "Verify Email Address",
                    style = LocalTypography.current.headlineSmall,
                    color = LocalAppColors.current.primaryTextColor
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Send the code, check your inbox, and enter it to complete verification",
                    style = LocalTypography.current.bodyLarge,
                    color = LocalAppColors.current.auxiliaryTextColor
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Email 只读展示框
                EmailDisplayField(email = email)

                Spacer(modifier = Modifier.height(16.dp))

                // 验证码输入框 + Send Code 按钮
                VerificationCodeField(
                    code = verificationCode,
                    isCountingDown = isCountingDown,
                    countdownSeconds = countdownSeconds,
                    isSendingCode = isSendingCode,
                    isCodeSent = isCodeSent,
                    onCodeChange = onCodeChange,
                    onSendCodeClick = onSendCodeClick
                )

                // 错误提示
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        style = LocalTypography.current.bodySmall,
                        color = LocalAppColors.current.googleRed,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Verify Email 主按钮
                Button(
                    onClick = onVerifyClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = canVerify && !isVerifying,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LocalAppColors.current.primaryTextColor,
                        contentColor = LocalAppColors.current.primaryBackgroundColor,
                        disabledContainerColor = LocalAppColors.current.primaryTextColor.copy(alpha = 0.4f),
                        disabledContentColor = LocalAppColors.current.primaryBackgroundColor
                    )
                ) {
                    AnimatedContent(targetState = isVerifying, label = "verify_button_state") { loading ->
                        if (loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = LocalAppColors.current.primaryBackgroundColor
                            )
                        } else {
                            Text(
                                text = "Verify Email",
                                style = LocalTypography.current.titleMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun EmailDisplayField(
    email: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = LocalAppColors.current.auxiliaryBackgroundColor,
                shape = RoundedCornerShape(12.dp)
            )
            .background(LocalAppColors.current.primaryBackgroundColor)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column {
            Text(
                text = "Email",
                style = LocalTypography.current.labelMedium,
                color = LocalAppColors.current.auxiliaryTextColor
            )
            if (email.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = email,
                    style = LocalTypography.current.bodyLarge,
                    color = LocalAppColors.current.primaryTextColor
                )
            }
        }
    }
}

@Composable
private fun VerificationCodeField(
    code: String,
    isCountingDown: Boolean,
    countdownSeconds: Int,
    isSendingCode: Boolean,
    isCodeSent: Boolean,
    onCodeChange: (String) -> Unit,
    onSendCodeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = code,
        onValueChange = onCodeChange,
        modifier = modifier.fillMaxWidth(),
        label = {
            Text(
                text = "Verification Code",
                style = LocalTypography.current.labelMedium
            )
        },
        placeholder = {
            Text(
                text = "Enter verification code",
                style = LocalTypography.current.bodyMedium
            )
        },
        trailingIcon = {
            SendCodeButton(
                isCountingDown = isCountingDown,
                countdownSeconds = countdownSeconds,
                isSendingCode = isSendingCode,
                isCodeSent = isCodeSent,
                onSendCodeClick = onSendCodeClick
            )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = LocalAppColors.current.primaryTextColor,
            unfocusedTextColor = LocalAppColors.current.primaryTextColor,
            focusedBorderColor = LocalAppColors.current.auxiliaryBackgroundColor,
            unfocusedBorderColor = LocalAppColors.current.auxiliaryBackgroundColor,
            focusedLabelColor = LocalAppColors.current.auxiliaryTextColor,
            unfocusedLabelColor = LocalAppColors.current.auxiliaryTextColor,
            cursorColor = LocalAppColors.current.primaryTextColor
        )
    )
}

@Composable
private fun SendCodeButton(
    isCountingDown: Boolean,
    countdownSeconds: Int,
    isSendingCode: Boolean,
    isCodeSent: Boolean,
    onSendCodeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = Triple(isCountingDown, isSendingCode, isCodeSent),
        label = "send_code_state",
        modifier = modifier
    ) { (counting, sending, sent) ->
        when {
            sending -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(18.dp),
                    strokeWidth = 2.dp,
                    color = LocalAppColors.current.auxiliaryTextColor
                )
            }

            counting -> {
                // 倒计时：灰色文字 "59s"
                Text(
                    text = "${countdownSeconds}s",
                    style = LocalTypography.current.labelLarge,
                    color = LocalAppColors.current.auxiliaryTextColor,
                    modifier = Modifier.padding(end = 12.dp)
                )
            }

            sent -> {
                // 可重新发送：显示 "Resend"
                TextButton(
                    onClick = onSendCodeClick,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Text(
                        text = "Resend",
                        style = LocalTypography.current.labelLarge,
                        color = LocalAppColors.current.primaryTextColor
                    )
                }
            }

            else -> {
                // 初始状态：Send Code 边框按钮
                TextButton(
                    onClick = onSendCodeClick,
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .border(
                            width = 1.dp,
                            color = LocalAppColors.current.primaryTextColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        )
                ) {
                    Text(
                        text = "Send Code",
                        style = LocalTypography.current.labelLarge,
                        color = LocalAppColors.current.primaryTextColor
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Light - 初始状态")
@Preview(
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
    name = "Dark - 输入中"
)
@Composable
private fun VerifyEmailScreenPreview() {
    AppTheme {
        VerifyEmailScreen(
            email = "frederick@gmail.com",
            verificationCode = "",
            isCodeSent = false,
            isSendingCode = false,
            isCountingDown = false,
            countdownSeconds = 0,
            isVerifying = false,
            canVerify = false,
            errorMessage = null,
            onCodeChange = {},
            onSendCodeClick = {},
            onVerifyClick = {},
            onNavigateBack = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(showBackground = true, name = "Light - 倒计时状态")
@Composable
private fun VerifyEmailScreenCountdownPreview() {
    AppTheme {
        VerifyEmailScreen(
            email = "frederick@gmail.com",
            verificationCode = "5567",
            isCodeSent = true,
            isSendingCode = false,
            isCountingDown = true,
            countdownSeconds = 59,
            isVerifying = false,
            canVerify = true,
            errorMessage = null,
            onCodeChange = {},
            onSendCodeClick = {},
            onVerifyClick = {},
            onNavigateBack = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}
