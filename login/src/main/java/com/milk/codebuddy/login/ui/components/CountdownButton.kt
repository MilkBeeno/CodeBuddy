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
import androidx.compose.ui.unit.dp
import com.milk.codebuddy.base.ui.theme.LocalTypography
import com.milk.codebuddy.resource.R

/** 倒计时按钮背景：半透明黑（启动图之上固定叠加，不随主题变化） */
private val CountdownButtonBackground = Color(0x80000000)

/**
 * 倒计时按钮状态
 * 使用 @Immutable 标注以优化 Compose 重组性能
 */
@Immutable
data class CountdownButtonState(
    val remainingSeconds: Int = 0
)

/**
 * 启动页跳过倒计时按钮
 *
 * 该组件叠加在启动页图片之上，采用固定半透明黑背景 + 白色文字，不跟随主题。
 * 字体尺寸/粗细通过 [LocalTypography] 引用，禁止硬编码。
 *
 * @param remainingSeconds 倒计时剩余秒数
 * @param onSkip           点击跳过回调
 */
@Composable
fun CountdownButton(
    remainingSeconds: Int,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val typography = LocalTypography.current
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color = CountdownButtonBackground)
            .clickable(onClick = onSkip)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.login_splash_skip),
            color = Color.White,
            style = typography.bodyMedium
        )

        Spacer(modifier = Modifier.size(4.dp))

        Text(
            text = stringResource(R.string.login_splash_countdown, remainingSeconds),
            color = Color.White,
            style = typography.labelLarge
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
