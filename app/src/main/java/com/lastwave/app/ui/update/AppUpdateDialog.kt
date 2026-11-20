package com.lastwave.app.ui.update

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lastwave.app.data.update.AppUpdateManager
import com.lastwave.app.data.update.UpdateDownloadState
import com.lastwave.app.data.update.UpdateInfo
import com.lastwave.app.ui.theme.LiquidGlassPreset
import com.lastwave.app.ui.theme.LocalLiquidGlass
import com.lastwave.app.ui.theme.liquidGlassChrome
import com.lastwave.app.ui.theme.liquidGlassContainerColor
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.inject.Inject

@HiltViewModel
class AppUpdateDialogViewModel @Inject constructor(
    val manager: AppUpdateManager,
) : ViewModel() {
    val info = manager.updateInfo
    val downloadState = manager.downloadState
}

/**
 * The in-app update dialog: opens by itself when a new release
 * is found on launch, and from Settings' "Update Ready".
 * Downloads and installs the APK in-app directly with live progress,
 * with a fallback option to open in browser if needed.
 */
@Composable
fun AppUpdateDialogHost(viewModel: AppUpdateDialogViewModel = hiltViewModel()) {
    val info by viewModel.info.collectAsStateWithLifecycle()
    val downloadState by viewModel.downloadState.collectAsStateWithLifecycle()
    if (!info.showDialog || !info.isUpdateAvailable) return
    val context = LocalContext.current
    val manager = viewModel.manager

    AppUpdateDialog(
        info = info,
        downloadState = downloadState,
        onStartDownload = {
            manager.downloadAndInstallUpdate(context)
        },
        onCancelDownload = {
            manager.cancelDownload()
        },
        onInstallApk = { apkFile ->
            manager.installApk(context, apkFile)
        },
        onOpenBrowser = {
            manager.openDownload(context)
        },
        onOpenGitHub = { manager.openReleasePage(context) },
        onClose = { skipThisVersion ->
            if (skipThisVersion) manager.dismissUpdate(info.latestVersion)
            manager.cancelDownload()
            manager.hideDialog()
        },
    )
}

@Composable
private fun AppUpdateDialog(
    info: UpdateInfo,
    downloadState: UpdateDownloadState,
    onStartDownload: () -> Unit,
    onCancelDownload: () -> Unit,
    onInstallApk: (File) -> Unit,
    onOpenBrowser: () -> Unit,
    onOpenGitHub: () -> Unit,
    onClose: (skipThisVersion: Boolean) -> Unit,
) {
    var skipThisVersion by rememberSaveable { mutableStateOf(false) }
    val dialogShape = RoundedCornerShape(28.dp)
    val isLiquid = LocalLiquidGlass.current

    Dialog(
        onDismissRequest = { onClose(skipThisVersion) },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            shape = dialogShape,
            color = liquidGlassContainerColor(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                enabled = isLiquid,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .widthIn(max = 560.dp)
                .liquidGlassChrome(
                    shape = dialogShape,
                    enabled = isLiquid,
                    preset = LiquidGlassPreset.ModalSheet,
                ),
        ) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.SystemUpdate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Update arrived",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            buildString {
                                append("v${info.latestVersion}")
                                if (info.currentVersion.isNotBlank()) append("  •  you have v${info.currentVersion}")
                                formatReleaseDate(info.publishedAt)?.let { append("  •  $it") }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (info.releaseTitle.isNotBlank() && !info.releaseTitle.contains(info.latestVersion)) {
                    Text(info.releaseTitle, style = MaterialTheme.typography.titleSmall)
                }

                val linkColor = MaterialTheme.colorScheme.primary
                val notes = remember(info.releaseNotes, linkColor) {
                    releaseNotesText(info.releaseNotes.ifBlank { "No release notes were published for this version." }, linkColor)
                }
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = if (isLiquid) 0.6f else 1f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        notes,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .heightIn(max = 240.dp)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                    )
                }

                info.downloadFileName?.let { name ->
                    Text(
                        if (info.downloadSizeBytes > 0) "$name  •  ${formatBytes(info.downloadSizeBytes)}" else name,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // In-App Download & Installation State Indicator
                when (downloadState) {
                    is UpdateDownloadState.Idle -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = skipThisVersion, onCheckedChange = { skipThisVersion = it })
                            Text("Don't show again for this version", style = MaterialTheme.typography.bodyMedium)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TextButton(onClick = onOpenGitHub) { Text("GitHub") }
                            TextButton(onClick = { onClose(skipThisVersion) }) { Text("Later") }
                            Button(onClick = onStartDownload) {
                                Icon(Icons.Filled.CloudDownload, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Download & Install")
                            }
                        }
                    }

                    is UpdateDownloadState.Downloading -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    "Downloading update...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                )
                                Text(
                                    "${(downloadState.progress * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                            LinearProgressIndicator(
                                progress = { downloadState.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    if (downloadState.totalBytes > 0) {
                                        "${formatBytes(downloadState.bytesDownloaded)} of ${formatBytes(downloadState.totalBytes)}"
                                    } else {
                                        formatBytes(downloadState.bytesDownloaded)
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            OutlinedButton(onClick = onCancelDownload) {
                                Text("Cancel")
                            }
                        }
                    }

                    is UpdateDownloadState.ReadyToInstall -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                "Download complete. Ready to install.",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TextButton(onClick = { onClose(skipThisVersion) }) { Text("Later") }
                            Button(onClick = { onInstallApk(downloadState.apkFile) }) {
                                Icon(Icons.Filled.SystemUpdate, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Install Now")
                            }
                        }
                    }

                    is UpdateDownloadState.Failed -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(
                                Icons.Filled.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                            )
                            Text(
                                "Download failed: ${downloadState.error}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TextButton(onClick = onOpenBrowser) {
                                Icon(Icons.Filled.OpenInBrowser, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("Browser")
                            }
                            TextButton(onClick = { onClose(skipThisVersion) }) { Text("Dismiss") }
                            Button(onClick = onStartDownload) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatBytes(bytes: Long): String = when {
    bytes >= 1024L * 1024L -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
    bytes >= 1024L -> "%.0f KB".format(bytes / 1024.0)
    else -> "$bytes B"
}

private fun formatReleaseDate(isoInstant: String): String? = runCatching {
    Instant.parse(isoInstant)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
}.getOrNull()
