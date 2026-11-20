package com.lastwave.app.data.update

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AppUpdateManagerTest {

    private val sampleAssets = listOf(
        ReleaseAsset(
            name = "BPlayer-v4.2.4-android7.apk",
            downloadUrl = "https://github.com/arybishal/BPlayer/releases/download/4.2.4/BPlayer-v4.2.4-android7.apk",
        ),
        ReleaseAsset(
            name = "BPlayer-v4.2.4-universal-uncompressed.apk",
            downloadUrl = "https://github.com/arybishal/BPlayer/releases/download/4.2.4/BPlayer-v4.2.4-universal-uncompressed.apk",
        ),
        ReleaseAsset(
            name = "BPlayer-v4.2.4-universal.apk",
            downloadUrl = "https://github.com/arybishal/BPlayer/releases/download/4.2.4/BPlayer-v4.2.4-universal.apk",
        ),
        ReleaseAsset(
            name = "SHA256SUMS.txt",
            downloadUrl = "https://github.com/arybishal/BPlayer/releases/download/4.2.4/SHA256SUMS.txt",
        ),
    )

    @Test
    fun classifyAsset_recognizesVariantsCorrectly() {
        assertThat(AppUpdateManager.classifyAsset("BPlayer-v4.2.4-android7.apk"))
            .isEqualTo(ApkVariant.ANDROID7)
        assertThat(AppUpdateManager.classifyAsset("BPlayer-v4.2.4-legacy.apk"))
            .isEqualTo(ApkVariant.ANDROID7)

        assertThat(AppUpdateManager.classifyAsset("BPlayer-v4.2.4-universal-uncompressed.apk"))
            .isEqualTo(ApkVariant.UNCOMPRESSED)
        assertThat(AppUpdateManager.classifyAsset("BPlayer-v4.2.4-raw.apk"))
            .isEqualTo(ApkVariant.UNCOMPRESSED)
        assertThat(AppUpdateManager.classifyAsset("BPlayer-v4.2.4-beta-raw.apk"))
            .isEqualTo(ApkVariant.UNCOMPRESSED)

        assertThat(AppUpdateManager.classifyAsset("BPlayer-v4.2.4-universal.apk"))
            .isEqualTo(ApkVariant.UNIVERSAL)
        assertThat(AppUpdateManager.classifyAsset("BPlayer-v4.2.4-release.apk"))
            .isEqualTo(ApkVariant.UNIVERSAL)
        assertThat(AppUpdateManager.classifyAsset("BPlayer-v4.2.4-beta.apk"))
            .isEqualTo(ApkVariant.UNIVERSAL)
    }

    @Test
    fun selectBestAsset_standardAndroid10Plus_selectsUniversalApk() {
        val selected = AppUpdateManager.selectBestAsset(
            assets = sampleAssets,
            isUncompressed = false,
            sdkInt = 34, // Android 14
        )
        assertThat(selected).isNotNull()
        assertThat(selected?.name).isEqualTo("BPlayer-v4.2.4-universal.apk")
        assertThat(selected?.downloadUrl).contains("BPlayer-v4.2.4-universal.apk")
    }

    @Test
    fun selectBestAsset_uncompressedBuild_selectsUncompressedApk() {
        val selected = AppUpdateManager.selectBestAsset(
            assets = sampleAssets,
            isUncompressed = true,
            sdkInt = 34, // Android 14
        )
        assertThat(selected).isNotNull()
        assertThat(selected?.name).isEqualTo("BPlayer-v4.2.4-universal-uncompressed.apk")
        assertThat(selected?.downloadUrl).contains("BPlayer-v4.2.4-universal-uncompressed.apk")
    }

    @Test
    fun selectBestAsset_legacyAndroid_selectsAndroid7Apk() {
        val selected = AppUpdateManager.selectBestAsset(
            assets = sampleAssets,
            isUncompressed = false,
            sdkInt = 26, // Android 8.0 Oreo
        )
        assertThat(selected).isNotNull()
        assertThat(selected?.name).isEqualTo("BPlayer-v4.2.4-android7.apk")
        assertThat(selected?.downloadUrl).contains("BPlayer-v4.2.4-android7.apk")
    }

    @Test
    fun selectBestAsset_ignoresNonApkAssets() {
        val nonApkOnly = listOf(
            ReleaseAsset(name = "SHA256SUMS.txt", downloadUrl = "https://example.com/sha"),
            ReleaseAsset(name = "notice.md", downloadUrl = "https://example.com/md"),
        )
        val selected = AppUpdateManager.selectBestAsset(
            assets = nonApkOnly,
            isUncompressed = false,
            sdkInt = 34,
        )
        assertThat(selected).isNull()
    }

    @Test
    fun selectBestAsset_fallbackWhenUniversalMissing_fallsBackToUncompressedOrAndroid7() {
        val assetsWithoutUniversal = listOf(
            ReleaseAsset(name = "BPlayer-v4.2.4-android7.apk", downloadUrl = "https://example.com/a7"),
            ReleaseAsset(name = "BPlayer-v4.2.4-universal-uncompressed.apk", downloadUrl = "https://example.com/raw"),
        )
        val selected = AppUpdateManager.selectBestAsset(
            assets = assetsWithoutUniversal,
            isUncompressed = false,
            sdkInt = 34,
        )
        assertThat(selected).isNotNull()
        assertThat(selected?.name).isEqualTo("BPlayer-v4.2.4-universal-uncompressed.apk")
    }

    private val assetsWithVivo = sampleAssets + ReleaseAsset(
        name = "BPlayer-v4.2.4-vivo-origin.apk",
        downloadUrl = "https://example.com/vivo",
    )

    @Test
    fun classifyAsset_recognizesVivoOrigin() {
        assertThat(AppUpdateManager.classifyAsset("BPlayer-v4.2.4-vivo-origin.apk"))
            .isEqualTo(ApkVariant.VIVO_ORIGIN)
    }

    @Test
    fun selectBestAsset_vivoBuild_onlySelectsVivoApk() {
        val selected = AppUpdateManager.selectBestAsset(
            assets = assetsWithVivo,
            isUncompressed = false,
            sdkInt = 34,
            isVivoOrigin = true,
        )
        assertThat(selected?.name).isEqualTo("BPlayer-v4.2.4-vivo-origin.apk")
    }

    @Test
    fun selectBestAsset_vivoBuild_withoutVivoApk_selectsNothing() {
        val selected = AppUpdateManager.selectBestAsset(
            assets = sampleAssets,
            isUncompressed = false,
            sdkInt = 34,
            isVivoOrigin = true,
        )
        assertThat(selected).isNull()
    }

    @Test
    fun isNewerVersion_detectsNewerVersionsCorrectly() {
        assertThat(AppUpdateManager.isNewerVersion("v1.2.1", "1.2.0")).isTrue()
        assertThat(AppUpdateManager.isNewerVersion("1.3.0", "1.2.0")).isTrue()
        assertThat(AppUpdateManager.isNewerVersion("2.0.0", "1.2.0")).isTrue()
        assertThat(AppUpdateManager.isNewerVersion("v1.2.0-beta", "1.2.0")).isFalse()
        assertThat(AppUpdateManager.isNewerVersion("1.2.0", "1.2.0")).isFalse()
        assertThat(AppUpdateManager.isNewerVersion("1.1.9", "1.2.0")).isFalse()
        assertThat(AppUpdateManager.isNewerVersion("", "1.2.0")).isFalse()
    }

    @Test
    fun updateDownloadState_implementsExpectedTypes() {
        val idle: UpdateDownloadState = UpdateDownloadState.Idle
        assertThat(idle).isInstanceOf(UpdateDownloadState.Idle::class.java)

        val downloading: UpdateDownloadState = UpdateDownloadState.Downloading(
            bytesDownloaded = 500L,
            totalBytes = 1000L,
            progress = 0.5f,
        )
        assertThat((downloading as UpdateDownloadState.Downloading).progress).isEqualTo(0.5f)

        val failed: UpdateDownloadState = UpdateDownloadState.Failed("Disk error")
        assertThat((failed as UpdateDownloadState.Failed).error).isEqualTo("Disk error")

        val ready: UpdateDownloadState = UpdateDownloadState.ReadyToInstall(java.io.File("test.apk"))
        assertThat((ready as UpdateDownloadState.ReadyToInstall).apkFile.name).isEqualTo("test.apk")
    }
}
