package com.pulse.player.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pulse.player.data.local.datastore.PreferencesManager
import com.pulse.player.service.audiofx.AudioEffectsController
import com.pulse.player.service.controller.PlaybackConnection
import com.pulse.player.service.sleeptimer.SleepTimerManager
import com.pulse.player.ui.components.MiniPlayer
import com.pulse.player.ui.navigation.PulseBottomBar
import com.pulse.player.ui.navigation.PulseNavGraph
import com.pulse.player.ui.navigation.Screen
import com.pulse.player.ui.player.NowPlayingScreen
import com.pulse.player.ui.theme.PulseTheme
import com.pulse.player.ui.theme.ThemeMode
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var playbackConnection: PlaybackConnection

    @Inject
    lateinit var audioEffectsController: AudioEffectsController

    @Inject
    lateinit var sleepTimerManager: SleepTimerManager

    @Inject
    lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by preferencesManager.themeModeFlow.collectAsState(initial = ThemeMode.DARK)
            val fontScale by preferencesManager.fontScaleFlow.collectAsState(initial = 1.0f)
            val isGlassEnabled by preferencesManager.liquidGlassFlow.collectAsState(initial = true)

            PulseTheme(
                themeMode = themeMode,
                fontScale = fontScale,
                isGlassEnabled = isGlassEnabled
            ) {
                PulseAppContent(
                    playbackConnection = playbackConnection,
                    audioEffectsController = audioEffectsController,
                    sleepTimerManager = sleepTimerManager
                )
            }
        }
    }
}

@Composable
fun PulseAppContent(
    playbackConnection: PlaybackConnection,
    audioEffectsController: AudioEffectsController,
    sleepTimerManager: SleepTimerManager
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val playbackState by playbackConnection.playbackState.collectAsState()
    var isNowPlayingExpanded by remember { mutableStateOf(false) }

    val showBottomBar = currentRoute in Screen.bottomNavItems.map { it.route } && !isNowPlayingExpanded
    val hasActiveTrack = playbackState.currentTrack != null

    BackHandler(enabled = isNowPlayingExpanded) {
        isNowPlayingExpanded = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                if (showBottomBar) {
                    PulseBottomBar(
                        currentRoute = currentRoute,
                        onNavigateToRoute = { route ->
                            navController.navigate(route) {
                                popUpTo(Screen.Home.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding())
            ) {
                PulseNavGraph(
                    navController = navController,
                    playbackConnection = playbackConnection,
                    audioEffectsController = audioEffectsController,
                    sleepTimerManager = sleepTimerManager,
                    modifier = Modifier.fillMaxSize()
                )

                // Persistent Floating Mini Player
                if (hasActiveTrack && !isNowPlayingExpanded) {
                    MiniPlayer(
                        playbackState = playbackState,
                        onExpand = { isNowPlayingExpanded = true },
                        onPlayPause = { playbackConnection.playPause() },
                        onNext = { playbackConnection.skipToNext() },
                        onPrevious = { playbackConnection.smartPrevious() },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 4.dp)
                    )
                }
            }
        }

        // Full Screen Hero Now Playing Modal
        AnimatedVisibility(
            visible = isNowPlayingExpanded,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            NowPlayingScreen(
                playbackConnection = playbackConnection,
                onCollapse = { isNowPlayingExpanded = false },
                audioEffectsController = audioEffectsController,
                sleepTimerManager = sleepTimerManager
            )
        }
    }
}
