package com.bebetter.bemora.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bebetter.bemora.ui.discover.DiscoverScreen
import com.bebetter.bemora.ui.details.ContentDetailsScreen
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
            DiscoverScreen(onMovieClick = { item ->
                Screen.ContentDetails.routeFor(item)?.let { route ->
                    navController.navigate(route)
                }
            })
        }

        composable(Screen.Search.route) {
            SearchScreen(onContentClick = { item ->
                Screen.ContentDetails.routeFor(item)?.let { route ->
                    navController.navigate(route)
                }
            })
        }

        composable(Screen.Library.route) {
            LibraryScreen(onContentClick = { item ->
                Screen.ContentDetails.routeFor(item)?.let { route ->
                    navController.navigate(route)
                }
            })
        }

        composable(Screen.Profile.route) {
            ProfileScreen()
        }

        composable(
            route = Screen.ContentDetails.route,
            arguments = listOf(navArgument(Screen.ContentDetails.CONTENT_ID) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) {
            ContentDetailsScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.MovieDetails.route,
            arguments = listOf(navArgument(Screen.MovieDetails.MOVIE_ID) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) {
            ContentDetailsScreen(onBack = { navController.popBackStack() })
        }
    }
}
