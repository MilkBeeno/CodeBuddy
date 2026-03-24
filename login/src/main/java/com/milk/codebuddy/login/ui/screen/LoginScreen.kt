package com.milk.codebuddy.login.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.milk.codebuddy.base.ui.theme.AppTheme
import com.milk.codebuddy.base.ui.theme.LocalAppColors
import com.milk.codebuddy.base.ui.theme.LocalTypography
import com.milk.codebuddy.login.ui.components.LoginTextField
import com.milk.codebuddy.login.ui.state.LoginState
import com.milk.codebuddy.login.ui.viewmodel.LoginViewModel
import com.milk.codebuddy.resource.R

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 处理登录成功
    LaunchedEffect(uiState.loginState) {
        if (uiState.loginState is LoginState.Success) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LocalAppColors.current.primaryBackgroundColor)
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
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

            // 手机号输入框
            LoginTextField(
                value = uiState.phone,
                onValueChange = viewModel::onPhoneChange,
                label = stringResource(R.string.login_phone),
                placeholder = stringResource(R.string.login_phone_hint),
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.phoneError != null,
                errorMessage = uiState.phoneError?.let { stringResource(it) },
                keyboardType = KeyboardType.Phone
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 验证码输入框 + 发送验证码按钮
            LoginTextField(
                value = uiState.code,
                onValueChange = viewModel::onCodeChange,
                label = stringResource(R.string.login_code),
                placeholder = stringResource(R.string.login_code_hint),
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.codeError != null,
                errorMessage = uiState.codeError?.let { stringResource(it) },
                keyboardType = KeyboardType.Number,
                trailingContent = {
                    OutlinedButton(
                        onClick = viewModel::onSendCodeClick,
                        enabled = !uiState.isCountingDown && !uiState.isSendingCode && uiState.phone.length == 11
                    ) {
                        if (uiState.isSendingCode) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .height(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (uiState.isCountingDown) {
                                    stringResource(R.string.login_code_countdown, uiState.countdownSeconds)
                                } else {
                                    stringResource(R.string.login_code_send)
                                }
                            )
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 错误提示
            if (uiState.loginState is LoginState.Error) {
                Text(
                    text = stringResource((uiState.loginState as LoginState.Error).messageResId),
                    style = LocalTypography.current.bodySmall,
                    color = androidx.compose.ui.graphics.Color.Red,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 登录按钮
            Button(
                onClick = { viewModel.onLoginClick(onLoginSuccess) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = LocalAppColors.current.primaryTextColor,
                    contentColor = LocalAppColors.current.primaryBackgroundColor,
                    disabledContainerColor = LocalAppColors.current.secondaryTextColor
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp),
                        strokeWidth = 2.dp,
                        color = LocalAppColors.current.primaryBackgroundColor
                    )
                } else {
                    Text(
                        text = stringResource(R.string.login_button),
                        style = LocalTypography.current.titleMedium
                    )
                }
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
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun LoginScreenPreview() {
    AppTheme {
        LoginScreen(
            onLoginSuccess = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}
