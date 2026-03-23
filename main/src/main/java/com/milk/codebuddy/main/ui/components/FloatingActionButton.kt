package com.milk.codebuddy.main.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.milk.codebuddy.base.ui.theme.LocalAppColors
import com.milk.codebuddy.resource.R
import kotlin.math.roundToInt
import kotlin.math.sqrt

@Composable
fun FloatingActionBall(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var screenWidth by remember { mutableFloatStateOf(0f) }
    var screenHeight by remember { mutableFloatStateOf(0f) }
    var ballSize by remember { mutableFloatStateOf(56f) }
    var totalDragDistance by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                screenWidth = coordinates.size.width.toFloat()
                screenHeight = coordinates.size.height.toFloat()
            }
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .onGloballyPositioned { coordinates ->
                    ballSize = coordinates.size.width.toFloat()
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            totalDragDistance = 0f
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            totalDragDistance += sqrt(dragAmount.x * dragAmount.x + dragAmount.y * dragAmount.y)
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                            
                            // 限制拖拽范围
                            offsetX = offsetX.coerceIn(0f, screenWidth - ballSize)
                            offsetY = offsetY.coerceIn(0f, screenHeight - ballSize)
                        },
                        onDragEnd = {
                            // 吸附到屏幕边缘
                            val centerX = offsetX + ballSize / 2
                            if (centerX < screenWidth / 2) {
                                offsetX = 0f
                            } else {
                                offsetX = screenWidth - ballSize
                            }
                            
                            // 如果拖拽距离很小，视为点击
                            if (totalDragDistance < 10f) {
                                onClick()
                            }
                        }
                    )
                }
                .size(56.dp)
                .background(
                    color = LocalAppColors.current.primaryAccentColor,
                    shape = CircleShape
                )
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    ambientColor = Color.Black.copy(alpha = 0.3f),
                    spotColor = Color.Black.copy(alpha = 0.3f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.main_floating_button_desc),
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
