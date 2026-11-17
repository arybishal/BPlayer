package com.pulse.player.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Home : Screen("home", "Home", Icons.Rounded.Home)
    object Library : Screen("library", "Library", Icons.Rounded.LibraryMusic)
    object Search : Screen("search", "Search", Icons.Rounded.Search)
    object Settings : Screen("settings", "Settings", Icons.Rounded.Settings)
    object ErrorLogs : Screen("error_logs", "Diagnostic Logs")
    object NowPlaying : Screen("now_playing", "Now Playing")

    companion object {
        val bottomNavItems = listOf(Home, Library, Search, Settings)
    }
}
