package com.milk.codebuddy.main.ui.screen

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.milk.codebuddy.base.ui.navigation.LocalNavController
import com.milk.codebuddy.base.ui.navigation.Screen
import com.milk.codebuddy.base.ui.theme.AppTheme
import com.milk.codebuddy.base.ui.theme.LocalAppColors
import com.milk.codebuddy.main.ui.components.BottomNavigation
import com.milk.codebuddy.main.ui.components.MainTab

@Composable
fun MainScreen(
    modifier: Modifier = Modifier
) {
    val navController = LocalNavController.current
    var selectedTab by remember { mutableStateOf(MainTab.HOME) }

    AppTheme {
        Scaffold(
            bottomBar = {
                BottomNavigation(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            },
            modifier = modifier,
            containerColor = LocalAppColors.current.primaryBackgroundColor,
            contentWindowInsets = WindowInsets(0.dp)
        ) { innerPadding ->
            when (selectedTab) {
                MainTab.HOME -> HomeScreen(
                    onAddTransactionClick = {
                        navController.navigate(Screen.AddTransaction.route)
                    },
                    modifier = Modifier.padding(innerPadding)
                )
                MainTab.MESSAGE -> MessageScreen(
                    modifier = Modifier.padding(innerPadding)
                )
                MainTab.PLAY -> PlayScreen(
                    modifier = Modifier.padding(innerPadding)
                )
                MainTab.MINE -> MineScreen(
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

