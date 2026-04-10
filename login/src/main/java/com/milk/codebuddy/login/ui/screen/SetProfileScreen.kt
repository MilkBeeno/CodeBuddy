package com.milk.codebuddy.login.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.milk.codebuddy.base.ui.theme.AppTheme
import com.milk.codebuddy.base.ui.theme.LocalAppColors
import com.milk.codebuddy.base.ui.theme.LocalTypography

/**
 * 设置头像昵称页 —— 对应设计稿 "Sign up - 设置昵称密码（Welcome to Followme）"
 *
 * 布局结构：
 * - 顶部：返回箭头
 * - 标题：Welcome to Followme
 * - 副标题：Set up your profile...
 * - 头像选择（圆形 + 相机图标）
 * - Display Name 输入框 + 刷新随机昵称按钮
 * - Continue 主按钮
 * - Skip 次级按钮
 * - 底部提示文案
 */
@Composable
fun SetProfileScreen(
    displayName: String,
    avatarUrl: String?,
    isLoading: Boolean,
    canContinue: Boolean,
    onDisplayNameChange: (String) -> Unit,
    onAvatarClick: () -> Unit,
    onRefreshNickname: () -> Unit,
    onContinueClick: () -> Unit,
    onSkipClick: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                .verticalScroll(rememberScrollState())
        ) {
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
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // 标题
                Text(
                    text = "Welcome to Followme",
                    style = LocalTypography.current.headlineSmall,
                    color = LocalAppColors.current.primaryTextColor
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Set up your profile with a display name and avatar to start connecting with millions of real traders worldwide.",
                    style = LocalTypography.current.bodyLarge,
                    color = LocalAppColors.current.auxiliaryTextColor
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 头像选择
                AvatarSelector(
                    avatarUrl = avatarUrl,
                    onAvatarClick = onAvatarClick,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Display Name 输入框 + 随机刷新按钮
                OutlinedTextField(
                    value = displayName,
                    onValueChange = onDisplayNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(
                            text = "Display Name",
                            style = LocalTypography.current.labelMedium
                        )
                    },
                    placeholder = {
                        Text(
                            text = "Enter your display name",
                            style = LocalTypography.current.bodyMedium,
                            color = LocalAppColors.current.auxiliaryTextColor
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = onRefreshNickname) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Random nickname",
                                tint = LocalAppColors.current.auxiliaryTextColor
                            )
                        }
                    },
                    singleLine = true,
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

                Spacer(modifier = Modifier.height(32.dp))

                // Continue 主按钮
                Button(
                    onClick = onContinueClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = canContinue && !isLoading,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LocalAppColors.current.primaryTextColor,
                        contentColor = LocalAppColors.current.primaryBackgroundColor,
                        disabledContainerColor = LocalAppColors.current.primaryTextColor.copy(alpha = 0.4f),
                        disabledContentColor = LocalAppColors.current.primaryBackgroundColor
                    )
                ) {
                    AnimatedContent(targetState = isLoading, label = "continue_button_state") { loading ->
                        if (loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = LocalAppColors.current.primaryBackgroundColor
                            )
                        } else {
                            Text(
                                text = "Continue",
                                style = LocalTypography.current.titleMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Skip 次级按钮
                OutlinedButton(
                    onClick = onSkipClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = LocalAppColors.current.primaryTextColor
                    )
                ) {
                    Text(
                        text = "Skip",
                        style = LocalTypography.current.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 底部提示
                Text(
                    text = "A beautiful avatar and personalized nickname can lead to more interactions.",
                    style = LocalTypography.current.bodySmall,
                    color = LocalAppColors.current.auxiliaryTextColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun AvatarSelector(
    avatarUrl: String?,
    onAvatarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(90.dp)
            .clickable(onClick = onAvatarClick)
    ) {
        // 头像圆形区域
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(LocalAppColors.current.auxiliaryBackgroundColor)
                .border(
                    width = 1.dp,
                    color = LocalAppColors.current.auxiliaryBackgroundColor,
                    shape = CircleShape
                )
                .align(Alignment.TopStart),
            contentAlignment = Alignment.Center
        ) {
            if (avatarUrl == null) {
                // 默认占位头像
                Icon(
                    imageVector = Icons.Filled.CameraAlt,
                    contentDescription = null,
                    tint = LocalAppColors.current.auxiliaryTextColor,
                    modifier = Modifier.size(32.dp)
                )
            }
            // 实际项目中使用 Coil：
            // AsyncImage(model = avatarUrl, contentDescription = null, contentScale = ContentScale.Crop)
        }

        // 相机编辑角标
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(LocalAppColors.current.auxiliaryBackgroundColor)
                .border(
                    width = 2.dp,
                    color = LocalAppColors.current.primaryBackgroundColor,
                    shape = CircleShape
                )
                .align(Alignment.BottomEnd),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.CameraAlt,
                contentDescription = null,
                tint = LocalAppColors.current.primaryTextColor,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode"
)
@Composable
private fun SetProfileScreenPreview() {
    AppTheme {
        SetProfileScreen(
            displayName = "Harvey_007",
            avatarUrl = null,
            isLoading = false,
            canContinue = true,
            onDisplayNameChange = {},
            onAvatarClick = {},
            onRefreshNickname = {},
            onContinueClick = {},
            onSkipClick = {},
            onNavigateBack = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(showBackground = true, name = "Light - 空状态")
@Composable
private fun SetProfileScreenEmptyPreview() {
    AppTheme {
        SetProfileScreen(
            displayName = "",
            avatarUrl = null,
            isLoading = false,
            canContinue = false,
            onDisplayNameChange = {},
            onAvatarClick = {},
            onRefreshNickname = {},
            onContinueClick = {},
            onSkipClick = {},
            onNavigateBack = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}
