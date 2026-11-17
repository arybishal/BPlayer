package com.pulse.player.ui.recognition

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Hearing
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.pulse.player.domain.model.RecognizedSong
import com.pulse.player.domain.model.Track
import com.pulse.player.domain.repository.RecognitionState
import com.pulse.player.ui.theme.LocalExtendedColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongRecognitionSheet(
    onDismiss: () -> Unit,
    onPlayTrack: (Track) -> Unit = {},
    onSearchLibrary: (String) -> Unit = {},
    onOpenSettings: () -> Unit = {},
    viewModel: SongRecognitionViewModel = hiltViewModel()
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val extendedColors = LocalExtendedColors.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onPermissionResult(isGranted)
    }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        viewModel.onPermissionResult(granted)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                !state.hasPermission -> {
                    MicrophonePermissionCard(
                        onRequestPermission = {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    )
                }

                state.state is RecognitionState.Recording || state.state is RecognitionState.Analyzing -> {
                    ListeningView(
                        isAnalyzing = state.state is RecognitionState.Analyzing,
                        countdown = state.recordingCountdown,
                        amplitude = state.amplitude,
                        onCancel = {
                            viewModel.cancelListening()
                            onDismiss()
                        }
                    )
                }

                state.state is RecognitionState.Success -> {
                    val song = (state.state as RecognitionState.Success).song
                    RecognitionResultView(
                        song = song,
                        onPlayLocal = { track ->
                            onPlayTrack(track)
                            onDismiss()
                        },
                        onSearchLibrary = { query ->
                            onSearchLibrary(query)
                            onDismiss()
                        },
                        onIdentifyAgain = { viewModel.startListening() },
                        onDone = onDismiss
                    )
                }

                state.state is RecognitionState.NoMatch -> {
                    val message = (state.state as RecognitionState.NoMatch).message
                    RecognitionFailedView(
                        title = "No Match Found",
                        message = message,
                        onRetry = { viewModel.startListening() },
                        onClose = onDismiss
                    )
                }

                state.state is RecognitionState.Error -> {
                    val err = state.state as RecognitionState.Error
                    RecognitionFailedView(
                        title = "Recognition Error",
                        message = err.message,
                        needsApiKey = err.needsApiKey,
                        onRetry = { viewModel.startListening() },
                        onOpenSettings = {
                            onOpenSettings()
                            onDismiss()
                        },
                        onClose = onDismiss
                    )
                }

                else -> {
                    // Idle state: Prompt to listen
                    IdleListeningPromptView(
                        onStart = { viewModel.startListening() },
                        onClose = onDismiss
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun MicrophonePermissionCard(
    onRequestPermission: () -> Unit
) {
    val extendedColors = LocalExtendedColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Mic,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Microphone Access Required",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Pulse listens to audio for 6 seconds solely to generate a transient acoustic fingerprint. Audio is never stored, recorded permanently, or uploaded for speech.",
            style = MaterialTheme.typography.bodyMedium,
            color = extendedColors.onSurfaceMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRequestPermission,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Grant Microphone Permission",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun ListeningView(
    isAnalyzing: Boolean,
    countdown: Int,
    amplitude: Float,
    onCancel: () -> Unit
) {
    val extendedColors = LocalExtendedColors.current
    val accent = MaterialTheme.colorScheme.primary

    // Dynamic scale responding to live audio amplitude
    val liveScale by animateFloatAsState(
        targetValue = 1.0f + (amplitude * 0.45f),
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "amplitudeScale"
    )

    // Smooth continuous ripple animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulseRipple")
    val rippleScale1 by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple1"
    )
    val rippleAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rippleAlpha1"
    )

    val rippleScale2 by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, delayMillis = 600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple2"
    )
    val rippleAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, delayMillis = 600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rippleAlpha2"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Visualizer Radar Pod
        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            // Ripple 1
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(rippleScale1)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = rippleAlpha1))
            )

            // Ripple 2
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(rippleScale2)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = rippleAlpha2))
            )

            // Dynamic Core Orb responding to audio volume
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(liveScale)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.22f))
                    .border(1.5.dp, accent.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(accent),
                    contentAlignment = Alignment.Center
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(36.dp),
                            color = Color.Black,
                            strokeWidth = 3.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Hearing,
                            contentDescription = "Listening",
                            tint = Color.Black,
                            modifier = Modifier.size(42.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        AnimatedContent(
            targetState = isAnalyzing,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "statusLabel"
        ) { analyzing ->
            if (analyzing) {
                Text(
                    text = "Analyzing acoustic fingerprint...",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Listening for music",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(accent.copy(alpha = 0.18f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${countdown}s",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = accent
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Hold your device steady near the audio source",
            style = MaterialTheme.typography.bodyMedium,
            color = extendedColors.onSurfaceMuted,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.height(44.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Cancel", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun RecognitionResultView(
    song: RecognizedSong,
    onPlayLocal: (Track) -> Unit,
    onSearchLibrary: (String) -> Unit,
    onIdentifyAgain: () -> Unit,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val extendedColors = LocalExtendedColors.current
    val accent = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Album Artwork Hero
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            if (!song.artworkUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(song.artworkUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Artwork for ${song.title}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.GraphicEq,
                    contentDescription = null,
                    tint = extendedColors.onSurfaceMuted,
                    modifier = Modifier.size(64.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Title & Artist
        Text(
            text = song.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${song.artist} • ${song.album}",
            style = MaterialTheme.typography.titleMedium,
            color = extendedColors.onSurfaceMuted,
            maxLines = 1,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis
        )

        if (!song.releaseDate.isNullOrEmpty() || !song.label.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(2.dp))
            val meta = listOfNotNull(song.releaseDate, song.label).joinToString(" • ")
            Text(
                text = meta,
                style = MaterialTheme.typography.labelSmall,
                color = extendedColors.onSurfaceSubtle
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Local Library Match Indicator
        if (song.localTrackMatch != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(extendedColors.statusSuccess.copy(alpha = 0.15f))
                    .border(0.75.dp, extendedColors.statusSuccess.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.LibraryMusic,
                        contentDescription = null,
                        tint = extendedColors.statusSuccess,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Found in your local library",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = extendedColors.statusSuccess
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { onPlayLocal(song.localTrackMatch) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Play Local Track",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(
                onClick = { onSearchLibrary(song.title) },
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.LibraryMusic,
                    contentDescription = "Search Library",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Library",
                    style = MaterialTheme.typography.labelMedium
                )
            }

            FilledTonalButton(
                onClick = {
                    val clip = ClipData.newPlainText("Song Info", "${song.title} - ${song.artist}")
                    (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
                    Toast.makeText(context, "Copied song info to clipboard", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.ContentCopy,
                    contentDescription = "Copy Song",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Copy",
                    style = MaterialTheme.typography.labelMedium
                )
            }

            if (!song.songLink.isNullOrEmpty()) {
                FilledTonalButton(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(song.songLink))
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    },
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                        contentDescription = "Open Link",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Web",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Bottom Actions: Identify Again / Done
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onIdentifyAgain) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Identify Another")
            }

            Button(
                onClick = onDone,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = "Done")
            }
        }
    }
}

@Composable
private fun RecognitionFailedView(
    title: String,
    message: String,
    needsApiKey: Boolean = false,
    onRetry: () -> Unit,
    onOpenSettings: () -> Unit = {},
    onClose: () -> Unit
) {
    val extendedColors = LocalExtendedColors.current
    val accent = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (needsApiKey) Icons.Rounded.Settings else Icons.Rounded.Hearing,
                contentDescription = null,
                tint = extendedColors.onSurfaceMuted,
                modifier = Modifier.size(34.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = extendedColors.onSurfaceMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (needsApiKey) {
            Button(
                onClick = onOpenSettings,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Configure AudD Key in Settings")
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onClose,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Close")
            }

            Button(
                onClick = onRetry,
                modifier = Modifier.weight(1f).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Try Again",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun IdleListeningPromptView(
    onStart: () -> Unit,
    onClose: () -> Unit
) {
    val extendedColors = LocalExtendedColors.current
    val accent = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(accent.copy(alpha = 0.16f))
                .clickable { onStart() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Hearing,
                contentDescription = "Identify Song",
                tint = accent,
                modifier = Modifier.size(42.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Identify Song Playing Nearby",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Tap below to listen for 6 seconds and discover track details, artwork, and matching files in your library.",
            style = MaterialTheme.typography.bodyMedium,
            color = extendedColors.onSurfaceMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accent),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.GraphicEq,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Start Listening",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.Black
            )
        }
    }
}
