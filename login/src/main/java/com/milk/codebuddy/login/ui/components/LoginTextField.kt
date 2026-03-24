package com.milk.codebuddy.login.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.milk.codebuddy.base.ui.theme.LocalAppColors
import com.milk.codebuddy.base.ui.theme.LocalTypography

/**
 * 登录输入框状态
 * 使用 @Immutable 标注以优化 Compose 重组性能
 */
@Immutable
data class LoginTextFieldState(
    val value: String = "",
    val label: String = "",
    val placeholder: String = "",
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val keyboardType: KeyboardType = KeyboardType.Text,
    val isPassword: Boolean = false,
    val passwordVisible: Boolean = false
)

/**
 * 登录输入框组件
 * 
 * 技术栈规范：
 * - 状态提升：Composable 必须尽可能"无状态"，将 State 和点击事件回调提升至调用者或 ViewModel
 * - Material 3：优先使用 M3 组件库，保持全局颜色和字体的 Theme 一致性
 * - 资源引用：所有 String/Color/Dimension 必须使用 stringResource() 等引用 API
 */
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
    onPasswordVisibilityToggle: () -> Unit = {},
    trailingContent: @Composable (() -> Unit)? = null
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
        trailingIcon = trailingContent,
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
