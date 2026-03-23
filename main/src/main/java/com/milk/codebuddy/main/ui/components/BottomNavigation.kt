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
import androidx.compose.ui.res.stringResource
import com.milk.codebuddy.base.ui.theme.LocalAppColors
import com.milk.codebuddy.resource.R

enum class MainTab(
    val titleResId: Int,
    val icon: ImageVector
) {
    HOME(R.string.main_tab_home, Icons.Filled.Home),
    MESSAGE(R.string.main_tab_message, Icons.Filled.Analytics),
    PLAY(R.string.main_tab_play, Icons.Filled.EmojiEvents),
    MINE(R.string.main_tab_mine, Icons.Filled.Person)
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
            val title = stringResource(id = tab.titleResId)
            NavigationBarItem(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = title
                    )
                },
                label = {
                    Text(text = title)
                }
            )
        }
    }
}
