package com.milk.codebuddy.main.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.milk.codebuddy.base.ui.theme.LocalTypography

@Composable
fun PlayScreen(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "娱乐",
                style = LocalTypography.current.headlineMedium
            )
            Text(
                text = "敬请期待",
                style = LocalTypography.current.bodyLarge,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}
