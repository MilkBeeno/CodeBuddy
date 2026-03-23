package com.milk.codebuddy.login.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.milk.codebuddy.base.ui.theme.LocalAppColors
import com.milk.codebuddy.base.ui.theme.LocalTypography
import com.milk.codebuddy.resource.R

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var account by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var accountError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 标题
        Text(
            text = stringResource(R.string.login_title),
            style = LocalTypography.current.headlineLarge,
            color = LocalAppColors.current.primaryTextColor
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.login_subtitle),
            style = LocalTypography.current.bodyLarge,
            color = LocalAppColors.current.secondaryTextColor
        )

        Spacer(modifier = Modifier.height(48.dp))

        // 账号输入框
        OutlinedTextField(
            value = account,
            onValueChange = {
                account = it
                accountError = null
            },
            label = {
                Text(
                    text = stringResource(R.string.login_account),
                    style = LocalTypography.current.bodyMedium
                )
            },
            placeholder = {
                Text(
                    text = stringResource(R.string.login_account_hint),
                    style = LocalTypography.current.bodySmall
                )
            },
            isError = accountError != null,
            supportingText = {
                accountError?.let {
                    Text(
                        text = it,
                        style = LocalTypography.current.bodySmall
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = LocalAppColors.current.primaryTextColor,
                unfocusedTextColor = LocalAppColors.current.primaryTextColor,
                focusedBorderColor = LocalAppColors.current.primaryTextColor,
                unfocusedBorderColor = LocalAppColors.current.secondaryTextColor,
                errorBorderColor = androidx.compose.ui.graphics.Color.Red,
                errorTextColor = androidx.compose.ui.graphics.Color.Red,
                errorSupportingTextColor = androidx.compose.ui.graphics.Color.Red,
                focusedPlaceholderColor = LocalAppColors.current.secondaryTextColor,
                unfocusedPlaceholderColor = LocalAppColors.current.secondaryTextColor,
                cursorColor = LocalAppColors.current.primaryTextColor,
                focusedLabelColor = LocalAppColors.current.primaryTextColor,
                unfocusedLabelColor = LocalAppColors.current.secondaryTextColor,
                errorLabelColor = androidx.compose.ui.graphics.Color.Red
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 密码输入框
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = null
            },
            label = {
                Text(
                    text = stringResource(R.string.login_password),
                    style = LocalTypography.current.bodyMedium
                )
            },
            placeholder = {
                Text(
                    text = stringResource(R.string.login_password_hint),
                    style = LocalTypography.current.bodySmall
                )
            },
            isError = passwordError != null,
            supportingText = {
                passwordError?.let {
                    Text(
                        text = it,
                        style = LocalTypography.current.bodySmall
                    )
                }
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "隐藏密码" else "显示密码",
                        tint = LocalAppColors.current.secondaryTextColor
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = LocalAppColors.current.primaryTextColor,
                unfocusedTextColor = LocalAppColors.current.primaryTextColor,
                focusedBorderColor = LocalAppColors.current.primaryTextColor,
                unfocusedBorderColor = LocalAppColors.current.secondaryTextColor,
                errorBorderColor = androidx.compose.ui.graphics.Color.Red,
                errorTextColor = androidx.compose.ui.graphics.Color.Red,
                errorSupportingTextColor = androidx.compose.ui.graphics.Color.Red,
                focusedPlaceholderColor = LocalAppColors.current.secondaryTextColor,
                unfocusedPlaceholderColor = LocalAppColors.current.secondaryTextColor,
                cursorColor = LocalAppColors.current.primaryTextColor,
                focusedLabelColor = LocalAppColors.current.primaryTextColor,
                unfocusedLabelColor = LocalAppColors.current.secondaryTextColor,
                errorLabelColor = androidx.compose.ui.graphics.Color.Red
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 忘记密码
        Text(
            text = stringResource(R.string.login_forget_password),
            color = LocalAppColors.current.primaryTextColor,
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 4.dp)
                .align(Alignment.End)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 登录按钮
        Button(
            onClick = {
                // 验证输入
                if (account.isBlank()) {
                    accountError = "请输入账号"
                    return@Button
                }
                if (password.isBlank()) {
                    passwordError = "请输入密码"
                    return@Button
                }

                // 登录逻辑
                isLoading = true
                // TODO: 实际登录请求
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    isLoading = false
                    onLoginSuccess()
                }, 1000)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = LocalAppColors.current.primaryTextColor,
                contentColor = LocalAppColors.current.primaryBackgroundColor,
                disabledContainerColor = LocalAppColors.current.secondaryTextColor
            )
        ) {
            Text(
                text = if (isLoading) "登录中..." else stringResource(R.string.login_button),
                style = LocalTypography.current.titleMedium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 注册账号
        Text(
            text = stringResource(R.string.login_register),
            style = LocalTypography.current.bodyMedium,
            color = LocalAppColors.current.primaryTextColor
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 其他登录方式
        Text(
            text = stringResource(R.string.login_other_methods),
            style = LocalTypography.current.bodySmall,
            color = LocalAppColors.current.secondaryTextColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 用户协议
        Text(
            text = stringResource(R.string.login_agreement),
            style = LocalTypography.current.bodySmall,
            color = LocalAppColors.current.secondaryTextColor
        )
    }
}
