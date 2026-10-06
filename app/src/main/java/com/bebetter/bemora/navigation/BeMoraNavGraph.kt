package com.bebetter.bemora.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bebetter.bemora.ui.discover.DiscoverScreen
import com.bebetter.bemora.ui.library.LibraryScreen
import com.bebetter.bemora.ui.profile.ProfileScreen
import com.bebetter.bemora.ui.search.SearchScreen

@Composable
fun BeMoraNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Discover.route,
        modifier = modifier
    ) {
        composable(Screen.Discover.route) {
            DiscoverScreen()
        }

        composable(Screen.Search.route) {
            SearchScreen()
        }

        composable(Screen.Library.route) {
            LibraryScreen()
        }

        composable(Screen.Profile.route) {
            ProfileScreen()
        }
    }
}