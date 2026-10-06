package com.bebetter.bemora.navigation

sealed class Screen(val route: String) {
    data object Discover : Screen("discover")
    data object Search : Screen("search")
    data object Library : Screen("library")
    data object Profile : Screen("profile")
}