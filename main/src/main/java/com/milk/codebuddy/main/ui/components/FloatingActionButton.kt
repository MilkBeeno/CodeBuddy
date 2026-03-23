package com.milk.codebuddy.main.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatOffsetStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.pointer.awaitFirstDown
import androidx.compose.ui.input.pointer.waitForUpOrCancellation
import androidx.compose.ui.input.pointer.awaitEachGesture
import com.milk.codebuddy.base.ui.theme.LocalAppColors
import com.milk.codebuddy.resource.R
import kotlin.math.roundToInt

@Composable
fun FloatingActionBall(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableFloatOffsetStateOf(0f, 0f) }
    var isDragging by remember { mutableStateOf(false) }
    var screenWidth by remember { mutableStateOf(0f) }
    var screenHeight by remember { mutableStateOf(0f) }
    var ballSize by remember { mutableStateOf(56f) }

    Box(
        modifier = modifier.onGloballyPositioned { coordinates ->
            screenWidth = coordinates.size.width.toFloat()
            screenHeight = coordinates.size.height.toFloat()
        }
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.x.roundToInt(), offsetY.y.roundToInt()) }
                .onGloballyPositioned { coordinates ->
                    ballSize = coordinates.size.width.toFloat()
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { isDragging = true },
                        onDragEnd = {
                            isDragging = false
                            val centerX = offsetX.x + ballSize / 2
                            if (centerX < screenWidth / 2) {
                                offsetX.x = 0f
                            } else {
                                offsetX.x = screenWidth - ballSize
                            }
                        }
                    ) { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount
                        if (offsetX.x < 0) offsetX.x = 0f
                        if (offsetX.x > screenWidth - ballSize) offsetX.x = screenWidth - ballSize
                        if (offsetX.y < 0) offsetY.y = 0f
                        if (offsetY.y > screenHeight - ballSize) offsetY.y = screenHeight - ballSize
                    }
                }
                .pointerInput(onClick) {
                    detectDragGestures(
                        onDragStart = { },
                        onDrag = { change, _ ->
                            change.consume()
                            isDragging = true
                        },
                        onDragEnd = {
                            isDragging = false
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

        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.x.roundToInt(), offsetY.y.roundToInt()) }
                .size(56.dp)
                .pointerInput(onClick) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        val up = waitForUpOrCancellation()
                        if (up != null && !isDragging) {
                            onClick()
                        }
                    }
                }
        )
    }
}
