package com.milk.codebuddy.login.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.milk.codebuddy.base.ui.theme.LocalAppColors
import com.milk.codebuddy.base.ui.theme.LocalTypography

@Composable
fun LoginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordVisibilityToggle: () -> Unit = {}
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                style = LocalTypography.current.bodyMedium
            )
        },
        placeholder = {
            Text(
                text = placeholder,
                style = LocalTypography.current.bodySmall
            )
        },
        isError = isError,
        supportingText = {
            errorMessage?.let {
                Text(
                    text = it,
                    style = LocalTypography.current.bodySmall
                )
            }
        },
        trailingIcon = {
            if (isPassword) {
                IconButton(onClick = onPasswordVisibilityToggle) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "隐藏密码" else "显示密码",
                        tint = LocalAppColors.current.secondaryTextColor
                    )
                }
            }
        },
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = modifier,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = LocalAppColors.current.primaryTextColor,
            unfocusedTextColor = LocalAppColors.current.primaryTextColor,
            focusedBorderColor = LocalAppColors.current.primaryTextColor,
            unfocusedBorderColor = LocalAppColors.current.secondaryTextColor,
            errorBorderColor = Color.Red,
            errorTextColor = Color.Red,
            errorSupportingTextColor = Color.Red,
            focusedPlaceholderColor = LocalAppColors.current.secondaryTextColor,
            unfocusedPlaceholderColor = LocalAppColors.current.secondaryTextColor,
            cursorColor = LocalAppColors.current.primaryTextColor,
            focusedLabelColor = LocalAppColors.current.primaryTextColor,
            unfocusedLabelColor = LocalAppColors.current.secondaryTextColor,
            errorLabelColor = Color.Red
        )
    )
}
