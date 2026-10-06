package com.bebetter.bemora.navigation

import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val label: String,
    val screen: Screen,
    val icon: ImageVector
)