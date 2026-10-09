package com.bebetter.bemora.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bebetter.bemora.ui.discover.DiscoverScreen
import com.bebetter.bemora.ui.details.MovieDetailsScreen
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
                Screen.MovieDetails.routeFor(item)?.let { route ->
                    navController.navigate(route)
                }
            })
        }

        composable(Screen.Search.route) {
            SearchScreen(onMovieClick = { item ->
                Screen.MovieDetails.routeFor(item)?.let { route ->
                    navController.navigate(route)
                }
            })
        }

        composable(Screen.Library.route) {
            LibraryScreen()
        }

        composable(Screen.Profile.route) {
            ProfileScreen()
        }

        composable(
            route = Screen.MovieDetails.route,
            arguments = listOf(navArgument(Screen.MovieDetails.MOVIE_ID) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) {
            MovieDetailsScreen(onBack = { navController.popBackStack() })
        }
    }
}
