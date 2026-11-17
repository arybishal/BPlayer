package com.pulse.player.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.pulse.player.service.audiofx.AudioEffectsController
import com.pulse.player.service.controller.PlaybackConnection
import com.pulse.player.service.sleeptimer.SleepTimerManager
import com.pulse.player.ui.home.HomeScreen
import com.pulse.player.ui.library.LibraryScreen
import com.pulse.player.ui.search.SearchScreen
import com.pulse.player.ui.settings.SettingsScreen
import com.pulse.player.ui.settings.logs.ErrorLogsScreen

@Composable
fun PulseNavGraph(
    navController: NavHostController,
    playbackConnection: PlaybackConnection,
    audioEffectsController: AudioEffectsController? = null,
    sleepTimerManager: SleepTimerManager? = null,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToLibrary = {
                    navController.navigate(Screen.Library.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Library.route) {
            LibraryScreen(
                onTrackClick = { track, queue ->
                    playbackConnection.playTrack(track, queue)
                },
                onPlayNext = { playbackConnection.playNext(it) },
                onAddToQueue = { playbackConnection.addToQueue(it) }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onTrackClick = { track, queue ->
                    playbackConnection.playTrack(track, queue)
                },
                onPlayNext = { playbackConnection.playNext(it) },
                onAddToQueue = { playbackConnection.addToQueue(it) }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateToErrorLogs = {
                    navController.navigate(Screen.ErrorLogs.route)
                },
                audioEffectsController = audioEffectsController,
                sleepTimerManager = sleepTimerManager
            )
        }

        composable(Screen.ErrorLogs.route) {
            ErrorLogsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
