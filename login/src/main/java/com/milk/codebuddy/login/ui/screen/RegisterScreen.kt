package com.milk.codebuddy.login.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import com.milk.codebuddy.login.ui.state.RegisterEffect
import com.milk.codebuddy.login.ui.state.RegisterState
import com.milk.codebuddy.login.ui.viewmodel.RegisterViewModel
import com.milk.codebuddy.resource.R

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = viewModel(),
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is RegisterEffect.NavigateToLogin -> onNavigateToLogin()
                is RegisterEffect.ShowToast -> { /* 处理 Toast */ }
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
            RegisterTopBar(onNavigateToLogin = onNavigateToLogin)

            Spacer(modifier = Modifier.height(24.dp))

            RegisterTitle()

            Spacer(modifier = Modifier.height(32.dp))

            RegisterInputs(
                uiState = uiState,
                onPhoneChange = viewModel::onPhoneChange,
                onCodeChange = viewModel::onCodeChange,
                onPasswordChange = viewModel::onPasswordChange,
                onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
                onPasswordVisibilityToggle = viewModel::onPasswordVisibilityToggle,
                onConfirmPasswordVisibilityToggle = viewModel::onConfirmPasswordVisibilityToggle,
                onSendCodeClick = viewModel::onSendCodeClick
            )

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedVisibility(visible = uiState.registerState is RegisterState.Error) {
                val errorState = uiState.registerState as? RegisterState.Error
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
                onClick = viewModel::onRegisterClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !uiState.isLoading && uiState.canRegister,
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
                        text = stringResource(R.string.register_button),
                        style = LocalTypography.current.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.register_agreement),
                style = LocalTypography.current.bodySmall,
                color = LocalAppColors.current.auxiliaryTextColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.register_to_login),
                style = LocalTypography.current.bodyMedium,
                color = LocalAppColors.current.primaryTextColor,
                modifier = Modifier.clickable(onClick = onNavigateToLogin)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun RegisterTopBar(onNavigateToLogin: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateToLogin) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = LocalAppColors.current.primaryTextColor
            )
        }
    }
}

@Composable
private fun RegisterTitle() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = stringResource(R.string.register_title),
            style = LocalTypography.current.headlineMedium,
            color = LocalAppColors.current.primaryTextColor
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.register_subtitle),
            style = LocalTypography.current.bodyLarge,
            color = LocalAppColors.current.secondaryTextColor
        )
    }
}

@Composable
private fun RegisterInputs(
    uiState: com.milk.codebuddy.login.ui.state.RegisterUiState,
    onPhoneChange: (String) -> Unit,
    onCodeChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onConfirmPasswordVisibilityToggle: () -> Unit,
    onSendCodeClick: () -> Unit
) {
    LoginTextField(
        value = uiState.phone,
        onValueChange = onPhoneChange,
        label = stringResource(R.string.register_phone),
        placeholder = stringResource(R.string.register_phone_hint),
        modifier = Modifier.fillMaxWidth(),
        isError = uiState.phoneError != null,
        errorMessage = uiState.phoneError?.let { stringResource(it) },
        keyboardType = KeyboardType.Phone
    )

    Spacer(modifier = Modifier.height(16.dp))

    LoginTextField(
        value = uiState.code,
        onValueChange = onCodeChange,
        label = stringResource(R.string.register_code),
        placeholder = stringResource(R.string.register_code_hint),
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
                            stringResource(R.string.register_code_countdown, uiState.countdownSeconds)
                        } else {
                            stringResource(R.string.register_code_send)
                        }
                    )
                }
            }
        }
    )

    Spacer(modifier = Modifier.height(16.dp))

    LoginTextField(
        value = uiState.password,
        onValueChange = onPasswordChange,
        label = stringResource(R.string.register_password),
        placeholder = stringResource(R.string.register_password_hint),
        modifier = Modifier.fillMaxWidth(),
        isError = uiState.passwordError != null,
        errorMessage = uiState.passwordError?.let { stringResource(it) },
        keyboardType = KeyboardType.Password,
        isPassword = true,
        passwordVisible = uiState.passwordVisible,
        onPasswordVisibilityToggle = onPasswordVisibilityToggle,
        trailingContent = {
            IconButton(onClick = onPasswordVisibilityToggle) {
                Icon(
                    imageVector = if (uiState.passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
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
        label = stringResource(R.string.register_confirm_password),
        placeholder = stringResource(R.string.register_confirm_password_hint),
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
fun RegisterScreenPreview() {
    AppTheme {
        RegisterScreen(
            onNavigateToLogin = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}
