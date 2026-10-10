package com.bebetter.bemora.navigation

import androidx.compose.ui.res.stringResource
import com.bebetter.bemora.R
import com.bebetter.bemora.ui.components.localizedLabel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BeMoraBottomBar(
    navController: NavHostController
) {
    val items = listOf(
        BottomNavItem(
            label = stringResource(R.string.discover),
            screen = Screen.Discover,
            icon = Icons.Default.Explore
        ),
        BottomNavItem(
            label = stringResource(R.string.search),
            screen = Screen.Search,
            icon = Icons.Default.Search
        ),
        BottomNavItem(
            label = stringResource(R.string.library),
            screen = Screen.Library,
            icon = Icons.Default.VideoLibrary
        ),
        BottomNavItem(
            label = stringResource(R.string.profile),
            screen = Screen.Profile,
            icon = Icons.Default.AccountCircle
        )
    )

    val currentRoute =
        navController.currentBackStackEntryAsState()
            .value
            ?.destination
            ?.route

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.screen.route,
                onClick = {
                    navController.navigate(item.screen.route) {
                        launchSingleTop = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(item.label)
                }
            )
        }
    }
}