package com.milk.codebuddy.main.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.milk.codebuddy.base.ui.theme.LocalAppColors

enum class MainTab(
    val title: String,
    val icon: ImageVector
) {
    HOME("首页", Icons.Filled.Home),
    MESSAGE("分析", Icons.Filled.Analytics),
    PLAY("战绩", Icons.Filled.EmojiEvents),
    MINE("我的", Icons.Filled.Person)
}

@Composable
fun BottomNavigation(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        containerColor = LocalAppColors.current.primaryBackgroundColor,
        modifier = modifier
    ) {
        MainTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title
                    )
                },
                label = {
                    Text(text = tab.title)
                }
            )
        }
    }
}
