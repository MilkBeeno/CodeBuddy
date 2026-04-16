package com.milk.codebuddy.login.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.milk.codebuddy.base.ui.theme.AppTheme
import com.milk.codebuddy.base.ui.theme.LocalAppColors
import com.milk.codebuddy.base.ui.theme.LocalTypography
import com.milk.codebuddy.login.ui.components.LoginTextField
import com.milk.codebuddy.login.ui.state.ResetPasswordEffect
import com.milk.codebuddy.login.ui.state.ResetPasswordState
import com.milk.codebuddy.login.ui.viewmodel.ResetPasswordViewModel
import com.milk.codebuddy.resource.R

@Composable
fun ResetPasswordScreen(
    viewModel: ResetPasswordViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ResetPasswordEffect.NavigateToLogin -> onNavigateToLogin()
                is ResetPasswordEffect.NavigateBack -> onNavigateBack()
                is ResetPasswordEffect.ShowToast -> { /* 处理 Toast */ }
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
            ResetPasswordTopBar(onNavigateBack = onNavigateBack)

            Spacer(modifier = Modifier.height(24.dp))

            ResetPasswordTitle()

            Spacer(modifier = Modifier.height(32.dp))

            ResetPasswordInputs(
                uiState = uiState,
                onNewPasswordChange = viewModel::onNewPasswordChange,
                onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
                onNewPasswordVisibilityToggle = viewModel::onNewPasswordVisibilityToggle,
                onConfirmPasswordVisibilityToggle = viewModel::onConfirmPasswordVisibilityToggle
            )

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedVisibility(visible = uiState.resetPasswordState is ResetPasswordState.Error) {
                val errorState = uiState.resetPasswordState as? ResetPasswordState.Error
                errorState?.let {
                    Text(
                        text = stringResource(it.messageResId),
                        style = LocalTypography.current.bodySmall,
                        color = LocalAppColors.current.googleRed,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = viewModel::onSubmitClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !uiState.isLoading && uiState.canSubmit,
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
                        text = stringResource(R.string.reset_password_button),
                        style = LocalTypography.current.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ResetPasswordTopBar(onNavigateBack: () -> Unit) {
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
private fun ResetPasswordTitle() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = stringResource(R.string.reset_password_title),
            style = LocalTypography.current.headlineMedium,
            color = LocalAppColors.current.primaryTextColor
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.reset_password_subtitle),
            style = LocalTypography.current.bodyLarge,
            color = LocalAppColors.current.secondaryTextColor
        )
    }
}

@Composable
private fun ResetPasswordInputs(
    uiState: com.milk.codebuddy.login.ui.state.ResetPasswordUiState,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onNewPasswordVisibilityToggle: () -> Unit,
    onConfirmPasswordVisibilityToggle: () -> Unit
) {
    LoginTextField(
        value = uiState.newPassword,
        onValueChange = onNewPasswordChange,
        label = stringResource(R.string.reset_password_new),
        placeholder = stringResource(R.string.reset_password_new_hint),
        modifier = Modifier.fillMaxWidth(),
        isError = uiState.newPasswordError != null,
        errorMessage = uiState.newPasswordError?.let { stringResource(it) },
        keyboardType = KeyboardType.Password,
        isPassword = true,
        passwordVisible = uiState.newPasswordVisible,
        onPasswordVisibilityToggle = onNewPasswordVisibilityToggle,
        trailingContent = {
            IconButton(onClick = onNewPasswordVisibilityToggle) {
                Icon(
                    imageVector = if (uiState.newPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                    contentDescription = null,
                    tint = LocalAppColors.current.secondaryTextColor
                )
            }
        }
    )

    Spacer(modifier = Modifier.height(16.dp))

    LoginTextField(
        value = uiState.confirmPassword,
        onValueChange = onConfirmPasswordChange,
        label = stringResource(R.string.reset_password_confirm),
        placeholder = stringResource(R.string.reset_password_confirm_hint),
        modifier = Modifier.fillMaxWidth(),
        isError = uiState.confirmPasswordError != null,
        errorMessage = uiState.confirmPasswordError?.let { stringResource(it) },
        keyboardType = KeyboardType.Password,
        isPassword = true,
        passwordVisible = uiState.confirmPasswordVisible,
        onPasswordVisibilityToggle = onConfirmPasswordVisibilityToggle,
        trailingContent = {
            IconButton(onClick = onConfirmPasswordVisibilityToggle) {
                Icon(
                    imageVector = if (uiState.confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                    contentDescription = null,
                    tint = LocalAppColors.current.secondaryTextColor
                )
            }
        }
    )
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun ResetPasswordScreenPreview() {
    AppTheme {
        ResetPasswordScreen(
            onNavigateBack = {},
            onNavigateToLogin = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}
