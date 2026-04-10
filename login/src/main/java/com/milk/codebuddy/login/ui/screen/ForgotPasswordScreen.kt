package com.milk.codebuddy.login.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.milk.codebuddy.login.ui.state.ForgotPasswordEffect
import com.milk.codebuddy.login.ui.state.ForgotPasswordState
import com.milk.codebuddy.login.ui.viewmodel.ForgotPasswordViewModel
import com.milk.codebuddy.resource.R

@Composable
fun ForgotPasswordScreen(
    viewModel: ForgotPasswordViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToResetPassword: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ForgotPasswordEffect.NavigateToResetPassword -> onNavigateToResetPassword(effect.phone)
                is ForgotPasswordEffect.NavigateBack -> onNavigateBack()
                is ForgotPasswordEffect.ShowToast -> { /* 处理 Toast */ }
            }
        }
    }

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
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ForgotPasswordTopBar(onNavigateBack = onNavigateBack)

            Spacer(modifier = Modifier.height(24.dp))

            ForgotPasswordTitle()

            Spacer(modifier = Modifier.height(32.dp))

            ForgotPasswordInputs(
                uiState = uiState,
                onPhoneChange = viewModel::onPhoneChange,
                onCodeChange = viewModel::onCodeChange,
                onSendCodeClick = viewModel::onSendCodeClick
            )

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedVisibility(visible = uiState.forgotPasswordState is ForgotPasswordState.Error) {
                val errorState = uiState.forgotPasswordState as? ForgotPasswordState.Error
                errorState?.let {
                    Text(
                        text = stringResource(it.messageResId),
                        style = LocalTypography.current.bodySmall,
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = viewModel::onNextClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !uiState.isLoading && uiState.canNext,
                colors = ButtonDefaults.buttonColors(
                    containerColor = LocalAppColors.current.primaryTextColor,
                    contentColor = LocalAppColors.current.primaryBackgroundColor,
                    disabledContainerColor = LocalAppColors.current.secondaryTextColor
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = LocalAppColors.current.primaryBackgroundColor
                    )
                } else {
                    Text(
                        text = stringResource(R.string.forgot_password_next),
                        style = LocalTypography.current.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.forgot_password_back),
                style = LocalTypography.current.bodyMedium,
                color = LocalAppColors.current.primaryTextColor,
                modifier = Modifier.clickable(onClick = onNavigateBack)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ForgotPasswordTopBar(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = LocalAppColors.current.primaryTextColor
            )
        }
    }
}

@Composable
private fun ForgotPasswordTitle() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = stringResource(R.string.forgot_password_title),
            style = LocalTypography.current.headlineMedium,
            color = LocalAppColors.current.primaryTextColor
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.forgot_password_subtitle),
            style = LocalTypography.current.bodyLarge,
            color = LocalAppColors.current.secondaryTextColor
        )
    }
}

@Composable
private fun ForgotPasswordInputs(
    uiState: com.milk.codebuddy.login.ui.state.ForgotPasswordUiState,
    onPhoneChange: (String) -> Unit,
    onCodeChange: (String) -> Unit,
    onSendCodeClick: () -> Unit
) {
    LoginTextField(
        value = uiState.phone,
        onValueChange = onPhoneChange,
        label = stringResource(R.string.forgot_password_phone),
        placeholder = stringResource(R.string.forgot_password_phone_hint),
        modifier = Modifier.fillMaxWidth(),
        isError = uiState.phoneError != null,
        errorMessage = uiState.phoneError?.let { stringResource(it) },
        keyboardType = KeyboardType.Phone
    )

    Spacer(modifier = Modifier.height(16.dp))

    LoginTextField(
        value = uiState.code,
        onValueChange = onCodeChange,
        label = stringResource(R.string.forgot_password_code),
        placeholder = stringResource(R.string.forgot_password_code_hint),
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
                            stringResource(R.string.forgot_password_code_countdown, uiState.countdownSeconds)
                        } else {
                            stringResource(R.string.forgot_password_code_send)
                        }
                    )
                }
            }
        }
    )
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun ForgotPasswordScreenPreview() {
    AppTheme {
        ForgotPasswordScreen(
            onNavigateBack = {},
            onNavigateToResetPassword = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}
