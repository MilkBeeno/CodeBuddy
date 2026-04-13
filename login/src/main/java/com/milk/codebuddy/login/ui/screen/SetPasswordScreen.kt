package com.milk.codebuddy.login.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.milk.codebuddy.base.ui.theme.AppTheme
import com.milk.codebuddy.base.ui.theme.LocalAppColors
import com.milk.codebuddy.base.ui.theme.LocalTypography

/**
 * 设置密码页 —— 对应设计稿 "Sign up - 验证码（Set Password）"
 *
 * 布局结构：
 * - 顶部：返回箭头
 * - 标题：Set Password
 * - 副标题：Create a secure password to protect your account
 * - Password 输入框（带密码可见切换）
 * - 密码规则说明（两行）
 * - Create Account / Complete 主按钮
 */
@Composable
fun SetPasswordScreen(
    password: String,
    isPasswordVisible: Boolean,
    isLoading: Boolean,
    canSubmit: Boolean,
    submitButtonText: String = "Create Account",
    passwordRuleHint1: String = "Must be 3–30 characters",
    passwordRuleHint2: String = "Include 2 types from: A-Z · a-z · 0-9 · ~!$#% etc.",
    isRule1Valid: Boolean = false,
    isRule2Valid: Boolean = false,
    errorMessage: String?,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onSubmitClick: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LocalAppColors.current.primaryBackgroundColor)
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

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
                    text = "Set Password",
                    style = LocalTypography.current.headlineSmall,
                    color = LocalAppColors.current.primaryTextColor
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Create a secure password to protect your account",
                    style = LocalTypography.current.bodyLarge,
                    color = LocalAppColors.current.auxiliaryTextColor
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 密码输入框
                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(
                            text = "Password",
                            style = LocalTypography.current.labelMedium
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = onPasswordVisibilityToggle) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                                tint = LocalAppColors.current.auxiliaryTextColor
                            )
                        }
                    },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
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

                Spacer(modifier = Modifier.height(8.dp))

                // 密码规则说明
                PasswordRuleItem(
                    text = passwordRuleHint1,
                    isValid = isRule1Valid
                )
                Spacer(modifier = Modifier.height(4.dp))
                PasswordRuleItem(
                    text = passwordRuleHint2,
                    isValid = isRule2Valid
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

                // 主按钮
                Button(
                    onClick = onSubmitClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = canSubmit && !isLoading,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LocalAppColors.current.primaryTextColor,
                        contentColor = LocalAppColors.current.primaryBackgroundColor,
                        disabledContainerColor = LocalAppColors.current.primaryTextColor.copy(alpha = 0.4f),
                        disabledContentColor = LocalAppColors.current.primaryBackgroundColor
                    )
                ) {
                    AnimatedContent(targetState = isLoading, label = "submit_button_state") { loading ->
                        if (loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = LocalAppColors.current.primaryBackgroundColor
                            )
                        } else {
                            Text(
                                text = submitButtonText,
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
private fun PasswordRuleItem(
    text: String,
    isValid: Boolean,
    modifier: Modifier = Modifier
) {
    val color = when {
        isValid -> LocalAppColors.current.successColor
        else -> LocalAppColors.current.auxiliaryTextColor
    }
    val prefix = if (isValid) "● " else "○ "
    Text(
        text = "$prefix$text",
        style = LocalTypography.current.bodySmall,
        color = color,
        modifier = modifier.fillMaxWidth()
    )
}

@Preview(showBackground = true, name = "Light - 初始状态")
@Preview(
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
    name = "Dark - 输入中"
)
@Composable
private fun SetPasswordScreenPreview() {
    AppTheme {
        SetPasswordScreen(
            password = "",
            isPasswordVisible = false,
            isLoading = false,
            canSubmit = false,
            submitButtonText = "Create Account",
            isRule1Valid = false,
            isRule2Valid = false,
            errorMessage = null,
            onPasswordChange = {},
            onPasswordVisibilityToggle = {},
            onSubmitClick = {},
            onNavigateBack = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(showBackground = true, name = "Light - 密码已满足规则")
@Composable
private fun SetPasswordScreenValidPreview() {
    AppTheme {
        SetPasswordScreen(
            password = "Abc@12345",
            isPasswordVisible = false,
            isLoading = false,
            canSubmit = true,
            submitButtonText = "Create Account",
            isRule1Valid = true,
            isRule2Valid = true,
            errorMessage = null,
            onPasswordChange = {},
            onPasswordVisibilityToggle = {},
            onSubmitClick = {},
            onNavigateBack = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}
