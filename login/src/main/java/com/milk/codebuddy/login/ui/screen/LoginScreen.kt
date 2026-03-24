package com.milk.codebuddy.login.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.milk.codebuddy.base.ui.theme.AppTheme
import com.milk.codebuddy.base.ui.theme.LocalAppColors
import com.milk.codebuddy.base.ui.theme.LocalTypography
import com.milk.codebuddy.login.ui.components.LoginTextField
import com.milk.codebuddy.login.ui.viewmodel.LoginViewModel
import com.milk.codebuddy.resource.R

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LocalAppColors.current.primaryBackgroundColor)
    ) {
        Column(
            modifier = Modifier
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
        LoginTextField(
            value = uiState.account,
            onValueChange = viewModel::onAccountChange,
            label = stringResource(R.string.login_account),
            placeholder = stringResource(R.string.login_account_hint),
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.accountError != null,
            errorMessage = uiState.accountError,
            keyboardType = KeyboardType.Email
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 密码输入框
        LoginTextField(
            value = uiState.password,
            onValueChange = viewModel::onPasswordChange,
            label = stringResource(R.string.login_password),
            placeholder = stringResource(R.string.login_password_hint),
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.passwordError != null,
            errorMessage = uiState.passwordError,
            keyboardType = KeyboardType.Password,
            isPassword = true,
            passwordVisible = uiState.passwordVisible,
            onPasswordVisibilityToggle = viewModel::onPasswordVisibilityToggle
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
                viewModel.onLoginClick(onLoginSuccess)
            },
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
            Text(
                text = if (uiState.isLoading) "登录中..." else stringResource(R.string.login_button),
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
