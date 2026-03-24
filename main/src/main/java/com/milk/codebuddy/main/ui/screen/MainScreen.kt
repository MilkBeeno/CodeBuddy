package com.milk.codebuddy.main.ui.screen

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.milk.codebuddy.base.ui.navigation.LocalNavController
import com.milk.codebuddy.base.ui.navigation.Routes
import com.milk.codebuddy.base.ui.theme.AppTheme
import com.milk.codebuddy.base.ui.theme.LocalAppColors
import com.milk.codebuddy.main.ui.components.BottomNavigation
import com.milk.codebuddy.main.ui.components.MainTab

@Composable
fun MainScreen(
    modifier: Modifier = Modifier
) {
    val navController = LocalNavController.current
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.HOME) }

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
            contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)
        ) { innerPadding ->
            when (selectedTab) {
                MainTab.HOME -> HomeScreen(
                    onAddTransactionClick = {
                        navController.navigate(Routes.AddTransaction)
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

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun MainScreenPreview() {
    AppTheme {
        MainScreen(modifier = Modifier.fillMaxSize())
    }
}

