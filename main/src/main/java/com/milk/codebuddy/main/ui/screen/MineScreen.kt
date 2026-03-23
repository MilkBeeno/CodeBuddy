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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.milk.codebuddy.base.ui.theme.LocalTypography
import com.milk.codebuddy.resource.R

@Composable
fun MineScreen(
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
                text = stringResource(R.string.main_mine_title),
                style = LocalTypography.current.headlineMedium
            )
            Text(
                text = stringResource(R.string.main_mine_subtitle),
                style = LocalTypography.current.bodyLarge,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}
