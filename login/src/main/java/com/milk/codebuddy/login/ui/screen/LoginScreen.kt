package com.milk.codebuddy.login.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.remember
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
import com.milk.codebuddy.login.ui.state.LoginEffect
import com.milk.codebuddy.login.ui.state.LoginState
import com.milk.codebuddy.login.ui.viewmodel.LoginViewModel
import com.milk.codebuddy.resource.R

/**
 * 登录页面
 * 
 * 技术栈规范：
 * - 状态提升：Composable 必须尽可能"无状态"，将 State 和点击事件回调提升至 ViewModel
 * - MVI 订阅：UI 仅通过 collectAsStateWithLifecycle() 观察 UiState
 * - 单向数据流：UI 发出 Intent/Event，ViewModel 更新 State，UI 自动响应渲染
 * - 副作用限制：只能在 LaunchedEffect 中执行初始化或弹窗操作
 */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onNavigateToMain: () -> Unit,
    onNavigateToRegister: () -> Unit = {},
    onNavigateToForgotPassword: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 处理单次事件（副作用）
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is LoginEffect.NavigateToMain -> onNavigateToMain()
                is LoginEffect.NavigateToLogin -> { /* 不适用 */ }
                is LoginEffect.ShowToast -> { /* 处理 Toast */ }
            }
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
            LoginTitle()
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // 输入区域
            LoginInputs(
                uiState = uiState,
                onPhoneChange = viewModel::onPhoneChange,
                onCodeChange = viewModel::onCodeChange,
                onSendCodeClick = viewModel::onSendCodeClick
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 错误提示
            ErrorMessage(loginState = uiState.loginState)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 登录按钮
            LoginButton(
                isLoading = uiState.isLoading,
                canLogin = uiState.canLogin,
                onLoginClick = viewModel::onLoginClick
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 底部信息
            LoginFooter(
                onNavigateToRegister = onNavigateToRegister,
                onNavigateToForgotPassword = onNavigateToForgotPassword
            )
        }
    }
}

/**
 * 登录标题
 * 组件拆分：将复杂的嵌套 UI 拆分为多个小于 50 行的子 Composable 函数
 */
@Composable
private fun LoginTitle() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
    }
}

/**
 * 登录输入区域
 */
@Composable
private fun LoginInputs(
    uiState: com.milk.codebuddy.login.ui.state.LoginUiState,
    onPhoneChange: (String) -> Unit,
    onCodeChange: (String) -> Unit,
    onSendCodeClick: () -> Unit
) {
    // 手机号输入框
    LoginTextField(
        value = uiState.phone,
        onValueChange = onPhoneChange,
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
        onValueChange = onCodeChange,
        label = stringResource(R.string.login_code),
        placeholder = stringResource(R.string.login_code_hint),
        modifier = Modifier.fillMaxWidth(),
        isError = uiState.codeError != null,
        errorMessage = uiState.codeError?.let { stringResource(it) },
        keyboardType = KeyboardType.Number,
        trailingContent = {
            OutlinedButton(
                onClick = onSendCodeClick,
                enabled = uiState.canSendCode
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
}

/**
 * 错误提示
 */
@Composable
private fun ErrorMessage(loginState: LoginState) {
    if (loginState is LoginState.Error) {
        Text(
            text = stringResource(loginState.messageResId),
            style = LocalTypography.current.bodySmall,
            color = androidx.compose.ui.graphics.Color.Red,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * 登录按钮
 */
@Composable
private fun LoginButton(
    isLoading: Boolean,
    canLogin: Boolean,
    onLoginClick: () -> Unit
) {
    Button(
        onClick = onLoginClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        enabled = !isLoading && canLogin,
        colors = ButtonDefaults.buttonColors(
            containerColor = LocalAppColors.current.primaryTextColor,
            contentColor = LocalAppColors.current.primaryBackgroundColor,
            disabledContainerColor = LocalAppColors.current.secondaryTextColor
        )
    ) {
        if (isLoading) {
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
}

/**
 * 登录页面底部信息
 */
@Composable
private fun LoginFooter(
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    Text(
        text = stringResource(R.string.login_register),
        style = LocalTypography.current.bodyMedium,
        color = LocalAppColors.current.primaryTextColor,
        modifier = Modifier.clickable(onClick = onNavigateToRegister)
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = stringResource(R.string.login_forgot_password),
        style = LocalTypography.current.bodyMedium,
        color = LocalAppColors.current.primaryTextColor,
        modifier = Modifier.clickable(onClick = onNavigateToForgotPassword)
    )

    Spacer(modifier = Modifier.height(32.dp))

    Text(
        text = stringResource(R.string.login_other_methods),
        style = LocalTypography.current.bodySmall,
        color = LocalAppColors.current.secondaryTextColor
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = stringResource(R.string.login_agreement),
        style = LocalTypography.current.bodySmall,
        color = LocalAppColors.current.secondaryTextColor
    )
}

/**
 * 预览
 * 生成预览：包含 Light/Dark Mode 的 @Preview 代码块
 */
@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun LoginScreenPreview() {
    AppTheme {
        LoginScreen(
            onNavigateToMain = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}
