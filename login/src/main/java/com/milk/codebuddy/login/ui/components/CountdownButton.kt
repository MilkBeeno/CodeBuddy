package com.milk.codebuddy.login.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.milk.codebuddy.resource.R

/**
 * 倒计时按钮状态
 * 使用 @Immutable 标注以优化 Compose 重组性能
 */
@Immutable
data class CountdownButtonState(
    val remainingSeconds: Int = 0
)

/**
 * 倒计时按钮组件
 * 
 * 技术栈规范：
 * - 状态提升：Composable 必须尽可能"无状态"
 * - Material 3：优先使用 M3 组件库
 * - 交互反馈：点击操作必须包含波纹效果
 */
@Composable
fun CountdownButton(
    remainingSeconds: Int,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color = Color(0x80000000))
            .clickable(onClick = onSkip)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.login_splash_skip),
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal
        )

        Spacer(modifier = Modifier.size(4.dp))

        Text(
            text = stringResource(R.string.login_splash_countdown, remainingSeconds),
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.size(4.dp))

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = stringResource(R.string.login_splash_skip_desc),
            tint = Color.White,
            modifier = Modifier.size(16.dp)
        )
    }
}
